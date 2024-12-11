package com.woocommerce.android.ui.woopos.cashpayment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WooPosCashPaymentViewModel @Inject constructor(
    private val repository: WooPosCashPaymentRepository,
    private val priceFormat: WooPosFormatPrice,
    savedState: SavedStateHandle,
) : ViewModel() {
    private val orderId = savedState.get<Long>("orderId")!!

    private val _state = savedState.getStateFlow<WooPosCashPaymentState>(
        scope = viewModelScope,
        initialValue = WooPosCashPaymentState.Initiating,
        key = "woo_pos_cash_payment_state"
    )

    val state: StateFlow<WooPosCashPaymentState> = _state

    init {
        viewModelScope.launch {
            val order = repository.getOrderById(orderId)!!
            _state.value = WooPosCashPaymentState.Collecting(
                enteredAmount = "",
                changeDue = priceFormat(BigDecimal.ZERO),
                total = priceFormat(order.total),
                canBeOrderBeCompleted = false
            )
        }
    }

    fun onUIEvent(event: WooPosCashPaymentUIEvent) {
        when (event) {
            is WooPosCashPaymentUIEvent.AmountChanged -> TODO()
            WooPosCashPaymentUIEvent.CompleteOrderClicked -> TODO()
        }
    }
}
