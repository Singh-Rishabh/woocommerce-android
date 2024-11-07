package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.base.BaseFragment
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WooShippingLabelPurchasedFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    Surface {
                        val selectedLabelPaperSizeOption = remember { mutableStateOf(WooShippingLabelPaperSize.LEGAL) }
                        WooShippingLabelPurchasedScreen(
                            selectedLabelPaperSizeOption = selectedLabelPaperSizeOption.value,
                            onLabelPaperSizeOptionSelected = { selectedLabelPaperSizeOption.value = it },
                            onPrintShippingLabelClicked = { },
                            modifier = Modifier.padding(16.dp),
                            onTrackShipmentClicked = { },
                            onSchedulePickUpClicked = { },
                            onRefundClicked = { },
                            onLearnMoreClicked = { }
                        )
                    }
                }
            }
        }
    }
}
