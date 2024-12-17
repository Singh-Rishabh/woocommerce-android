package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelPurchasedViewModel @Inject constructor(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    fun onPrintShippingLabelClicked() {

    }

    fun onLabelPaperSizeOptionSelected(paperSize: WooShippingLabelPaperSize) {

    }

    fun onTrackShipmentClicked() {

    }

    fun onSchedulePickUpClicked() {

    }

    fun onRefundClicked() {

    }

    fun onLearnMoreClicked() {

    }
}
