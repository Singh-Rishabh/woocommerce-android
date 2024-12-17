package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelPurchasedViewModel @Inject constructor(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    private val _viewState = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = ViewState()
    )
    val viewState = _viewState.asLiveData()

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

    @Parcelize
    data class ViewState(
        val paperSizeOption: WooShippingLabelPaperSize? = null
    ) : Parcelable
}
