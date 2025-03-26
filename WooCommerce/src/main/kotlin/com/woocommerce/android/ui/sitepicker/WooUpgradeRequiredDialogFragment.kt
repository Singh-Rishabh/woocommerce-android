package com.woocommerce.android.ui.sitepicker

import android.app.Dialog
import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.woocommerce.android.AppUrls
import com.woocommerce.android.ui.compose.theme.WooTheme
import com.woocommerce.android.util.ChromeCustomTabUtils

class WooUpgradeRequiredDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "WooUpgradeRequiredDialog"
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val composeView = ComposeView(requireContext()).apply {
            setContent {
                WooTheme {
                    WooUpgradeRequiredDialog(
                        onUpdateInstructions = {
                            ChromeCustomTabUtils.launchUrl(
                                requireContext(),
                                AppUrls.WOOCOMMERCE_UPGRADE
                            )
                        },
                        onDismiss = {
                            dismiss()
                        }
                    )
                }
            }
        }

        return Dialog(requireContext()).apply {
            setContentView(composeView)
            setCancelable(false)
        }
    }
}
