package com.cataloghub.android.ui.blaze.creation.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BlazeCampaignPaymentMethodsListFragment : BaseFragment() {
    companion object {
        const val SELECTED_PAYMENT_METHOD_KEY = "selectedPaymentMethodId"
    }

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    private val viewModel: BlazeCampaignPaymentMethodsListViewModel by viewModels()

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return composeView {
            WooThemeWithBackground {
                BlazeCampaignPaymentMethodsListScreen(viewModel)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleResults()
    }

    private fun handleResults() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                MultiLiveEvent.Event.Exit -> findNavController().navigateUp()
                is MultiLiveEvent.Event.ExitWithResult<*> -> navigateBackWithResult(
                    key = SELECTED_PAYMENT_METHOD_KEY,
                    result = event.data
                )
                is MultiLiveEvent.Event.ShowSnackbar -> uiMessageResolver.showSnack(event.message)
            }
        }
    }
}
