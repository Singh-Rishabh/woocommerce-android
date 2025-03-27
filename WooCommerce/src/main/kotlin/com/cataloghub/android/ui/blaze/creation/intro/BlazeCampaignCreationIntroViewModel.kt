package com.cataloghub.android.ui.blaze.creation.intro

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.analytics.AnalyticsEvent.BLAZE_ENTRY_POINT_TAPPED
import com.cataloghub.android.analytics.AnalyticsEvent.BLAZE_INTRO_DISPLAYED
import com.cataloghub.android.analytics.AnalyticsEvent.BLAZE_INTRO_LEARN_MORE_TAPPED
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.ui.blaze.BlazeUrlsHelper.BlazeFlowSource
import com.cataloghub.android.ui.products.AddProductNavigator
import com.cataloghub.android.ui.products.ProductStatus
import com.cataloghub.android.ui.products.list.ProductListRepository
import com.cataloghub.android.util.CoroutineDispatchers
import com.cataloghub.android.util.WooLog
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WCProductStore.ProductFilterOption
import org.wordpress.android.fluxc.store.WCProductStore.ProductSorting
import javax.inject.Inject

@HiltViewModel
class BlazeCampaignCreationIntroViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val productListRepository: ProductListRepository,
    private val coroutineDispatchers: CoroutineDispatchers,
    private val analyticsTracker: AnalyticsTrackerWrapper,
    val addProductNavigator: AddProductNavigator
) : ScopedViewModel(savedStateHandle) {
    private val navArgs: BlazeCampaignCreationIntroFragmentArgs by savedStateHandle.navArgs()
    fun onContinueClick() {
        suspend fun getPublishedProducts() = withContext(coroutineDispatchers.io) {
            analyticsTracker.track(
                stat = BLAZE_ENTRY_POINT_TAPPED,
                properties = mapOf(AnalyticsTracker.KEY_BLAZE_SOURCE to BlazeFlowSource.INTRO_VIEW.trackingName)
            )
            productListRepository.getProductList(
                productFilterOptions = mapOf(ProductFilterOption.STATUS to ProductStatus.PUBLISH.value),
                sortType = ProductSorting.DATE_DESC,
            ).filterNot { it.isSampleProduct }
        }

        launch {
            if (navArgs.productId != -1L) {
                triggerEvent(ShowCampaignCreationForm(navArgs.productId, BlazeFlowSource.INTRO_VIEW))
            } else {
                val products = getPublishedProducts()
                when {
                    products.size == 1 -> triggerEvent(
                        ShowCampaignCreationForm(
                            products.first().remoteId,
                            BlazeFlowSource.INTRO_VIEW
                        )
                    )

                    products.isNotEmpty() -> triggerEvent(ShowProductSelector)
                    else -> {
                        WooLog.w(WooLog.T.BLAZE, "No products available to create a campaign")
                        triggerEvent(ShowNoProductDialog)
                    }
                }
            }
        }
    }

    init {
        analyticsTracker.track(
            stat = BLAZE_INTRO_DISPLAYED,
            properties = mapOf(AnalyticsTracker.KEY_BLAZE_SOURCE to navArgs.source.trackingName)
        )
    }

    fun onDismissClick() {
        triggerEvent(Exit)
    }

    fun onProductSelected(productId: Long) {
        triggerEvent(ShowCampaignCreationForm(productId, BlazeFlowSource.INTRO_VIEW))
    }

    fun onLearnMoreClick() {
        analyticsTracker.track(stat = BLAZE_INTRO_LEARN_MORE_TAPPED)
    }

    fun onCreateProductClicked() {
        triggerEvent(NavigateToAddProduct)
    }

    object ShowProductSelector : MultiLiveEvent.Event()
    data class ShowCampaignCreationForm(val productId: Long, val source: BlazeFlowSource) : MultiLiveEvent.Event()
    object ShowNoProductDialog : MultiLiveEvent.Event()
    object NavigateToAddProduct : MultiLiveEvent.Event()
}
