package com.cataloghub.android.ui.orders.creation.taxes

import android.app.Dialog
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.util.ChromeCustomTabUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class TaxRatesInfoDialogFragment : DialogFragment() {
    @Inject lateinit var tracker: AnalyticsTrackerWrapper
    private val args: TaxRatesInfoDialogFragmentArgs by navArgs()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext()).setView(
            ComposeView(requireContext()).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                setContent {
                    WooThemeWithBackground {
                        TaxRateInfoDialog(args.dialogState, ::dismiss, ::goToTaxRatesSettings)
                    }
                }
            }
        )
            .setCancelable(true)
            .create()
    }

    private fun goToTaxRatesSettings() {
        args.dialogState.taxRatesSettingsUrl.let {
            ChromeCustomTabUtils.launchUrl(requireContext(), it)
            dismiss()
        }
        tracker.track(AnalyticsEvent.TAX_EDUCATIONAL_DIALOG_EDIT_IN_ADMIN_BUTTON_TAPPED)
    }
}
