package com.woocommerce.android.ui.woopos.emailreceipt

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.viewmodel.ResourceProvider
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
@Suppress("UnusedPrivateProperty")
class WooPosEmailReceiptViewModel @Inject constructor(
    private val repository: WooPosEmailReceiptRepository,
    resourceProvider: ResourceProvider,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val orderId = savedState.get<Long>(EMAIL_RECEIPT_ROUTE_ORDER_ID_KEY)!!

    private val _state = savedState.getStateFlow<WooPosEmailReceiptState>(
        scope = viewModelScope,
        initialValue = WooPosEmailReceiptState(
            email = "",
            errorMessage = null,
            button = WooPosEmailReceiptState.Button(
                text = resourceProvider.getString(R.string.woopos_email_receipt_send_button),
                status = WooPosEmailReceiptState.Button.Status.DISABLED
            )
        ),
        key = "woo_pos_email_receipt_state",
    )

    val state: StateFlow<WooPosEmailReceiptState> = _state

    fun onUIEvent(event: WooPosEmailReceiptUIEvent) {
        when (event) {
            WooPosEmailReceiptUIEvent.SendEmailClicked -> TODO()
            is WooPosEmailReceiptUIEvent.EmailChanged -> TODO()
        }
    }
}
