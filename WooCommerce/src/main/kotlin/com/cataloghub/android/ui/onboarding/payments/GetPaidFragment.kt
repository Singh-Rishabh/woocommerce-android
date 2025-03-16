package com.cataloghub.android.ui.onboarding.payments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.common.webview.WebViewAuthenticator
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.onboarding.payments.GetPaidViewModel.ShowWooPaymentsSetupSuccess
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.fluxc.network.UserAgent
import javax.inject.Inject

@AndroidEntryPoint
class GetPaidFragment : BaseFragment() {
    private val viewModel: GetPaidViewModel by viewModels()

    @Inject
    lateinit var userAgent: UserAgent

    @Inject
    lateinit var authenticator: WebViewAuthenticator

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    GetPaidScreen(viewModel, userAgent, authenticator)
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
                is ShowWooPaymentsSetupSuccess -> findNavController().navigateSafely(
                    GetPaidFragmentDirections.actionGetPaidFragmentToWooPaymentsSetupCelebrationDialog()
                )

                is Exit -> findNavController().popBackStack()
            }
        }
    }
}
