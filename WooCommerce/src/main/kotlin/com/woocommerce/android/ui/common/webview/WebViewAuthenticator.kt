package com.woocommerce.android.ui.common.webview

import android.webkit.CookieManager
import android.webkit.WebView
import com.woocommerce.android.extensions.loginUrlOrDefault
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.compose.component.web.WCWebViewEvent
import com.woocommerce.android.util.WooLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.wordpress.android.fluxc.store.AccountStore
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

class WebViewAuthenticator @Inject constructor(
    private val authenticationFlowResolver: WebViewAuthenticationFlowResolver,
    private val selectedSite: SelectedSite,
    private val accountStore: AccountStore,
    private val webViewCookieManager: CookieManager
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
        val postData = prepareLoginPostData(
            redirectUrl = url,
            username = accountStore.account.userName,
            authorizationParam = "authorization" to "Bearer ${accountStore.accessToken}"
        )

        if (postData != null) {
            webView.postUrl(WPCOM_LOGIN_URL, postData.toByteArray())
            return true
        } else {
            webView.loadUrl(url)
            return false
        }
    }

    private suspend fun authenticateSSOAndLoad(webView: WebView, url: String, webViewEvents: Flow<WCWebViewEvent>) {
        authenticateWPComAndLoad(webView, JETPACK_SSO_TEMP_REDIRECT_URL).also {
            if (!it) {
                // The authentication failed, so load the original URL
                webView.loadUrl(url)
                return
            }
        }

        // Wait for the WPCom login to complete
        webViewEvents.first { it is WCWebViewEvent.UrlLoaded && it.url == JETPACK_SSO_TEMP_REDIRECT_URL }

        // Handle SSO login and redirect back to the original URL
        val site = selectedSite.get()
        webViewCookieManager.setCookie(site.url, "jetpack_sso_redirect_to=$url")
        val ssoLoginUrl = site.loginUrlOrDefault.toHttpUrl().newBuilder()
            .addQueryParameter("action", "jetpack-sso")
            .build()
            .toString()

        webView.loadUrl(ssoLoginUrl)
    }

    private fun authenticateUsingSiteCredentialsAndLoad(webView: WebView, url: String) {
        val site = selectedSite.get()

        val postData = prepareLoginPostData(
            redirectUrl = url,
            username = site.username,
            authorizationParam = "pwd" to site.password
        )

        if (postData != null) {
            webView.postUrl(site.loginUrlOrDefault, postData.toByteArray())
        } else {
            webView.loadUrl(url)
        }
    }

    private fun prepareLoginPostData(
        redirectUrl: String,
        username: String,
        authorizationParam: Pair<String, String>,
    ): String? {
        val utf8 = StandardCharsets.UTF_8.name()
        val (authorizationKey, authorizationValue) = authorizationParam
        return try {
            buildString {
                append("redirect_to=").append(URLEncoder.encode(redirectUrl, utf8))

                append("&log=").append(URLEncoder.encode(username, utf8))

                append("&${URLEncoder.encode(authorizationKey, utf8)}=")
                    .append(URLEncoder.encode(authorizationValue, utf8))

            }
        } catch (e: UnsupportedEncodingException) {
            WooLog.e(WooLog.T.UTILS, e)
            null
        }
    }

    companion object {
        private const val WPCOM_LOGIN_URL = "https://wordpress.com/wp-login.php"
        const val JETPACK_SSO_TEMP_REDIRECT_URL = "https://wordpress.com/mobile-redirect"
    }
}
