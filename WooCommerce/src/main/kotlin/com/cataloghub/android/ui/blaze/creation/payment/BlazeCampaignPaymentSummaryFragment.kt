package com.cataloghub.android.ui.blaze.creation.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.extensions.navigateToHelpScreen
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.blaze.creation.payment.BlazeCampaignPaymentSummaryViewModel.NavigateToStartingScreenWithSuccessBottomSheet
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlazeCampaignPaymentSummaryFragment : BaseFragment() {
    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    private val viewModel: BlazeCampaignPaymentSummaryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return composeView {
            WooThemeWithBackground {
                BlazeCampaignPaymentSummaryScreen(viewModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleEvents()
        handleResults()
    }

    private fun handleEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                MultiLiveEvent.Event.Exit -> findNavController().navigateUp()
                is MultiLiveEvent.Event.NavigateToHelpScreen -> navigateToHelpScreen(event.origin)
                is BlazeCampaignPaymentSummaryViewModel.NavigateToPaymentsListScreen -> {
                    findNavController().navigateSafely(
                        BlazeCampaignPaymentSummaryFragmentDirections
                            .actionBlazeCampaignPaymentSummaryFragmentToBlazeCampaignPaymentMethodsListFragment(
                                paymentMethodsData = event.paymentMethodsData,
                                selectedPaymentMethodId = event.selectedPaymentMethodId
                            )
                    )
                }

                is NavigateToStartingScreenWithSuccessBottomSheet -> navigateBackToStartingScreen()
            }
        }
    }

    private fun handleResults() {
        handleResult<String>(BlazeCampaignPaymentMethodsListFragment.SELECTED_PAYMENT_METHOD_KEY) {
            viewModel.onPaymentMethodSelected(it)
        }
    }

    private fun navigateBackToStartingScreen() {
        findNavController().navigateSafely(
            BlazeCampaignPaymentSummaryFragmentDirections
                .actionBlazeCampaignPaymentSummaryFragmentToBlazeCampaignSuccessBottomSheetFragment()
        )
    }
}
