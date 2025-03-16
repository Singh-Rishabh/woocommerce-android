package com.cataloghub.android.ui.prefs.domain

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
import com.cataloghub.android.ui.prefs.domain.DomainPurchaseViewModel.NavigateToSuccessScreen
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.fluxc.network.UserAgent
import javax.inject.Inject

@AndroidEntryPoint
class DomainPurchaseFragment : BaseFragment() {
    private val viewModel: DomainPurchaseViewModel by viewModels()

    @Inject internal lateinit var authenticator: WebViewAuthenticator

    @Inject internal lateinit var userAgent: UserAgent

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    DomainRegistrationCheckoutScreen(viewModel, authenticator, userAgent)
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
                is MultiLiveEvent.Event.Exit -> findNavController().popBackStack()
                is NavigateToSuccessScreen -> navigateToPurchaseSuccessScreen(event.domain)
            }
        }
    }

    private fun navigateToPurchaseSuccessScreen(domain: String) {
        findNavController().navigateSafely(
            DomainPurchaseFragmentDirections
                .actionDomainRegistrationCheckoutFragmentToPurchaseSuccessfulFragment(domain)
        )
    }
}
