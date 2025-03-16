package com.cataloghub.android.ui.login.jetpack.wpcom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.cataloghub.android.NavGraphJetpackInstallDirections
import com.cataloghub.android.R
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.login.jetpack.wpcom.JetpackActivationWPComPostLoginViewModel.ShowJetpackActivationScreen
import com.cataloghub.android.ui.login.jetpack.wpcom.JetpackActivationWPComPostLoginViewModel.ShowJetpackCPInstallationScreen
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JetpackActivationWPCom2FAFragment : BaseFragment() {
    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    private val viewModel: JetpackActivationWPCom2FAViewModel by viewModels()

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WooThemeWithBackground {
                    JetpackActivationWPCom2FAScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowJetpackActivationScreen -> navigateToJetpackActivationScreen(event)
                is ShowJetpackCPInstallationScreen -> navigateToJetpackCPInstallationScreen()
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is Exit -> findNavController().navigateUp()
            }
        }
    }

    private fun navigateToJetpackActivationScreen(event: ShowJetpackActivationScreen) {
        findNavController().navigateSafely(
            JetpackActivationWPCom2FAFragmentDirections
                .actionJetpackActivationWPCom2FAFragmentToJetpackActivationMainFragment(
                    isJetpackInstalled = event.isJetpackInstalled,
                    siteUrl = event.siteUrl
                )
        )
    }

    private fun navigateToJetpackCPInstallationScreen() {
        findNavController().navigateSafely(
            NavGraphJetpackInstallDirections.actionGlobalJetpackCPInstallProgressDialog(),
            navOptions = navOptions {
                popUpTo(R.id.jetpackActivationDispatcherFragment) { inclusive = true }
            }
        )
    }
}
