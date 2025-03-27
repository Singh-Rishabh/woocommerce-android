package com.cataloghub.android.ui.login.accountmismatch

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.navigateBackWithNotice
import com.cataloghub.android.extensions.navigateToHelpScreen
import com.cataloghub.android.support.help.HelpActivity
import com.cataloghub.android.support.help.HelpOrigin
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.login.LoginActivity
import com.cataloghub.android.ui.login.LoginEmailHelpDialogFragment
import com.cataloghub.android.ui.login.LoginEmailHelpDialogFragment.Listener
import com.cataloghub.android.ui.login.accountmismatch.AccountMismatchErrorViewModel.NavigateToEmailHelpDialogEvent
import com.cataloghub.android.ui.login.accountmismatch.AccountMismatchErrorViewModel.NavigateToLoginScreen
import com.cataloghub.android.ui.login.accountmismatch.AccountMismatchErrorViewModel.OnJetpackConnectedEvent
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.NavigateToHelpScreen
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialog
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowUiStringSnackbar
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.login.LoginMode
import javax.inject.Inject

@AndroidEntryPoint
class AccountMismatchErrorFragment : BaseFragment(), Listener {
    companion object {
        const val JETPACK_CONNECTED_NOTICE = "jetpack-connected"
    }

    private val viewModel: AccountMismatchErrorViewModel by viewModels()

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WooThemeWithBackground {
                    AccountMismatchErrorScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NavigateToHelpScreen -> navigateToHelpScreen(event.origin)
                is NavigateToEmailHelpDialogEvent -> {
                    LoginEmailHelpDialogFragment.newInstance(this).also {
                        it.show(parentFragmentManager, LoginEmailHelpDialogFragment.TAG)
                    }
                }
                is NavigateToLoginScreen -> navigateToLoginScreen()
                is OnJetpackConnectedEvent -> onJetpackConnected(event)
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ShowUiStringSnackbar -> uiMessageResolver.showSnack(event.message)
                is ShowDialog -> event.showDialog()
                is Exit -> navigateBack()
            }
        }
    }

    private fun navigateBack() {
        if (requireActivity() is LoginActivity) {
            parentFragmentManager.popBackStack()
        } else {
            findNavController().navigateUp()
        }
    }

    private fun onJetpackConnected(event: OnJetpackConnectedEvent) {
        if (!event.isAuthenticated) {
            // Make sure this fragment is removed from the backstack
            val intent = Intent(requireActivity(), LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                action = LoginActivity.LOGIN_WITH_WPCOM_EMAIL_ACTION
                putExtra(LoginActivity.EMAIL_PARAMETER, event.email)
                LoginMode.WOO_LOGIN_MODE.putInto(this)
            }
            startActivity(intent)
        } else {
            navigateBackWithNotice(JETPACK_CONNECTED_NOTICE)
        }
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            LoginMode.WOO_LOGIN_MODE.putInto(this)
        }
        startActivity(intent)
    }

    override fun onEmailNeedMoreHelpClicked() {
        startActivity(
            HelpActivity.createIntent(
                context = requireContext(),
                origin = HelpOrigin.LOGIN_CONNECTED_EMAIL_HELP,
                extraSupportTags = null
            )
        )
    }
}
