package com.cataloghub.android.ui.payments.cardreader.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.CardReaderWelcomeDialogBinding
import com.cataloghub.android.ui.payments.PaymentsBaseDialogFragment
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderWelcomeViewModel.CardReaderWelcomeDialogEvent.NavigateToOnboardingFlow
import com.cataloghub.android.util.UiHelpers
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CardReaderWelcomeDialogFragment : PaymentsBaseDialogFragment(R.layout.card_reader_welcome_dialog) {
    val viewModel: CardReaderWelcomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = CardReaderWelcomeDialogBinding.bind(view)
        initObservers(binding)
    }

    private fun initObservers(binding: CardReaderWelcomeDialogBinding) {
        viewModel.viewState.observe(viewLifecycleOwner) { viewState ->
            UiHelpers.setImageOrHideInLandscapeOnCompactScreenHeightSizeClass(binding.illustration, viewState.img)
            UiHelpers.setTextOrHide(binding.headerLabel, viewState.header)
            UiHelpers.setTextOrHide(binding.text, viewState.text)
            UiHelpers.setTextOrHide(binding.actionBtn, viewState.buttonLabel)
            binding.actionBtn.setOnClickListener {
                viewState.buttonAction.invoke()
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NavigateToOnboardingFlow -> {
                    findNavController()
                        .navigate(
                            CardReaderWelcomeDialogFragmentDirections
                                .actionCardReaderWelcomeDialogFragmentToCardReaderConnectDialogFragment(
                                    event.cardReaderFlowParam,
                                    event.cardReaderType
                                )
                        )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }
}
