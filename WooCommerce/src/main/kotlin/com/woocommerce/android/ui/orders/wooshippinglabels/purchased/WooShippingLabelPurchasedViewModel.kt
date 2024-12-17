package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPaperSize.LETTER
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.printing.FetchShippingLabelFile
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.util.Locale
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import kotlinx.coroutines.flow.update
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
                }?.let { triggerEvent(OpenShippingLabelFile(it)) }
        }
    }

    fun onLabelPaperSizeOptionSelected(paperSize: WooShippingLabelPaperSize) {
        _viewState.update { it.copy(paperSizeOption = paperSize) }
    }

    fun onTrackShipmentClicked() { triggerEvent(TrackShipmentRequested) }

    fun onSchedulePickUpClicked() { triggerEvent(SchedulePickUpRequested) }

    fun onRefundClicked() { triggerEvent(StartRefundRequest) }

    fun onLearnMoreClicked() { triggerEvent(OpenLearnMoreScreen) }

    @Parcelize
    data class ViewState(
        val paperSizeOption: WooShippingLabelPaperSize? = null,
        val labelId: Long? = null
    ) : Parcelable

    data class OpenShippingLabelFile(val file: File) : MultiLiveEvent.Event()
    object TrackShipmentRequested : MultiLiveEvent.Event()
    object SchedulePickUpRequested : MultiLiveEvent.Event()
    object StartRefundRequest : MultiLiveEvent.Event()
    object OpenLearnMoreScreen : MultiLiveEvent.Event()


}
