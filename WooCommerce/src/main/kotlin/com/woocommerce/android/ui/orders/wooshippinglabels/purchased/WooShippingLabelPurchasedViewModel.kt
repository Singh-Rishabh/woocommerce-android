package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPaperSize.LABEL
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.printing.FetchShippingLabelFile
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.io.File
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelPurchasedViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val fetchShippingLabelFile: FetchShippingLabelFile
) : ScopedViewModel(savedState) {
    private val navArgs by savedState.navArgs<WooShippingLabelPurchasedFragmentArgs>()

    private val _viewState = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = ViewState(
            paperSizeOption = LABEL
        )
    )
    val viewState = _viewState.asLiveData()

    fun onPrintShippingLabelClicked() {
        launch {
            val paperSize = _viewState.value.paperSizeOption
            fetchShippingLabelFile(
                labelIds = listOf(navArgs.labelId),
                paperSize = paperSize.name.lowercase(Locale.US)
            )?.let { triggerEvent(OpenShippingLabelFile(it)) }
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
        val paperSizeOption: WooShippingLabelPaperSize,
    ) : Parcelable

    @Parcelize
    data class ShippingLabelData(
        val totalWeight: String,
        val totalPrice: String,
        val dimensionUnit: String,
        val weightUnit: String,
        val items: List<ShippableItem>
    ) : Parcelable

    @Parcelize
    data class ShippableItem(
        val itemId: Long,
        val productId: Long,
        val title: String,
        val dimensions: String,
        val weight: String,
        val formattedPrice: String,
        val quantity: Float,
        val dimensionUnit: String,
        val weightUnit: String,
        val imageUrl: String? = null
    ) : Parcelable

    data class OpenShippingLabelFile(val file: File) : MultiLiveEvent.Event()
    object TrackShipmentRequested : MultiLiveEvent.Event()
    object SchedulePickUpRequested : MultiLiveEvent.Event()
    object StartRefundRequest : MultiLiveEvent.Event()
    object OpenLearnMoreScreen : MultiLiveEvent.Event()
}
