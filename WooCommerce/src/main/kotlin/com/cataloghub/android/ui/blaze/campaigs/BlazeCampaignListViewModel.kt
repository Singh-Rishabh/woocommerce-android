package com.cataloghub.android.ui.blaze.campaigs

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent.BLAZE_CAMPAIGN_DETAIL_SELECTED
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.extensions.NumberExtensionsWrapper
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.blaze.BlazeCampaignUi
import com.cataloghub.android.ui.blaze.BlazeUrlsHelper.BlazeFlowSource
import com.cataloghub.android.ui.blaze.detail.BlazeCampaignDetailWebViewViewModel
import com.cataloghub.android.ui.blaze.detail.BlazeCampaignDetailWebViewViewModel.BlazeAction.CampaignStopped
import com.cataloghub.android.ui.blaze.detail.BlazeCampaignDetailWebViewViewModel.BlazeAction.None
import com.cataloghub.android.ui.blaze.detail.BlazeCampaignDetailWebViewViewModel.BlazeAction.PromoteProductAgain
import com.cataloghub.android.ui.blaze.toUiState
import com.cataloghub.android.util.CurrencyFormatter
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.withIndex
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.store.blaze.BlazeCampaignsStore
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class BlazeCampaignListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val blazeCampaignsStore: BlazeCampaignsStore,
    private val selectedSite: SelectedSite,
    private val appPrefsWrapper: AppPrefsWrapper,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    private val currencyFormatter: CurrencyFormatter,
    private val numberExtensionsWrapper: NumberExtensionsWrapper
) : ScopedViewModel(savedStateHandle) {
    companion object {
        private const val LOADING_TRANSITION_DELAY = 200L
    }

    private val navArgs: BlazeCampaignListFragmentArgs by savedStateHandle.navArgs()

    private var totalItems = 0
    private val isLoadingMore = MutableStateFlow(false)
    private val isCampaignCelebrationShown = MutableStateFlow(false)

    val state = combine(
        blazeCampaignsStore.observeBlazeCampaigns(selectedSite.get()),
        isLoadingMore.withIndex().debounce { (index, isLoading) ->
            if (index != 0 && !isLoading) {
                // When resetting to not loading, wait a bit to make sure the campaigns list has been fetched from DB
                LOADING_TRANSITION_DELAY
            } else {
                0L
            }
        }.map { it.value },
        isCampaignCelebrationShown
    ) { campaigns, loadingMore, isBlazeCelebrationScreenShown ->
        BlazeCampaignListState(
            campaigns = campaigns.map {
                ClickableCampaign(
                    campaignUi = it.toUiState(currencyFormatter, numberExtensionsWrapper),
                    onCampaignClicked = { onCampaignClicked(it.campaignId) }
                )
            },
            onAddNewCampaignClicked = { onAddNewCampaignClicked() },
            isLoading = loadingMore,
            isCampaignCelebrationShown = isBlazeCelebrationScreenShown
        )
    }.asLiveData()

    init {
        if (navArgs.isPostCampaignCreation) {
            showCampaignCelebrationIfNeeded()
        }
        if (navArgs.campaignId != null) {
            triggerEvent(
                ShowCampaignDetails(campaignId = navArgs.campaignId!!)
            )
        }
        launch {
            loadCampaigns(offset = 0)
        }
    }

    fun onLoadMoreCampaigns() {
        val offset = state.value?.campaigns?.size ?: 0
        if (!isLoadingMore.value && offset < totalItems) {
            launch {
                isLoadingMore.value = true
                loadCampaigns(offset)
                isLoadingMore.value = false
            }
        }
    }

    fun onCampaignCelebrationDismissed() {
        isCampaignCelebrationShown.value = false
    }

    private suspend fun loadCampaigns(offset: Int) {
        val result = blazeCampaignsStore.fetchBlazeCampaigns(selectedSite.get(), offset)
        if (result.isError || result.model == null) {
            triggerEvent(Event.ShowSnackbar(R.string.blaze_campaign_list_error_fetching_campaigns))
        } else {
            totalItems = result.model?.totalItems ?: 0
        }
    }

    private fun onCampaignClicked(campaignId: String) {
        analyticsTrackerWrapper.track(
            stat = BLAZE_CAMPAIGN_DETAIL_SELECTED,
            properties = mapOf(AnalyticsTracker.KEY_BLAZE_SOURCE to BlazeFlowSource.CAMPAIGN_LIST.trackingName)
        )
        triggerEvent(
            ShowCampaignDetails(campaignId)
        )
    }

    private fun onAddNewCampaignClicked() {
        triggerEvent(LaunchBlazeCampaignCreation(BlazeFlowSource.CAMPAIGN_LIST))
    }

    private fun showCampaignCelebrationIfNeeded() {
        if (!appPrefsWrapper.isBlazeCelebrationScreenShown) {
            isCampaignCelebrationShown.value = true
            appPrefsWrapper.isBlazeCelebrationScreenShown = true
        }
    }

    fun onBlazeCampaignWebViewAction(action: BlazeCampaignDetailWebViewViewModel.BlazeAction) {
        when (action) {
            CampaignStopped -> launch { loadCampaigns(offset = 0) }
            is PromoteProductAgain -> triggerEvent(
                LaunchBlazeCampaignCreationForProduct(
                    productId = action.productId,
                    source = BlazeFlowSource.CAMPAIGN_LIST
                )
            )

            None -> Unit // Do nothing
        }
    }

    data class BlazeCampaignListState(
        val campaigns: List<ClickableCampaign>,
        val onAddNewCampaignClicked: () -> Unit,
        val isLoading: Boolean,
        val isCampaignCelebrationShown: Boolean
    )

    data class ClickableCampaign(
        val campaignUi: BlazeCampaignUi,
        val onCampaignClicked: () -> Unit,
    )

    data class LaunchBlazeCampaignCreation(val source: BlazeFlowSource) : Event()
    data class LaunchBlazeCampaignCreationForProduct(
        val productId: Long?,
        val source: BlazeFlowSource,
    ) : Event()

    data class ShowCampaignDetails(val campaignId: String) : Event()
}
