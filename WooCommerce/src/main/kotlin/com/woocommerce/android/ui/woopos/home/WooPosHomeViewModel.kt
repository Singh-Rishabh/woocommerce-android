package com.woocommerce.android.ui.woopos.home

import androidx.annotation.StringRes
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.home.WooPosHomeState.ExitConfirmationDialog
import com.woocommerce.android.ui.woopos.home.WooPosHomeState.ProductsInfoDialog
import com.woocommerce.android.ui.woopos.home.WooPosHomeState.ScreenPositionState
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooPosHomeViewModel @Inject constructor(
    private val childrenToParentEventReceiver: WooPosChildrenToParentEventReceiver,
    private val parentToChildrenEventSender: WooPosParentToChildrenEventSender,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _state = savedStateHandle.getStateFlow(
        scope = viewModelScope,
        key = "home_state",
        initialValue = WooPosHomeState(
            screenPositionState = ScreenPositionState.Cart.Visible,
            productsInfoDialog = ProductsInfoDialog(isVisible = false),
            exitConfirmationDialog = ExitConfirmationDialog(isVisible = false),
        )
    )
    val state: StateFlow<WooPosHomeState> = _state

    private val _toastEvent = MutableSharedFlow<Toast>()
    val toastEvent: SharedFlow<Toast> = _toastEvent

    data class Toast(
        @StringRes val message: Int,
    )

    init {
        listenBottomEvents()
    }

    fun onUIEvent(event: WooPosHomeUIEvent) {
        return when (event) {
            WooPosHomeUIEvent.SystemBackClicked -> {
                when (_state.value.screenPositionState) {
                    ScreenPositionState.Checkout.NotPaid -> {
                        _state.value = _state.value.copy(
                            screenPositionState = ScreenPositionState.Cart.Visible
                        )
                        sendEventToChildren(ParentToChildrenEvent.BackFromCheckoutToCartClicked)
                    }

                    ScreenPositionState.Checkout.Paid -> {
                        _state.value = _state.value.copy(
                            screenPositionState = ScreenPositionState.Cart.Visible
                        )
                    }

                    is ScreenPositionState.Cart -> {
                        _state.value = _state.value.copy(
                            exitConfirmationDialog = ExitConfirmationDialog(isVisible = true)
                        )
                    }
                }
            }

            WooPosHomeUIEvent.ExitConfirmationDialogDismissed -> {
                _state.value = _state.value.copy(
                    exitConfirmationDialog = ExitConfirmationDialog(isVisible = false)
                )
            }

            WooPosHomeUIEvent.DismissProductsInfoDialog -> {
                _state.value = _state.value.copy(
                    productsInfoDialog = ProductsInfoDialog(isVisible = false)
                )
            }

            WooPosHomeUIEvent.OnPaymentCompletedViaCash -> onOrderSuccessfullyPaid()
        }
    }

    private fun listenBottomEvents() {
        viewModelScope.launch {
            childrenToParentEventReceiver.events.collect { event ->
                when (event) {
                    is ChildToParentEvent.CheckoutClicked -> {
                        _state.value = _state.value.copy(
                            screenPositionState = ScreenPositionState.Checkout.NotPaid
                        )
                        sendEventToChildren(ParentToChildrenEvent.CheckoutClicked(event.itemClickedDataList))
                    }

                    is ChildToParentEvent.BackFromCheckoutToCartClicked -> {
                        _state.value = _state.value.copy(
                            screenPositionState = ScreenPositionState.Cart.Visible
                        )
                    }

                    is ChildToParentEvent.ItemClickedInProductSelector -> {
                        sendEventToChildren(
                            ParentToChildrenEvent.ItemClickedInProductSelector(event.itemData)
                        )
                    }

                    is ChildToParentEvent.NewTransactionClicked -> {
                        _state.value = _state.value.copy(
                            screenPositionState = ScreenPositionState.Cart.Visible
                        )
                    }

                    is ChildToParentEvent.OrderSuccessfullyPaid -> onOrderSuccessfullyPaid()

                    ChildToParentEvent.ExitPosClicked -> {
                        _state.value = _state.value.copy(
                            exitConfirmationDialog = ExitConfirmationDialog(isVisible = true)
                        )
                    }

                    is ChildToParentEvent.ProductsStatusChanged -> handleProductsStatusChanged(event)

                    ChildToParentEvent.ProductsDialogInfoIconClicked -> {
                        _state.value = _state.value.copy(
                            productsInfoDialog = ProductsInfoDialog(isVisible = true)
                        )
                    }

                    ChildToParentEvent.NoInternet -> {
                        viewModelScope.launch {
                            _toastEvent.emit(Toast(R.string.woopos_no_internet_message))
                        }
                    }
                }
            }
        }
    }

    private fun handleProductsStatusChanged(event: ChildToParentEvent.ProductsStatusChanged) {
        val screenPosition = _state.value.screenPositionState
        val newScreenPositionState = when (event) {
            ChildToParentEvent.ProductsStatusChanged.FullScreen -> {
                when (screenPosition) {
                    is ScreenPositionState.Cart -> ScreenPositionState.Cart.Hidden
                    is ScreenPositionState.Checkout -> screenPosition
                }
            }
            ChildToParentEvent.ProductsStatusChanged.WithCart -> {
                when (screenPosition) {
                    ScreenPositionState.Cart.Hidden ->
                        ScreenPositionState.Cart.Visible

                    ScreenPositionState.Cart.Visible,
                    ScreenPositionState.Checkout.NotPaid,
                    ScreenPositionState.Checkout.Paid -> screenPosition
                }
            }
        }
        _state.value = _state.value.copy(screenPositionState = newScreenPositionState)
    }

    private fun sendEventToChildren(event: ParentToChildrenEvent) {
        viewModelScope.launch {
            parentToChildrenEventSender.sendToChildren(event)
        }
    }

    private fun onOrderSuccessfullyPaid() {
        _state.value = _state.value.copy(
            screenPositionState = ScreenPositionState.Checkout.Paid
        )
        sendEventToChildren(ParentToChildrenEvent.OrderSuccessfullyPaid)
    }
}
