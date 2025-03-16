package com.cataloghub.android.ui.prefs.domain

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.navigateToHelpScreen
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.prefs.domain.DomainDashboardViewModel.NavigateToDomainSearch
import com.cataloghub.android.ui.prefs.domain.DomainDashboardViewModel.ShowMoreAboutDomains
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DomainDashboardFragment : BaseFragment() {
    private val viewModel: DomainDashboardViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    DomainDashboardScreen(viewModel)
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
                is MultiLiveEvent.Event.NavigateToHelpScreen -> navigateToHelpScreen(event.origin)
                is ShowMoreAboutDomains -> ChromeCustomTabUtils.launchUrl(requireContext(), event.url)
                is NavigateToDomainSearch -> findNavController().navigate(
                    DomainDashboardFragmentDirections.actionDomainDashboardFragmentToDomainSearchFragment(
                        isFreeCreditAvailable = event.hasFreeCredits,
                        freeDomainUrl = event.freeUrl
                    )
                )
            }
        }
    }
}
