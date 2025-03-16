package com.cataloghub.android.ui.payments.cardreader.update

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.fragment.app.viewModels
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.CardReaderUpdateDialogBinding
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.model.UiString
import com.cataloghub.android.ui.payments.PaymentsBaseDialogFragment
import com.cataloghub.android.ui.payments.cardreader.update.CardReaderUpdateViewModel.CardReaderUpdateEvent.SoftwareUpdateAboutToStart
import com.cataloghub.android.ui.payments.cardreader.update.CardReaderUpdateViewModel.CardReaderUpdateEvent.SoftwareUpdateProgress
import com.cataloghub.android.ui.payments.cardreader.update.CardReaderUpdateViewModel.UpdateResult
import com.cataloghub.android.util.UiHelpers
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardReaderUpdateDialogFragment : PaymentsBaseDialogFragment(R.layout.card_reader_update_dialog) {
    val viewModel: CardReaderUpdateViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        requireDialog().setCanceledOnTouchOutside(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = ComponentDialog(requireContext(), theme)
        dialog.onBackPressedDispatcher.addCallback(dialog) {
            viewModel.onBackPressed()
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = CardReaderUpdateDialogBinding.bind(view)

        initObservers(binding)
    }

    private fun initObservers(binding: CardReaderUpdateDialogBinding) {
        viewModel.event.observe(
            viewLifecycleOwner
        ) { event ->
            when (event) {
                is ExitWithResult<*> -> navigateBackWithResult(
                    KEY_READER_UPDATE_RESULT,
                    event.data as UpdateResult
                )

                is SoftwareUpdateProgress ->
                    announceSoftwareUpdateProgress(event.progress, binding)

                is SoftwareUpdateAboutToStart ->
                    binding.root.announceForAccessibility(getString(event.accessibilityText))

                else -> event.isHandled = false
            }
        }

        viewModel.viewStateData.observe(
            viewLifecycleOwner
        ) { state ->
            with(binding) {
                UiHelpers.setTextOrHide(titleTextView, state.title)
                UiHelpers.setTextOrHide(descriptionTextView, state.description)
                UiHelpers.setTextOrHide(progressTextView, state.progressText)
                UiHelpers.setTextOrHide(actionButton, state.button?.text)
                with(progressCircleProgressOverlayView) {
                    UiHelpers.updateVisibility(this, state.progress != null)
                    currentProgressPercentage = state.progress ?: 0
                }
                UiHelpers.setImageOrHideInLandscapeOnCompactScreenHeightSizeClass(progressImageView, state.illustration)
                actionButton.setOnClickListener { state.button?.onActionClicked?.invoke() }
            }
        }
    }

    private fun announceSoftwareUpdateProgress(
        progressText: UiString,
        binding: CardReaderUpdateDialogBinding
    ) {
        val progress = UiHelpers.getTextOfUiString(requireActivity(), progressText)
        binding.progressTextView.announceForAccessibility(progress)
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    companion object {
        const val KEY_READER_UPDATE_RESULT = "key_reader_update_result"
    }
}
