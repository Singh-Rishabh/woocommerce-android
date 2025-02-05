package com.woocommerce.android.ui.common.webview

import android.webkit.WebView
import com.woocommerce.android.extensions.isNotNullOrEmpty
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.compose.component.web.WCWebViewEvent
import com.woocommerce.android.util.WooLog
import kotlinx.coroutines.flow.Flow
import org.wordpress.android.fluxc.store.AccountStore
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale
import javax.inject.Inject

private const val WPCOM_LOGIN_URL = "https://wordpress.com/wp-login.php"

class WebViewAuthenticator @Inject constructor(
    private val authenticationFlowResolver: WebViewAuthenticationFlowResolver,
    private val selectedSite: SelectedSite,
    private val accountStore: AccountStore
) {
    suspend fun authenticateAndLoadUrl(webView: WebView, url: String, webViewEvents: Flow<WCWebViewEvent>) {
        val authenticationFlow = authenticationFlowResolver.resolve(url)
        when (authenticationFlow) {
            WebViewAuthenticationFlowResolver.WebViewAuthenticationFlow.WPCom -> {
                authenticateWPComAndLoad(webView, url)
            }

            WebViewAuthenticationFlowResolver.WebViewAuthenticationFlow.JetpackSSO -> {
                authenticateSSOAndLoad(webView, url, webViewEvents)
            }

            WebViewAuthenticationFlowResolver.WebViewAuthenticationFlow.SiteCredentials -> {
                authenticateUsingSiteCredentialsAndLoad(webView, url)
            }

            WebViewAuthenticationFlowResolver.WebViewAuthenticationFlow.None -> {
                webView.loadUrl(url)
            }
        }
    }

    private fun authenticateWPComAndLoad(webView: WebView, url: String): Boolean {
        val postData = getWPComPostData(url)
        if (postData != null) {
            webView.postUrl(WPCOM_LOGIN_URL, postData.toByteArray())
            return true
        } else {
            webView.loadUrl(url)
            return false
        }
    }

    private suspend fun authenticateSSOAndLoad(webView: WebView, url: String, webViewEvents: Flow<WCWebViewEvent>) {
        // TODO
    }

    private fun authenticateUsingSiteCredentialsAndLoad(webView: WebView, url: String) {
        // TODO
    }

    @Suppress("ReturnCount")
    private fun getWPComPostData(redirectUrl: String): String? {
        require(accountStore.account.userName.isNotNullOrEmpty()) { "Username is required" }
        require(accountStore.accessToken.isNotNullOrEmpty()) { "Access token is required" }

        val username = accountStore.account.userName
        val token = accountStore.accessToken

        val utf8 = StandardCharsets.UTF_8.name()
        try {
            var postData = String.format(
                Locale.ROOT,
                "log=%s&redirect_to=%s",
                URLEncoder.encode(username, utf8),
                URLEncoder.encode(redirectUrl, utf8),
            )

            // Add token authorization
            postData += "&authorization=Bearer " + URLEncoder.encode(token, utf8)

            return postData
        } catch (e: UnsupportedEncodingException) {
            WooLog.e(WooLog.T.UTILS, e)
        }
        return null
    }
}
