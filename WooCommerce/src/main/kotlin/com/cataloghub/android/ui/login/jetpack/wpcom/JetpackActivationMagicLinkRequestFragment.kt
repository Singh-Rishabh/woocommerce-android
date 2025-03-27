package com.cataloghub.android.ui.login.jetpack.wpcom

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.login.jetpack.wpcom.JetpackActivationMagicLinkRequestViewModel.OpenEmailClient
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.util.ActivityUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JetpackActivationMagicLinkRequestFragment : BaseFragment() {
    private val viewModel: JetpackActivationMagicLinkRequestViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WooThemeWithBackground {
                    JetpackActivationMagicLinkRequestScreen(viewModel = viewModel)
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
                is OpenEmailClient -> openEmailClient()
                is JetpackActivationMagicLinkRequestViewModel.ShowPasswordScreen -> openPasswordScreen(event)
                is JetpackActivationMagicLinkRequestViewModel.ShowUsernameScreen -> openUsernameScreen(event)
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is Exit -> findNavController().navigateUp()
            }
        }
    }

    private fun openEmailClient() {
        if (ActivityUtils.isEmailClientAvailable(requireContext())) {
            ActivityUtils.openEmailClient(requireContext())
        } else {
            uiMessageResolver.showSnack(R.string.login_email_client_not_found)
        }
    }

    private fun openPasswordScreen(event: JetpackActivationMagicLinkRequestViewModel.ShowPasswordScreen) {
        findNavController().navigate(
            JetpackActivationMagicLinkRequestFragmentDirections
                .actionJetpackActivationMagicLinkRequestFragmentToJetpackActivationWPComPasswordFragment(
                    emailOrUsername = event.emailOrUsername,
                    jetpackStatus = event.jetpackStatus
                )
        )
    }

    private fun openUsernameScreen(event: JetpackActivationMagicLinkRequestViewModel.ShowUsernameScreen) {
        findNavController().navigate(
            JetpackActivationMagicLinkRequestFragmentDirections
                .actionJetpackActivationMagicLinkRequestFragmentToJetpackActivationWPComEmailFragment(
                    usernameOnly = true,
                    jetpackStatus = event.jetpackStatus
                )
        )
    }
}
