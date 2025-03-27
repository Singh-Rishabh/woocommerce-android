package com.cataloghub.android.ui.payments.cardreader.payment

import android.app.Dialog
import android.content.ContentResolver
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings.ACTION_NFC_SETTINGS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.cataloghub.android.NavGraphPaymentFlowDirections
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.CardReaderPaymentDialogBinding
import com.cataloghub.android.extensions.navigateBackWithNotice
import com.cataloghub.android.support.help.HelpOrigin
import com.cataloghub.android.support.requests.SupportRequestFormActivity
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.payments.PaymentsBaseDialogFragment
import com.cataloghub.android.ui.payments.cardreader.payment.ViewState.BuiltInReaderPaymentSuccessfulReceiptSentAutomaticallyState
import com.cataloghub.android.ui.payments.cardreader.payment.ViewState.BuiltInReaderPaymentSuccessfulState
import com.cataloghub.android.ui.payments.cardreader.payment.ViewState.ExternalReaderPaymentSuccessfulReceiptSentAutomaticallyState
import com.cataloghub.android.ui.payments.cardreader.payment.ViewState.ExternalReaderPaymentSuccessfulState
import com.cataloghub.android.ui.payments.refunds.RefundSummaryFragment.Companion.KEY_INTERAC_SUCCESS
import com.cataloghub.android.util.PrintHtmlHelper
import com.cataloghub.android.util.UiHelpers
import com.cataloghub.android.util.UiHelpers.getIllustrationVisibilityForFontScale
import com.cataloghub.android.util.UiHelpers.getTextOfUiString
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CardReaderPaymentDialogFragment : PaymentsBaseDialogFragment(R.layout.card_reader_payment_dialog) {
    val viewModel: CardReaderPaymentViewModel by viewModels()

    @Inject
    lateinit var printHtmlHelper: PrintHtmlHelper

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.setCanceledOnTouchOutside(false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel.onViewCreated()
        val dialog = ComponentDialog(requireContext(), theme)
        dialog.onBackPressedDispatcher.addCallback(dialog) {
            viewModel.onBackPressed()
        }
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val binding = CardReaderPaymentDialogBinding.bind(view)
        initObservers(binding)
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.start()
    }

    @Suppress("LongMethod")
    private fun initObservers(binding: CardReaderPaymentDialogBinding) {
        viewModel.event.observe(
            viewLifecycleOwner
        ) { event ->
            when (event) {
                is PrintReceipt -> printHtmlHelper.printReceipt(
                    requireActivity(),
                    event.receiptUrl,
                    event.documentName
                )
                InteracRefundSuccessful -> navigateBackWithNotice(KEY_INTERAC_SUCCESS)
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ShowSnackbarInDialog -> Snackbar.make(
                    requireView(),
                    event.message,
                    BaseTransientBottomBar.LENGTH_LONG
                ).show()
                is PlayChaChing -> playChaChing()
                is ContactSupport -> openSupportRequestScreen()
                is EnableNfc -> openEnableNfcScreen()
                is PurchaseCardReader -> openPurchaseCardReaderScreen(event.url)
                else -> event.isHandled = false
            }
        }
        viewModel.viewStateData.observe(
            viewLifecycleOwner
        ) { viewState ->
            announceForAccessibility(binding, viewState)
            UiHelpers.setTextOrHide(binding.headerLabel, viewState.headerLabel)
            UiHelpers.setTextOrHide(binding.amountLabel, viewState.amountWithCurrencyLabel)
            UiHelpers.setImageOrHideInLandscapeOnCompactScreenHeightSizeClass(
                binding.illustration,
                viewState.illustration
            ).also {
                if (binding.illustration.isVisible) {
                    binding.illustration.visibility =
                        getIllustrationVisibilityForFontScale(resources.configuration.fontScale)
                }
            }
            UiHelpers.setTextOrHide(binding.paymentStateLabel, viewState.paymentStateLabel)
            UiHelpers.setTextOrHide(binding.hintLabel, viewState.hintLabel)
            UiHelpers.setTextOrHide(binding.primaryActionBtn, viewState.primaryActionLabel)
            UiHelpers.setTextOrHide(binding.secondaryActionBtn, viewState.secondaryActionLabel)
            UiHelpers.setTextOrHide(binding.tertiaryActionBtn, viewState.tertiaryActionLabel)
            UiHelpers.setTextOrHide(binding.receiptSentLabel, viewState.receiptSentAutomaticallyHint)
            UiHelpers.updateVisibility(binding.progressBarWrapper, viewState.isProgressVisible)
            binding.primaryActionBtn.setOnClickListener {
                viewState.onPrimaryActionClicked?.invoke()
            }
            binding.secondaryActionBtn.setOnClickListener {
                viewState.onSecondaryActionClicked?.invoke()
            }
            binding.tertiaryActionBtn.setOnClickListener {
                viewState.onTertiaryActionClicked?.invoke()
            }
        }

        viewModel.event.observe(
            viewLifecycleOwner
        ) { event ->
            when (event) {
                Exit -> {
                    navigateBackWithNotice(KEY_CARD_PAYMENT_RESULT)
                }
            }
        }
    }

    private fun openPurchaseCardReaderScreen(url: String) {
        findNavController().navigate(
            NavGraphPaymentFlowDirections.actionGlobalAuthenticatedWebViewFragment(urlToLoad = url)
        )
    }

    private fun openSupportRequestScreen() {
        SupportRequestFormActivity.createIntent(
            context = requireContext(),
            origin = HelpOrigin.CARD_READER_PAYMENT_ERROR,
            extraTags = ArrayList()
        ).let { activity?.startActivity(it) }
    }

    private fun openEnableNfcScreen() {
        startActivity(Intent(ACTION_NFC_SETTINGS))
    }

    private fun announceForAccessibility(binding: CardReaderPaymentDialogBinding, viewState: ViewState) {
        with(binding) {
            val isPaymentSuccessful = viewState is BuiltInReaderPaymentSuccessfulState ||
                viewState is ExternalReaderPaymentSuccessfulState
            val isPaymentSuccessfulReceiptSentAutomatically =
                viewState is BuiltInReaderPaymentSuccessfulReceiptSentAutomaticallyState ||
                    viewState is ExternalReaderPaymentSuccessfulReceiptSentAutomaticallyState
            if (isPaymentSuccessful || isPaymentSuccessfulReceiptSentAutomatically) {
                viewState.headerLabel?.let {
                    headerLabel.announceForAccessibility(getString(it) + viewState.amountWithCurrencyLabel)
                }
            } else {
                viewState.paymentStateLabel?.let {
                    paymentStateLabel.announceForAccessibility(getTextOfUiString(requireContext(), it))
                }
            }
            viewState.hintLabel?.let {
                hintLabel.announceForAccessibility(getString(it))
            }
        }
    }

    private fun playChaChing() {
        val chaChingUri =
            Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE +
                    "://" + requireActivity().packageName + "/" + R.raw.cha_ching
            )
        val mp = MediaPlayer.create(requireActivity(), chaChingUri)
        mp.start()
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
        printHtmlHelper.getAndClearPrintJobResult()?.let {
            viewModel.onPrintResult(it)
        }
        disableDigitalWallets()
    }

    override fun onPause() {
        super.onPause()
        reEnableDigitalWallets()
    }

    /**
     * Disables digital wallets (eg. Google Pay) in order to prevent the merchant from accidentally charging themselves
     * instead of the customer.
     */
    private fun disableDigitalWallets() {
        NfcAdapter.getDefaultAdapter(requireContext())
            ?.enableReaderMode(
                requireActivity(),
                { },
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null
            )
    }

    private fun reEnableDigitalWallets() {
        NfcAdapter.getDefaultAdapter(requireContext())
            ?.disableReaderMode(requireActivity())
    }

    companion object {
        const val KEY_CARD_PAYMENT_RESULT = "key_card_payment_result"
    }
}
