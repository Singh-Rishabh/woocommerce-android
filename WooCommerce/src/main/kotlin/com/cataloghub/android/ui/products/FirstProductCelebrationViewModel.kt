package com.cataloghub.android.ui.products

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.extensions.isSitePublic
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FirstProductCelebrationViewModel @Inject constructor(
    private val tracker: AnalyticsTrackerWrapper,
    private val selectedSite: SelectedSite,
    savedStateHandle: SavedStateHandle
) : ScopedViewModel(savedStateHandle) {
    private val navArgs: FirstProductCelebrationDialogArgs by savedStateHandle.navArgs()

    val showShareButton = selectedSite.get().isSitePublic

    init {
        tracker.track(AnalyticsEvent.FIRST_CREATED_PRODUCT_SHOWN)
    }
    fun onShareButtonClicked() {
        tracker.track(AnalyticsEvent.FIRST_CREATED_PRODUCT_SHARE_TAPPED)
        triggerEvent(ProductNavigationTarget.ShareProduct(navArgs.permalink, navArgs.productName))
    }

    fun onDismissButtonClicked() {
        triggerEvent(Exit)
    }
}
