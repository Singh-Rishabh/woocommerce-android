package com.woocommerce.android.ui.woopos.home.totals

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.model.Order
import com.woocommerce.android.ui.woopos.cardreader.WooPosCardReaderFacade
import com.woocommerce.android.ui.woopos.cardreader.WooPosCardReaderPaymentStatus
import com.woocommerce.android.ui.woopos.featureflags.WooPosIsCashPaymentsEnabled
import com.woocommerce.android.ui.woopos.featureflags.WooPosIsReceiptsEnabled
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.ParentToChildrenEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.WooPosParentToChildrenEventReceiver
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsViewModel
import com.woocommerce.android.ui.woopos.home.totals.payment.receipt.WooPosTotalsPaymentReceiptIsSendingSupported
import com.woocommerce.android.ui.woopos.home.totals.payment.receipt.WooPosTotalsPaymentReceiptIsSendingSupported.Companion.WC_VERSION_SUPPORTS_SENDING_RECEIPTS_BY_EMAIL
import com.woocommerce.android.ui.woopos.home.totals.payment.receipt.WooPosTotalsPaymentReceiptRepository
import com.woocommerce.android.ui.woopos.util.WooPosNetworkStatus
import com.woocommerce.android.ui.woopos.util.analytics.WooPosAnalyticsEvent
import com.woocommerce.android.ui.woopos.util.analytics.WooPosAnalyticsTracker
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import com.woocommerce.android.util.WooLog
import com.woocommerce.android.util.WooLog.T
import com.woocommerce.android.viewmodel.ResourceProvider
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class WooPosTotalsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver,
    private val childrenToParentEventSender: WooPosChildrenToParentEventSender,
    private val cardReaderFacade: WooPosCardReaderFacade,
    private val totalsRepository: WooPosTotalsRepository,
    private val receiptRepository: WooPosTotalsPaymentReceiptRepository,
    private val priceFormat: WooPosFormatPrice,
    private val analyticsTracker: WooPosAnalyticsTracker,
    private val networkStatus: WooPosNetworkStatus,
    private val isReceiptSendingSupported: WooPosTotalsPaymentReceiptIsSendingSupported,
    private val isReceiptsEnabled: WooPosIsReceiptsEnabled,
    private val isCashPaymentsEnabled: WooPosIsCashPaymentsEnabled,
    savedState: SavedStateHandle,
) : ViewModel() {

    private companion object {
        private const val EMPTY_ORDER_ID = -1L
        private const val KEY_STATE = "woo_pos_totals_data_state"
        private val InitialState = WooPosTotalsViewState.Loading
    }

    private val isReceiptSendingSupportedValue: Deferred<Boolean> by lazy {
        viewModelScope.async((Dispatchers.IO)) { isReceiptSendingSupported() }
    }

    private val uiState = savedState.getStateFlow<WooPosTotalsViewState>(
        scope = viewModelScope,
        initialValue = InitialState,
        key = "woo_pos_totals_view_state"
    )

    val state: StateFlow<WooPosTotalsViewState> = uiState

    private var dataState: MutableStateFlow<TotalsDataState> = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = TotalsDataState(),
        key = KEY_STATE,
    )

    init {
        listenUpEvents()
        listenToPaymentsStatus()

        initIsReceiptSendingSupportedValue()
    }

    fun onUIEvent(event: WooPosTotalsUIEvent) {
        when (event) {
            is WooPosTotalsUIEvent.CollectPaymentClicked -> collectPayment()
            is WooPosTotalsUIEvent.OnNewTransactionClicked -> {
                viewModelScope.launch {
                    childrenToParentEventSender.sendToParent(
                        ChildToParentEvent.NewTransactionClicked
                    )
                }
            }
            is WooPosTotalsUIEvent.RetryOrderCreationClicked -> {
                createOrderDraft(dataState.value.itemClickedDataList)
            }
            WooPosTotalsUIEvent.OnSendReceiptClicked -> sendReceiptByEmail()
            WooPosTotalsUIEvent.OnStartReceiptFlowClicked -> {
                viewModelScope.launch {
                    if (isReceiptSendingSupportedValue.await()) {
                        uiState.value = WooPosTotalsViewState.ReceiptSending(email = "")
                    } else {
                        childrenToParentEventSender.sendToParent(
                            ChildToParentEvent.ToastMessageDisplayed(
                                message = resourceProvider.getString(
                                    R.string.woopos_receipt_sending_not_supported,
                                    WC_VERSION_SUPPORTS_SENDING_RECEIPTS_BY_EMAIL,
                                )
                            )
                        )
                    }
                }
            }
            WooPosTotalsUIEvent.OnTakeCashPaymentClicked -> {
                viewModelScope.launch {
                    uiState.value = WooPosTotalsViewState.CashPayment(
                        enteredAmount = "",
                        changeDue = priceFormat(BigDecimal.ZERO),
                        total = priceFormat(dataState.value.orderTotal!!),
                        canBeOrderBeCompleted = false
                    )
                }
            }
            is WooPosTotalsUIEvent.OnEmailChanged -> {
                uiState.value = WooPosTotalsViewState.ReceiptSending(email = event.email)
            }
        }
    }

    private fun collectPayment() {
        if (!networkStatus.isConnected()) {
            viewModelScope.launch {
                childrenToParentEventSender.sendToParent(
                    ChildToParentEvent.ToastMessageDisplayed(
                        message = resourceProvider.getString(R.string.woopos_no_internet_message)
                    )
                )
            }
        } else {
            val orderId = dataState.value.orderId
            check(orderId != EMPTY_ORDER_ID)
            cardReaderFacade.collectPayment(orderId)
        }
    }

    private fun sendReceiptByEmail() {
        val viewState = uiState.value as WooPosTotalsViewState.ReceiptSending
        val email = viewState.email
        val orderId = dataState.value.orderId
        check(orderId != EMPTY_ORDER_ID)
        viewModelScope.launch {
            receiptRepository.sendReceiptByEmail(orderId, email)
        }
    }

    private fun listenUpEvents() {
        viewModelScope.launch {
            parentToChildrenEventReceiver.events.collect { event ->
                when (event) {
                    is ParentToChildrenEvent.CheckoutClicked -> {
                        dataState.value = dataState.value.copy(itemClickedDataList = event.itemClickedDataList)
                        createOrderDraft(dataState.value.itemClickedDataList)
                    }

                    is ParentToChildrenEvent.BackFromCheckoutToCartClicked -> {
                        uiState.value = InitialState
                    }

                    is ParentToChildrenEvent.ItemClickedInProductSelector,
                    ParentToChildrenEvent.OrderSuccessfullyPaid -> Unit
                }
            }
        }
    }

    private fun listenToPaymentsStatus() {
        viewModelScope.launch {
            cardReaderFacade.paymentStatus.collect { status ->
                when (status) {
                    is WooPosCardReaderPaymentStatus.Success -> {
                        val state = uiState.value
                        check(state is WooPosTotalsViewState.Totals)
                        val orderTotalText = resourceProvider.getString(
                            R.string.woopos_success_screen_total,
                            state.orderTotalText
                        )
                        uiState.value = WooPosTotalsViewState.PaymentSuccess(
                            orderTotalText = orderTotalText,
                            isReceiptAvailable = isReceiptsEnabled()
                        )
                        childrenToParentEventSender.sendToParent(ChildToParentEvent.OrderSuccessfullyPaid)
                    }
                    is WooPosCardReaderPaymentStatus.Failure,
                    is WooPosCardReaderPaymentStatus.Unknown -> Unit
                }
            }
        }
    }

    private fun createOrderDraft(itemClickedDataList: List<WooPosItemsViewModel.ItemClickedData>) {
        viewModelScope.launch {
            uiState.value = WooPosTotalsViewState.Loading

            totalsRepository.createOrderWithProducts(itemClickedDataList = itemClickedDataList)
                .fold(
                    onSuccess = { order ->
                        dataState.value = dataState.value.copy(
                            orderId = order.id,
                            orderTotal = order.total
                        )
                        uiState.value = buildWooPosTotalsViewState(order)
                        analyticsTracker.track(WooPosAnalyticsEvent.Event.OrderCreationSuccess)
                    },
                    onFailure = { error ->
                        WooLog.e(T.POS, "Order creation failed - $error")
                        uiState.value = WooPosTotalsViewState.Error(
                            resourceProvider.getString(R.string.woopos_totals_order_creation_error)
                        )
                        analyticsTracker.track(
                            WooPosAnalyticsEvent.Error.OrderCreationError(
                                errorContext = WooPosTotalsViewModel::class,
                                errorType = error::class.simpleName,
                                errorDescription = error.message
                            )
                        )
                    }
                )
        }
    }

    private suspend fun buildWooPosTotalsViewState(order: Order): WooPosTotalsViewState.Totals {
        val subtotalAmount = order.productsTotal
        val taxAmount = order.totalTax
        val totalAmount = order.total

        return WooPosTotalsViewState.Totals(
            orderSubtotalText = priceFormat(subtotalAmount),
            orderTaxText = priceFormat(taxAmount),
            orderTotalText = priceFormat(totalAmount),
            isCashPaymentAvailable = isCashPaymentsEnabled()
        )
    }

    private fun initIsReceiptSendingSupportedValue() {
        viewModelScope.launch {
            isReceiptSendingSupportedValue.await()
        }
    }

    @Parcelize
    private data class TotalsDataState(
        val orderId: Long = EMPTY_ORDER_ID,
        val orderTotal: BigDecimal? = null,
        val itemClickedDataList: List<WooPosItemsViewModel.ItemClickedData> = emptyList()
    ) : Parcelable
}
