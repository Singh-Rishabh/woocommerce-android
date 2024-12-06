package com.woocommerce.android.ui.woopos.cashpayment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import com.woocommerce.android.viewmodel.ResourceProvider
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
    private val resourceProvider: ResourceProvider,
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
                button = WooPosCashPaymentState.Collecting.Button(
                    text = resourceProvider.getString(R.string.woopos_complete_cash_order_button),
                    status = WooPosCashPaymentState.Collecting.Button.Status.ENABLED
                )
            )
        }
    }

    fun onUIEvent(event: WooPosCashPaymentUIEvent) {
        when (event) {
            is WooPosCashPaymentUIEvent.AmountChanged -> TODO()
            WooPosCashPaymentUIEvent.CompleteOrderClicked -> handleOrderCompletion()
        }
    }

    private fun handleOrderCompletion() {
        viewModelScope.launch {
            val stateBeforeCompleting = _state.value as? WooPosCashPaymentState.Collecting ?: return@launch
            _state.value = stateBeforeCompleting.copy(
                button = stateBeforeCompleting.button.copy(
                    status = WooPosCashPaymentState.Collecting.Button.Status.LOADING
                )
            )

            val result = repository.completeOrder(orderId)
            if (result.isSuccess) {
                _state.value = WooPosCashPaymentState.Complete
            } else {
                val currentState = _state.value as? WooPosCashPaymentState.Collecting ?: return@launch
                currentState
                _state.value = currentState.copy(
                    button = currentState.button.copy(
                        status = WooPosCashPaymentState.Collecting.Button.Status.ENABLED
                    )
                )
            }
        }
    }
}
