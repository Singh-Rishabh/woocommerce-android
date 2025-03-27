package com.cataloghub.android.ui.prefs.domain

import android.net.Uri
import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.prefs.domain.DomainPurchaseViewModel.ViewState.LoadingState
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.getStateFlow
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class DomainPurchaseViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val appPrefsWrapper: AppPrefsWrapper,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    private val selectedSite: SelectedSite
) : ScopedViewModel(savedStateHandle) {
    companion object {
        const val CART_URL = "https://wordpress.com/checkout"
        const val WEBVIEW_SUCCESS_TRIGGER_KEYWORD = "https://wordpress.com/checkout/thank-you/"
        const val WEBVIEW_EXIT_TRIGGER_KEYWORD = "https://woocommerce.com/"
    }

    private val navArgs: DomainPurchaseFragmentArgs by savedStateHandle.navArgs()

    private val _viewState = savedStateHandle.getStateFlow<ViewState>(this, LoadingState)
    val viewState = _viewState.asLiveData()

    fun onExitTriggered() {
        triggerEvent(Exit)
    }

    fun onPurchaseSuccess() {
        analyticsTrackerWrapper.track(
            AnalyticsEvent.CUSTOM_DOMAIN_PURCHASE_SUCCESS,
            mapOf(
                AnalyticsTracker.KEY_SOURCE to appPrefsWrapper.getCustomDomainsSourceAsString(),
                AnalyticsTracker.KEY_USE_DOMAIN_CREDIT to false // the WebView is only used for non-credits purchases
            )
        )
        triggerEvent(NavigateToSuccessScreen(navArgs.domain))
    }

    init {
        analyticsTrackerWrapper.track(
            AnalyticsEvent.CUSTOM_DOMAINS_STEP,
            mapOf(
                AnalyticsTracker.KEY_SOURCE to appPrefsWrapper.getCustomDomainsSourceAsString(),
                AnalyticsTracker.KEY_STEP to AnalyticsTracker.VALUE_STEP_WEB_CHECKOUT
            )
        )

        _viewState.update {
            val siteHost = Uri.parse(selectedSite.get().url).host
            ViewState.CheckoutState(
                startUrl = "$CART_URL/$siteHost",
                successTriggerKeyword = WEBVIEW_SUCCESS_TRIGGER_KEYWORD,
                exitTriggerKeyword = WEBVIEW_EXIT_TRIGGER_KEYWORD
            )
        }
    }

    sealed interface ViewState : Parcelable {
        @Parcelize
        object LoadingState : ViewState

        @Parcelize
        data class CheckoutState(
            val startUrl: String,
            val successTriggerKeyword: String,
            val exitTriggerKeyword: String
        ) : ViewState
    }

    data class NavigateToSuccessScreen(val domain: String) : MultiLiveEvent.Event()
}
