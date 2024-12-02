package com.woocommerce.android.ui.woopos.home.totals

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.cardreader.connection.CardReaderStatus
import com.woocommerce.android.cardreader.connection.CardReaderStatus.Connected
import com.woocommerce.android.model.Order
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderFlowParam.PaymentOrRefund
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentController
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentControllerFactory
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState
import com.woocommerce.android.ui.woopos.cardreader.WooPosCardReaderFacade
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.ParentToChildrenEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.WooPosParentToChildrenEventReceiver
import com.woocommerce.android.ui.woopos.util.WooPosNetworkStatus
import com.woocommerce.android.ui.woopos.util.analytics.WooPosAnalyticsEvent
import com.woocommerce.android.ui.woopos.util.analytics.WooPosAnalyticsTracker
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import com.woocommerce.android.util.WooLog
import com.woocommerce.android.util.WooLog.T
import com.woocommerce.android.viewmodel.ResourceProvider
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

private const val KEY_TTP_PAYMENT_IN_PROGRESS = "ttp_payment_in_progress"

@HiltViewModel
class WooPosTotalsViewModel @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver,
    private val childrenToParentEventSender: WooPosChildrenToParentEventSender,
    private val cardReaderFacade: WooPosCardReaderFacade,
    private val totalsRepository: WooPosTotalsRepository,
    private val priceFormat: WooPosFormatPrice,
    private val analyticsTracker: WooPosAnalyticsTracker,
    private val networkStatus: WooPosNetworkStatus,
    private val cardReaderPaymentControllerFactory: CardReaderPaymentControllerFactory,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    private companion object {
        private const val EMPTY_ORDER_ID = -1L
        private const val KEY_STATE = "woo_pos_totals_data_state"
        private val InitialState = WooPosTotalsViewState.Loading
    }

    private val uiState: MutableStateFlow<WooPosTotalsViewState> =
        savedState.getStateFlow<WooPosTotalsViewState>(
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

    private var isTTPPaymentInProgress: Boolean
        get() = savedState.get<Boolean>(KEY_TTP_PAYMENT_IN_PROGRESS) == true
        set(value) {
            savedState[KEY_TTP_PAYMENT_IN_PROGRESS] = value
        }

    private var paymentScope: CoroutineScope? = null
    private var cardReaderPaymentController: CardReaderPaymentController? = null

    private fun createCardReaderPaymentController(orderId: Long) {
        paymentScope = CoroutineScope(SupervisorJob(viewModelScope.coroutineContext[Job]))
        cardReaderPaymentController = cardReaderPaymentControllerFactory.create(
            orderId = orderId,
            paymentType = PaymentOrRefund.Payment.PaymentType.WOO_POS,
            coroutineScope = paymentScope!!,
            isTTPPaymentInProgress = ::isTTPPaymentInProgress,
        )
    }

    init {
        listenUpEvents()
        observeCardReaderStatus()
    }

    private fun observeCardReaderStatus() {
        viewModelScope.launch {
            cardReaderFacade.readerStatus.collect { status ->
                when (status) {
                    is CardReaderStatus.NotConnected -> {
                        val state = uiState.value
                        if (state !is WooPosTotalsViewState.Totals) return@collect
                        uiState.value = state.copy(error = buildTotalsReaderNotConnectedError())
                    }
                    else -> {
                        val state = uiState.value
                        if (state !is WooPosTotalsViewState.Totals) return@collect
                        uiState.value = state.copy(error = null)
                        // now if order is created, collect payment
                        // TODO: @samiuelson
                    }
                }
            }
        }
    }

    fun onUIEvent(event: WooPosTotalsUIEvent) {
        when (event) {
            is WooPosTotalsUIEvent.OnNewTransactionClicked -> {
                viewModelScope.launch {
                    childrenToParentEventSender.sendToParent(
                        ChildToParentEvent.NewTransactionClicked
                    )
                }
            }
            is WooPosTotalsUIEvent.RetryOrderCreationClicked -> {
                createOrderDraft(dataState.value.productIds)
            }
        }
    }

    private fun collectPayment() {
        if (!networkStatus.isConnected()) {
            viewModelScope.launch {
                childrenToParentEventSender.sendToParent(ChildToParentEvent.NoInternet)
            }
        } else {
            val orderId = dataState.value.orderId
            check(orderId != EMPTY_ORDER_ID)
            if (cardReaderFacade.readerStatus.value is Connected) {
                val state = uiState.value
                check(state is WooPosTotalsViewState.Totals)
                val orderId = dataState.value.orderId
                check(orderId != EMPTY_ORDER_ID)
                check(uiState.value is WooPosTotalsViewState.Totals)
                createCardReaderPaymentController(dataState.value.orderId)
                cardReaderPaymentController?.start()
                listenToPaymentState()
            }
        }
    }

    private fun listenUpEvents() {
        viewModelScope.launch {
            parentToChildrenEventReceiver.events.collect { event ->
                when (event) {
                    is ParentToChildrenEvent.CheckoutClicked -> {
                        dataState.value = dataState.value.copy(productIds = event.productIds)
                        createOrderDraft(dataState.value.productIds)
                    }

                    is ParentToChildrenEvent.BackFromCheckoutToCartClicked -> {
                        cardReaderPaymentController?.onBackPressed()
                        cardReaderPaymentController?.onCleared()
                        paymentScope?.cancel()
                        uiState.value = InitialState
                    }

                    is ParentToChildrenEvent.ItemClickedInProductSelector,
                    ParentToChildrenEvent.OrderSuccessfullyPaid -> Unit
                }
            }
        }
    }

    private fun listenToPaymentState() {
        viewModelScope.launch {
            cardReaderPaymentController?.paymentState?.collect { paymentState ->
                val totalsState = uiState.value
                if (totalsState is WooPosTotalsViewState.Totals) {
                    uiState.value = totalsState.copy(
                        paymentStateText = paymentState.javaClass.simpleName
                    )
                }
                if (paymentState is CardReaderPaymentOrRefundState.CardReaderPaymentState.PaymentSuccessful) {
                    uiState.value =
                        WooPosTotalsViewState.PaymentSuccess(
                            orderTotalText = paymentState.amountWithCurrencyLabel
                        )
                    childrenToParentEventSender.sendToParent(ChildToParentEvent.OrderSuccessfullyPaid)
                }
            }
        }
    }

    private fun createOrderDraft(productIds: List<Long>) {
        viewModelScope.launch {
            uiState.value = WooPosTotalsViewState.Loading

            totalsRepository.createOrderWithProducts(productIds = productIds)
                .fold(
                    onSuccess = { order ->
                        dataState.value = dataState.value.copy(orderId = order.id)
                        uiState.value = buildWooPosTotalsViewState(order)
                        analyticsTracker.track(WooPosAnalyticsEvent.Event.OrderCreationSuccess)
                        collectPayment()
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
        val error = when (cardReaderFacade.readerStatus.value) {
            is Connected -> null
            else -> buildTotalsReaderNotConnectedError()
        }

        return WooPosTotalsViewState.Totals(
            orderSubtotalText = priceFormat(subtotalAmount),
            orderTaxText = priceFormat(taxAmount),
            orderTotalText = priceFormat(totalAmount),
            paymentStateText = "",
            error = error
        )
    }

    private fun buildTotalsReaderNotConnectedError(): WooPosTotalsViewState.Totals.Error = WooPosTotalsViewState.Totals.Error(
        title = resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_title),
        subtitle = resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_subtitle),
        actionButonLabel = resourceProvider.getString(
            R.string.woopos_success_totals_error_reader_not_connected_cta_button_label
        ),
        onAction = { cardReaderFacade.connectToReader() }
    )

    @Parcelize
    private data class TotalsDataState(
        val orderId: Long = EMPTY_ORDER_ID,
        val productIds: List<Long> = emptyList()
    ) : Parcelable
}
