package com.cataloghub.android.ui.blaze.creation.preview

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
import com.cataloghub.android.ui.blaze.BlazeRepository.Budget
import com.cataloghub.android.ui.blaze.BlazeRepository.DestinationParameters
import com.cataloghub.android.ui.blaze.creation.ad.BlazeCampaignCreationEditAdFragment
import com.cataloghub.android.ui.blaze.creation.ad.BlazeCampaignCreationEditAdViewModel.EditAdResult
import com.cataloghub.android.ui.blaze.creation.budget.BlazeCampaignBudgetFragment
import com.cataloghub.android.ui.blaze.creation.destination.BlazeCampaignCreationAdDestinationFragment
import com.cataloghub.android.ui.blaze.creation.objective.BlazeCampaignObjectiveFragment
import com.cataloghub.android.ui.blaze.creation.objective.BlazeCampaignObjectiveViewModel.ObjectiveResult
import com.cataloghub.android.ui.blaze.creation.preview.BlazeCampaignCreationPreviewViewModel.NavigateToAdDestinationScreen
import com.cataloghub.android.ui.blaze.creation.preview.BlazeCampaignCreationPreviewViewModel.NavigateToBudgetScreen
import com.cataloghub.android.ui.blaze.creation.preview.BlazeCampaignCreationPreviewViewModel.NavigateToEditAdScreen
import com.cataloghub.android.ui.blaze.creation.preview.BlazeCampaignCreationPreviewViewModel.NavigateToObjectiveSelectionScreen
import com.cataloghub.android.ui.blaze.creation.preview.BlazeCampaignCreationPreviewViewModel.NavigateToPaymentSummary
import com.cataloghub.android.ui.blaze.creation.preview.BlazeCampaignCreationPreviewViewModel.NavigateToTargetLocationSelectionScreen
import com.cataloghub.android.ui.blaze.creation.preview.BlazeCampaignCreationPreviewViewModel.NavigateToTargetSelectionScreen
import com.cataloghub.android.ui.blaze.creation.targets.BlazeCampaignTargetLocationSelectionFragment
import com.cataloghub.android.ui.blaze.creation.targets.BlazeCampaignTargetLocationSelectionViewModel.TargetLocationResult
import com.cataloghub.android.ui.blaze.creation.targets.BlazeCampaignTargetSelectionFragment
import com.cataloghub.android.ui.blaze.creation.targets.BlazeCampaignTargetSelectionViewModel.TargetSelectionResult
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BlazeCampaignCreationPreviewFragment : BaseFragment() {
    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    val viewModel: BlazeCampaignCreationPreviewViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return composeView {
            BlazeCampaignCreationPreviewScreen(viewModel = viewModel)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        handleResults()
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is Exit -> findNavController().popBackStack()

                is MultiLiveEvent.Event.NavigateToHelpScreen -> navigateToHelpScreen(event.origin)

                is NavigateToBudgetScreen -> findNavController().navigateSafely(
                    BlazeCampaignCreationPreviewFragmentDirections
                        .actionBlazeCampaignCreationPreviewFragmentToBlazeCampaignBudgetFragment(
                            budget = event.budget,
                            targetingParameters = event.targetingParameters
                        )
                )

                is NavigateToEditAdScreen -> findNavController().navigateSafely(
                    BlazeCampaignCreationPreviewFragmentDirections
                        .actionBlazeCampaignCreationPreviewFragmentToBlazeCampaignCreationEditAdFragment(
                            productId = event.productId,
                            tagline = event.tagLine,
                            description = event.description,
                            ctaText = event.ctaText,
                            adImage = event.campaignImage,
                            aiSuggestionsForAd = event.aiSuggestions.toTypedArray()
                        )
                )

                is NavigateToObjectiveSelectionScreen -> findNavController().navigateSafely(
                    BlazeCampaignCreationPreviewFragmentDirections
                        .actionBlazeCampaignCreationPreviewFragmentToBlazeCampaignObjectiveFragment(event.selectedId)
                )

                is NavigateToTargetSelectionScreen -> findNavController().navigateSafely(
                    BlazeCampaignCreationPreviewFragmentDirections
                        .actionBlazeCampaignCreationPreviewFragmentToBlazeCampaignTargetSelectionFragment(
                            event.targetType,
                            event.selectedIds.toTypedArray()
                        )
                )

                is NavigateToTargetLocationSelectionScreen -> findNavController().navigateSafely(
                    BlazeCampaignCreationPreviewFragmentDirections
                        .actionBlazeCampaignCreationPreviewFragmentToBlazeCampaignTargetLocationSelectionFragment(
                            event.locations.toTypedArray()
                        )
                )

                is NavigateToAdDestinationScreen -> findNavController().navigateSafely(
                    BlazeCampaignCreationPreviewFragmentDirections
                        .actionBlazeCampaignCreationPreviewFragmentToBlazeCampaignCreationAdDestinationFragment(
                            event.productId,
                            event.destinationParameters
                        )
                )

                is NavigateToPaymentSummary -> findNavController().navigateSafely(
                    BlazeCampaignCreationPreviewFragmentDirections
                        .actionBlazeCampaignCreationPreviewFragmentToBlazeCampaignPaymentSummaryFragment(
                            event.campaignDetails
                        )
                )
            }
        }
    }

    private fun handleResults() {
        handleResult<EditAdResult>(BlazeCampaignCreationEditAdFragment.EDIT_AD_RESULT) {
            viewModel.onAdUpdated(it)
        }
        handleResult<ObjectiveResult>(BlazeCampaignObjectiveFragment.BLAZE_OBJECTIVE_SELECTION_RESULT) {
            viewModel.onObjectiveUpdated(it.objectiveId)
        }
        handleResult<Budget>(BlazeCampaignBudgetFragment.EDIT_BUDGET_AND_DURATION_RESULT) {
            viewModel.onBudgetAndDurationUpdated(it)
        }
        handleResult<TargetSelectionResult>(BlazeCampaignTargetSelectionFragment.BLAZE_TARGET_SELECTION_RESULT) {
            viewModel.onTargetSelectionUpdated(it.targetType, it.selectedIds)
        }
        handleResult<TargetLocationResult>(BlazeCampaignTargetLocationSelectionFragment.BLAZE_TARGET_LOCATION_RESULT) {
            viewModel.onTargetLocationsUpdated(it.locations)
        }
        handleResult<DestinationParameters>(BlazeCampaignCreationAdDestinationFragment.BLAZE_DESTINATION_RESULT) {
            viewModel.onDestinationUpdated(it)
        }
    }
}
