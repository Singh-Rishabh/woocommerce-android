package com.cataloghub.android.ui.sitepicker.sitediscovery

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.NavGraphMainDirections
import com.cataloghub.android.R
import com.cataloghub.android.extensions.handleNotice
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.navigateToHelpScreen
import com.cataloghub.android.model.JetpackStatus
import com.cataloghub.android.support.help.HelpOrigin
import com.cataloghub.android.support.requests.SupportRequestFormActivity
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.common.webview.AuthenticatedWebViewFragment
import com.cataloghub.android.ui.common.webview.AuthenticatedWebViewViewModel
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.login.LoginActivity
import com.cataloghub.android.ui.login.accountmismatch.AccountMismatchErrorFragment
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.sitepicker.sitediscovery.SitePickerSiteDiscoveryViewModel.CreateZendeskTicket
import com.cataloghub.android.ui.sitepicker.sitediscovery.SitePickerSiteDiscoveryViewModel.StartNativeJetpackActivation
import com.cataloghub.android.ui.sitepicker.sitediscovery.SitePickerSiteDiscoveryViewModel.StartWebBasedJetpackInstallation
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Logout
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.NavigateToHelpScreen
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.login.LoginMode

@AndroidEntryPoint
class SitePickerSiteDiscoveryFragment : BaseFragment() {
    companion object {
        const val SITE_PICKER_SITE_ADDRESS_RESULT = "site-url"
        private const val JETPACK_CONNECT_URL = "https://wordpress.com/jetpack/connect"
        private const val JETPACK_CONNECTED_REDIRECT_URL = "cataloghub://jetpack-connected"
    }

    private val viewModel: SitePickerSiteDiscoveryViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireActivity()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    SitePickerSiteDiscoveryScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()
        setupResultHandlers()
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is CreateZendeskTicket -> startSupportRequestForm()
                is NavigateToHelpScreen -> navigateToHelpScreen(event.origin)
                is StartWebBasedJetpackInstallation -> startWebBasedJetpackInstallation(event.siteAddress)
                is StartNativeJetpackActivation -> startNativeJetpackActivation(event)
                is Logout -> onLogout()
                is ExitWithResult<*> -> navigateBackWithResult(SITE_PICKER_SITE_ADDRESS_RESULT, event.data)
                is Exit -> findNavController().navigateUp()
            }
        }
    }

    private fun setupResultHandlers() {
        handleNotice(AuthenticatedWebViewFragment.WEBVIEW_RESULT) {
            viewModel.onJetpackInstalled()
        }
        handleNotice(AccountMismatchErrorFragment.JETPACK_CONNECTED_NOTICE) {
            viewModel.onJetpackConnected()
        }
    }

    private fun startWebBasedJetpackInstallation(siteAddress: String) {
        val url = "$JETPACK_CONNECT_URL?" +
            "url=$siteAddress" +
            "&mobile_redirect=$JETPACK_CONNECTED_REDIRECT_URL" +
            "&from=mobile"

        findNavController().navigate(
            NavGraphMainDirections.actionGlobalAuthenticatedWebViewFragment(
                urlToLoad = url,
                urlsToTriggerExit = arrayOf(JETPACK_CONNECTED_REDIRECT_URL),
                urlComparisonMode = AuthenticatedWebViewViewModel.UrlComparisonMode.EQUALITY,
                title = getString(R.string.login_jetpack_install)
            )
        )
    }

    private fun startNativeJetpackActivation(event: StartNativeJetpackActivation) {
        findNavController().navigate(
            SitePickerSiteDiscoveryFragmentDirections
                .actionSitePickerSiteDiscoveryFragmentToJetpackActivation(
                    siteUrl = event.siteAddress,
                    jetpackStatus = JetpackStatus(
                        isJetpackInstalled = event.isJetpackInstalled,
                        isJetpackConnected = false,
                        wpComEmail = null
                    )
                )
        )
    }

    private fun startSupportRequestForm() {
        startActivity(
            SupportRequestFormActivity.createIntent(
                context = requireActivity(),
                origin = HelpOrigin.LOGIN_SITE_ADDRESS,
                extraTags = ArrayList()
            )
        )
    }

    private fun onLogout() {
        requireActivity().setResult(Activity.RESULT_CANCELED)
        val intent = Intent(context, LoginActivity::class.java)
        LoginMode.WOO_LOGIN_MODE.putInto(intent)
        startActivity(intent)
        requireActivity().finish()
    }
}
