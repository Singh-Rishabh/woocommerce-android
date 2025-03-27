package com.cataloghub.android.ui.moremenu.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MoreMenuCustomerListFragment : BaseFragment() {

    private val viewModel by viewModels<CustomerListDetailsViewModel>()

    override val activityAppBarStatus: AppBarStatus = AppBarStatus.Hidden

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            WooThemeWithBackground {
                MenuCustomerListScreen(viewModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.event.observe(
            viewLifecycleOwner
        ) { event ->
            when (event) {
                is CustomerSelected -> {
                    findNavController().navigateSafely(
                        MoreMenuCustomerListFragmentDirections.actionMenuCustomerListFragmentToCustomerDetailsFragment(
                            event.customer
                        )
                    )
                }

                is MultiLiveEvent.Event.Exit -> {
                    findNavController().navigateUp()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }
}
