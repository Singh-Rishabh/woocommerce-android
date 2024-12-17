package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPaperSize.LETTER
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.printing.FetchShippingLabelFile
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import kotlinx.coroutines.launch

@HiltViewModel
class WooShippingLabelPurchasedViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val fetchShippingLabelFile: FetchShippingLabelFile
) : ScopedViewModel(savedState) {
    private val _viewState = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = ViewState()
    )
    val viewState = _viewState.asLiveData()

    fun onPrintShippingLabelClicked() {
        launch {
            _viewState.value
                .takeIf { it.labelId != null }
                ?.let { Pair(it.labelId ?: 0L, it.paperSizeOption ?: LETTER) }
                ?.let { (labelId, paperSize) ->
                    fetchShippingLabelFile(
                        labelIds = listOf(labelId),
                        paperSize = paperSize.name.lowercase(Locale.US)
                    )
                }
        }
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
        val paperSizeOption: WooShippingLabelPaperSize? = null,
        val labelId: Long? = null
    ) : Parcelable
}
