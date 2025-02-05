package com.woocommerce.android.ui.compose.component.web

import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

open class DefaultWebViewClient : WebViewClient() {
    private val _eventsObservable = MutableSharedFlow<WebViewEvent>(extraBufferCapacity = Int.MAX_VALUE)
    val eventsObservable: Flow<WebViewEvent> = _eventsObservable.asSharedFlow()

    override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
        url?.let { _eventsObservable.tryEmit(WebViewEvent.UrlLoaded(it)) }
    }

    override fun onLoadResource(view: WebView?, url: String?) {
        url?.let { _eventsObservable.tryEmit(WebViewEvent.UrlLoaded(it)) }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        url?.let { _eventsObservable.tryEmit(WebViewEvent.PageFinished(it)) }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        request?.url?.let { url ->
            _eventsObservable.tryEmit(WebViewEvent.UrlFailed(url.toString(), error?.errorCode))
        }
    }

    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        request?.url?.let { url ->
            _eventsObservable.tryEmit(WebViewEvent.UrlFailed(url.toString(), errorResponse?.statusCode))
        }
    }

    sealed interface WebViewEvent {
        val url: String

        data class UrlLoaded(override val url: String) : WebViewEvent
        data class PageFinished(override val url: String) : WebViewEvent
        data class UrlFailed(override val url: String, val errorCode: Int?) : WebViewEvent
    }
}
