package com.cataloghub.android.ui.google.webview

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.AppUrls
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.ui.common.webview.WebViewAuthenticator
import com.cataloghub.android.ui.google.CanUseAutoLoginWebview
import com.cataloghub.android.ui.google.webview.GoogleAdsWebViewViewModel.UrlComparisonMode.EQUALITY
import com.cataloghub.android.ui.google.webview.GoogleAdsWebViewViewModel.UrlComparisonMode.PARTIAL
import com.cataloghub.android.ui.google.webview.GoogleAdsWebViewViewModel.UrlComparisonMode.STARTS_WITH
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import org.wordpress.android.fluxc.network.UserAgent
import javax.inject.Inject

@HiltViewModel
class GoogleAdsWebViewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val webViewAuthenticator: WebViewAuthenticator,
    val canUseAutoLoginWebview: CanUseAutoLoginWebview,
    val userAgent: UserAgent,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) : ScopedViewModel(savedStateHandle) {
    private val navArgs: GoogleAdsWebViewFragmentArgs by savedStateHandle.navArgs()
    private var isExiting = false

    // `WebView`'s onPageFinished callback is called multiple times when loading the same URL once,
    // so this flag is needed for callback to be called exactly one time.
    private var isUrlToLoadFinishedOnce = false

    // `WebView`s onPageFinished callback is still called even if the url failed to load.
    // This flag is used to ensure the callback can return early.
    private var isUrlLoadingFailed = false

    private val successUrlTriggers = listOf(
        AppUrls.GOOGLE_ADMIN_FIRST_CAMPAIGN_CREATION_SUCCESS_TRIGGER,
        AppUrls.GOOGLE_ADMIN_SUBSEQUENT_CAMPAIGN_CREATION_SUCCESS_TRIGGER,
        AppUrls.GOOGLE_ADMIN_SUBMISSION_SUCCESS_TRIGGER
    )

    val viewState = navArgs.let {
        ViewState(
            urlToLoad = it.urlToLoad,
            title = it.title,
            displayMode = it.displayMode,
            captureBackButton = it.captureBackButton,
            clearCache = it.clearCache,
            canUseAutoLoginWebview = canUseAutoLoginWebview(),
            isCreationFlow = it.isCreationFlow,
            source = it.entryPointSource
        )
    }

    private val sourceValue = when (viewState.source) {
        EntryPointSource.MORE_MENU -> AnalyticsTracker.VALUE_GOOGLEADS_ENTRY_POINT_SOURCE_MOREMENU
        EntryPointSource.MYSTORE -> AnalyticsTracker.VALUE_GOOGLEADS_ENTRY_POINT_SOURCE_MYSTORE
        EntryPointSource.ANALYTICS_HUB -> AnalyticsTracker.VALUE_GOOGLEADS_ENTRY_POINT_TYPE_ANALYTICS_HUB
    }

    fun onUrlLoaded(url: String) {
        fun String.matchesUrl(url: String) = when (navArgs.urlComparisonMode) {
            PARTIAL -> url.contains(this, ignoreCase = true)
            EQUALITY -> equals(url, ignoreCase = true)
            STARTS_WITH -> url.startsWith(this, ignoreCase = true)
        }

        if (successUrlTriggers.any { it.matchesUrl(url) } && !isExiting) {
            if (viewState.isCreationFlow) {
                analyticsTrackerWrapper.track(
                    stat = AnalyticsEvent.GOOGLEADS_CAMPAIGN_CREATION_SUCCESS,
                    properties = mapOf(
                        AnalyticsTracker.KEY_GOOGLEADS_SOURCE to sourceValue
                    )
                )
            }

            isExiting = true
            triggerEvent(ExitWithResult(Unit))
        }
    }

    fun onPageFinished(url: String) {
        if (isUrlLoadingFailed) {
            isUrlLoadingFailed = false
            return
        }

        if (url == viewState.urlToLoad && !isUrlToLoadFinishedOnce && viewState.isCreationFlow) {
            analyticsTrackerWrapper.track(
                stat = AnalyticsEvent.GOOGLEADS_FLOW_STARTED,
                properties = mapOf(
                    AnalyticsTracker.KEY_GOOGLEADS_SOURCE to sourceValue
                )
            )
            isUrlToLoadFinishedOnce = true
        }
    }

    fun onClose() {
        if (viewState.isCreationFlow) {
            analyticsTrackerWrapper.track(
                stat = AnalyticsEvent.GOOGLEADS_FLOW_CANCELED,
                properties = mapOf(
                    AnalyticsTracker.KEY_GOOGLEADS_SOURCE to sourceValue
                )
            )
        }

        triggerEvent(Exit)
    }

    fun onUrlFailed(url: String, errorCode: Int?) {
        if (viewState.isCreationFlow) {
            analyticsTrackerWrapper.track(
                stat = AnalyticsEvent.GOOGLEADS_FLOW_ERROR,
                properties = mapOf(
                    AnalyticsTracker.KEY_GOOGLEADS_SOURCE to sourceValue,
                    AnalyticsTracker.KEY_URL to url,
                    AnalyticsTracker.KEY_ERROR to errorCode
                )
            )
            isUrlLoadingFailed = true
        }
    }

    data class ViewState(
        val urlToLoad: String,
        val title: String?,
        val displayMode: DisplayMode,
        val captureBackButton: Boolean,
        val clearCache: Boolean = false,
        val canUseAutoLoginWebview: Boolean,
        val isCreationFlow: Boolean,
        val source: EntryPointSource
    )

    enum class UrlComparisonMode {
        PARTIAL, EQUALITY, STARTS_WITH
    }

    enum class DisplayMode {
        REGULAR, MODAL
    }

    enum class EntryPointSource {
        MORE_MENU, MYSTORE, ANALYTICS_HUB
    }
}
