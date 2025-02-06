package com.woocommerce.android.ui.common.webview

import com.woocommerce.android.extensions.isNotNullOrEmpty
import com.woocommerce.android.tools.SelectedSite
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.AccountStore
import javax.inject.Inject

class WebViewAuthenticationFlowResolver @Inject constructor(
    private val selectedSite: SelectedSite,
    private val accountStore: AccountStore
) {
    private val wpComAuthAcceptedDomains
        get() = listOf("wordpress.com", "wp.com", "jetpack.com", "woocommerce.com")

    fun resolve(url: String): WebViewAuthenticationFlow {
        val currentSite = selectedSite.getOrNull()
        val urlDomain = url.findDomain()
        val isWPComAuthenticated = accountStore.accessToken.isNotNullOrEmpty()
            && accountStore.account.userName.isNotNullOrEmpty()

        return if (isWPComAuthenticated) {
            when {
                currentSite?.isWPComAtomic == true ||
                    wpComAuthAcceptedDomains.any { it == urlDomain } -> WebViewAuthenticationFlow.WPCom

                currentSite?.supportsJetpackSSO() == true -> WebViewAuthenticationFlow.JetpackSSO

                else -> WebViewAuthenticationFlow.None
            }
        } else if (currentSite?.username.isNotNullOrEmpty() && currentSite?.password.isNotNullOrEmpty()) {
            WebViewAuthenticationFlow.SiteCredentials
        } else {
            WebViewAuthenticationFlow.None
        }
    }

    private fun String.findDomain(): String? = toHttpUrl().host

    private fun SiteModel.supportsJetpackSSO(): Boolean {
        return jetpackModules?.contains("sso") == true
    }

    enum class WebViewAuthenticationFlow {
        WPCom, JetpackSSO, SiteCredentials, None
    }
}
