package com.cataloghub.android.ui.blaze.creation.destination

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.blaze.BlazeRepository.DestinationParameters
import com.cataloghub.android.ui.blaze.creation.destination.BlazeCampaignCreationAdDestinationParametersFragment.Companion.BLAZE_DESTINATION_PARAMETERS_RESULT
import com.cataloghub.android.ui.blaze.creation.destination.BlazeCampaignCreationAdDestinationViewModel.NavigateToParametersScreen
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlazeCampaignCreationAdDestinationFragment : BaseFragment() {
    companion object {
        const val BLAZE_DESTINATION_RESULT = "blaze_destination_result"
    }
    private val viewModel: BlazeCampaignCreationAdDestinationViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return composeView {
            BlazeCampaignCreationAdDestinationScreen(viewModel)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleEvents()
        handleResults()
    }

    private fun handleEvents() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ExitWithResult<*> -> navigateBackWithResult(BLAZE_DESTINATION_RESULT, event.data)
                is NavigateToParametersScreen -> {
                    val action = BlazeCampaignCreationAdDestinationFragmentDirections
                        .actionAdDestinationFragmentToAdDestinationParametersFragment(event.destinationParameters)
                    findNavController().navigateSafely(action)
                }
            }
        }
    }

    private fun handleResults() {
        handleResult<DestinationParameters>(BLAZE_DESTINATION_PARAMETERS_RESULT) {
            viewModel.onDestinationParametersUpdated(it.targetUrl, it.parameters)
        }
    }
}
