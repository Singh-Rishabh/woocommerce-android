package com.cataloghub.android.ui.dashboard.google

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cataloghub.android.AppUrls
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.extensions.adminUrlOrDefault
import com.cataloghub.android.extensions.formatToString
import com.cataloghub.android.model.DashboardWidget
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.dashboard.DashboardViewModel
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetAction
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetMenu
import com.cataloghub.android.ui.dashboard.DashboardViewModel.RefreshEvent
import com.cataloghub.android.ui.dashboard.defaultHideMenuEntry
import com.cataloghub.android.ui.google.HasGoogleAdsCampaigns
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.transformLatest
import org.wordpress.android.fluxc.store.WCGoogleStore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@HiltViewModel(assistedFactory = DashboardGoogleAdsViewModel.Factory::class)
@Suppress("MagicNumber", "LongParameterList", "UnusedPrivateMember")
class DashboardGoogleAdsViewModel @AssistedInject constructor(
    savedStateHandle: SavedStateHandle,
    private val selectedSite: SelectedSite,
    @Assisted private val parentViewModel: DashboardViewModel,
    private val hasGoogleAdsCampaigns: HasGoogleAdsCampaigns,
    private val googleAdsStore: WCGoogleStore,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) : ScopedViewModel(savedStateHandle) {
    companion object {
        private const val EPOCH_DATE = "1970-01-01"
    }

    private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val currentDate: String = dateFormatter.format(LocalDateTime.now())

    private val _refreshTrigger = MutableSharedFlow<RefreshEvent>(extraBufferCapacity = 1)
    private val refreshTrigger = merge(_refreshTrigger, (parentViewModel.refreshTrigger))
        .onStart { emit(RefreshEvent()) }

    private var storeHasCampaigns = false

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState = refreshTrigger
        .transformLatest {
            emit(DashboardGoogleAdsState.Loading)

            hasGoogleAdsCampaigns().fold(
                onSuccess = { hasCampaigns ->
                    analyticsTrackerWrapper.track(
                        stat = AnalyticsEvent.GOOGLEADS_ENTRY_POINT_DISPLAYED,
                        properties = mapOf(
                            AnalyticsTracker.KEY_GOOGLEADS_SOURCE
                                to AnalyticsTracker.VALUE_GOOGLEADS_ENTRY_POINT_SOURCE_MYSTORE
                        )
                    )

                    emit(
                        if (hasCampaigns) {
                            storeHasCampaigns = true

                            googleAdsStore.fetchImpressionsAndClicks(
                                site = selectedSite.get(),
                                startDate = EPOCH_DATE, // To get all time data
                                endDate = currentDate
                            ).let { result ->

                                when {
                                    result.isError -> DashboardGoogleAdsState.Error(widgetMenu)
                                    else -> {
                                        val data = result.model!!
                                        val campaignButton = DashboardWidgetAction(
                                            titleResource =
                                            R.string.dashboard_google_ads_card_view_all_campaigns_button,
                                            action = { launchCampaignDetails() }
                                        )
                                        DashboardGoogleAdsState.HasCampaigns(
                                            impressions = data.impressions.formatToString(),
                                            clicks = data.clicks.formatToString(),
                                            onCreateCampaignClicked = { launchCampaignCreation() },
                                            onPerformanceAreaClicked = { launchCampaignDetails() },
                                            showAllCampaignsButton = campaignButton,
                                            menu = widgetMenu
                                        )
                                    }
                                }
                            }
                        } else {
                            storeHasCampaigns = false

                            DashboardGoogleAdsState.NoCampaigns(
                                onCreateCampaignClicked = { launchCampaignCreation() },
                                menu = widgetMenu
                            )
                        }
                    )
                },
                onFailure = {
                    emit(DashboardGoogleAdsState.Error(widgetMenu))
                }
            )
        }
        .asLiveData()

    private val widgetMenu = DashboardWidgetMenu(
        items = listOf(
            DashboardWidget.Type.GOOGLE_ADS.defaultHideMenuEntry {
                parentViewModel.onHideWidgetClicked(DashboardWidget.Type.GOOGLE_ADS)
            }
        )
    )

    private fun launchCampaignCreation() {
        analyticsTrackerWrapper.track(
            stat = AnalyticsEvent.GOOGLEADS_ENTRY_POINT_TAPPED,
            properties = mapOf(
                AnalyticsTracker.KEY_GOOGLEADS_SOURCE
                    to AnalyticsTracker.VALUE_GOOGLEADS_ENTRY_POINT_SOURCE_MYSTORE,
                AnalyticsTracker.KEY_GOOGLEADS_TYPE
                    to AnalyticsTracker.VALUE_GOOGLEADS_ENTRY_POINT_TYPE_CREATION,
                AnalyticsTracker.KEY_GOOGLEADS_HAS_CAMPAIGNS
                    to storeHasCampaigns
            )
        )

        val creationUrl = selectedSite.get().adminUrlOrDefault + AppUrls.GOOGLE_ADMIN_CAMPAIGN_CREATION_SUFFIX
        triggerEvent(ViewGoogleForWooEvent(creationUrl, isCreationFlow = true))
    }

    private fun launchCampaignDetails() {
        analyticsTrackerWrapper.track(
            stat = AnalyticsEvent.GOOGLEADS_ENTRY_POINT_TAPPED,
            properties = mapOf(
                AnalyticsTracker.KEY_GOOGLEADS_SOURCE
                    to AnalyticsTracker.VALUE_GOOGLEADS_ENTRY_POINT_SOURCE_MYSTORE,
                AnalyticsTracker.KEY_GOOGLEADS_TYPE
                    to AnalyticsTracker.VALUE_GOOGLEADS_ENTRY_POINT_TYPE_DASHBOARD,
                AnalyticsTracker.KEY_GOOGLEADS_HAS_CAMPAIGNS
                    to storeHasCampaigns
            )
        )

        val adminUrl = selectedSite.get().adminUrlOrDefault + AppUrls.GOOGLE_ADMIN_DASHBOARD
        triggerEvent(ViewGoogleForWooEvent(adminUrl, isCreationFlow = false))
    }

    fun onRefresh() {
        _refreshTrigger.tryEmit(RefreshEvent(isForced = true))
    }

    sealed class DashboardGoogleAdsState(
        open val menu: DashboardWidgetMenu,
        val mainButton: DashboardWidgetAction? = null
    ) {
        data object Loading : DashboardGoogleAdsState(DashboardWidgetMenu(emptyList()))
        data class Error(
            override val menu: DashboardWidgetMenu
        ) : DashboardGoogleAdsState(menu)

        data class NoCampaigns(
            val onCreateCampaignClicked: () -> Unit,
            override val menu: DashboardWidgetMenu
        ) : DashboardGoogleAdsState(menu)

        data class HasCampaigns(
            val impressions: String,
            val clicks: String,
            val onCreateCampaignClicked: () -> Unit,
            val onPerformanceAreaClicked: () -> Unit,
            val showAllCampaignsButton: DashboardWidgetAction,
            override val menu: DashboardWidgetMenu
        ) : DashboardGoogleAdsState(menu, showAllCampaignsButton)
    }

    data class ViewGoogleForWooEvent(val url: String, val isCreationFlow: Boolean) : MultiLiveEvent.Event()

    object NavigateToGoogleAdsSuccessEvent : MultiLiveEvent.Event()

    @AssistedFactory
    interface Factory {
        fun create(parentViewModel: DashboardViewModel?): DashboardGoogleAdsViewModel
    }
}
