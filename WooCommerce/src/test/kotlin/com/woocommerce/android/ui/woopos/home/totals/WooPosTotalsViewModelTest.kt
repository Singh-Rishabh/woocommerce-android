package com.woocommerce.android.ui.woopos.home.totals

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.AppPrefs
import com.woocommerce.android.R
import com.woocommerce.android.cardreader.CardReaderManager
import com.woocommerce.android.cardreader.connection.CardReaderStatus
import com.woocommerce.android.cardreader.connection.event.BluetoothCardReaderMessages
import com.woocommerce.android.cardreader.connection.event.CardReaderBatteryStatus
import com.woocommerce.android.cardreader.payments.CardPaymentStatus
import com.woocommerce.android.model.Order
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.payments.cardreader.CardReaderCountryConfigProvider
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderOnboardingChecker
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderInteracRefundErrorMapper
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderInteracRefundableChecker
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentCollectibilityChecker
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentErrorMapper
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentOrderHelper
import com.woocommerce.android.ui.payments.cardreader.payment.PaymentFlowError
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentController
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentControllerFactory
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentStateProvider
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderTrackCanceledFlowAction
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptHelper
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptShare
import com.woocommerce.android.ui.payments.tracking.CardReaderTrackingInfoKeeper
import com.woocommerce.android.ui.payments.tracking.PaymentsFlowTracker
import com.woocommerce.android.ui.woopos.cardreader.WooPosCardReaderFacade
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.ParentToChildrenEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.WooPosParentToChildrenEventReceiver
import com.woocommerce.android.ui.woopos.util.WooPosCoroutineTestRule
import com.woocommerce.android.ui.woopos.util.WooPosNetworkStatus
import com.woocommerce.android.ui.woopos.util.analytics.WooPosAnalyticsEvent
import com.woocommerce.android.ui.woopos.util.analytics.WooPosAnalyticsTracker
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.ResourceProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.store.WooCommerceStore
import java.math.BigDecimal
import java.util.Date
import kotlin.test.Test

@ExperimentalCoroutinesApi
class WooPosTotalsViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val coroutinesTestRule = WooPosCoroutineTestRule()

    private val networkStatus: WooPosNetworkStatus = mock()

    private val childrenToParentEventSender: WooPosChildrenToParentEventSender = mock()
    private val resourceProvider: ResourceProvider = mock()
    private val cardReaderManager: CardReaderManager = mock()
    private val orderRepository: OrderDetailRepository = mock()
    private val selectedSite: SelectedSite = mock()
    private val appPrefs: AppPrefs = mock()
    private val paymentCollectibilityChecker: CardReaderPaymentCollectibilityChecker = mock()
    private val interacRefundableChecker: CardReaderInteracRefundableChecker = mock()
    private val tracker: PaymentsFlowTracker = mock()
    private val trackCanceledFlow = CardReaderTrackCanceledFlowAction(tracker)
    private val currencyFormatter: CurrencyFormatter = mock()
    private val errorMapper: CardReaderPaymentErrorMapper = mock()
    private val interacRefundErrorMapper: CardReaderInteracRefundErrorMapper = mock()
    private val wooStore: WooCommerceStore = mock()
    private val cardReaderTrackingInfoKeeper: CardReaderTrackingInfoKeeper = mock()
    private val paymentStateProvider = CardReaderPaymentStateProvider()
    private val cardReaderPaymentOrderHelper: CardReaderPaymentOrderHelper = mock()
    private val paymentReceiptHelper: PaymentReceiptHelper = mock()
    private val cardReaderOnboardingChecker: CardReaderOnboardingChecker = mock()
    private val cardReaderConfigProvider: CardReaderCountryConfigProvider = mock()
    private val paymentReceiptShare: PaymentReceiptShare = mock()
    private val paymentControllerFactory = CardReaderPaymentControllerFactory(
        cardReaderManager = cardReaderManager,
        orderRepository = orderRepository,
        selectedSite = selectedSite,
        appPrefs = appPrefs,
        paymentCollectibilityChecker = paymentCollectibilityChecker,
        interacRefundableChecker = interacRefundableChecker,
        tracker = tracker,
        trackCancelledFlow = trackCanceledFlow,
        currencyFormatter = currencyFormatter,
        errorMapper = errorMapper,
        interacRefundErrorMapper = interacRefundErrorMapper,
        wooStore = wooStore,
        dispatchers = coroutinesTestRule.testDispatchers,
        cardReaderTrackingInfoKeeper = cardReaderTrackingInfoKeeper,
        paymentStateProvider = paymentStateProvider,
        cardReaderPaymentOrderHelper = cardReaderPaymentOrderHelper,
        paymentReceiptHelper = paymentReceiptHelper,
        cardReaderOnboardingChecker = cardReaderOnboardingChecker,
        cardReaderConfigProvider = cardReaderConfigProvider,
        paymentReceiptShare = paymentReceiptShare,
    )

    private fun createMockSavedStateHandle(): SavedStateHandle {
        return SavedStateHandle(
            mapOf(
                "orderId" to EMPTY_ORDER_ID,
                "totalsViewState" to WooPosTotalsViewState.Loading
            )
        )
    }

    private val cardReaderFacade: WooPosCardReaderFacade = mock()
    private val analyticsTracker: WooPosAnalyticsTracker = mock()

    private companion object {
        private const val EMPTY_ORDER_ID = -1L
    }

    @Before
    fun setUp() = runTest {
        whenever(cardReaderManager.readerStatus).thenReturn(MutableStateFlow(CardReaderStatus.Connected(mock())))
        whenever(cardReaderManager.batteryStatus).thenAnswer { flow { emit(CardReaderBatteryStatus.Unknown) } }
        whenever(cardReaderManager.collectPayment(any())).thenAnswer {
            flow<CardPaymentStatus> { }
        }
        whenever(cardReaderManager.retryCollectPayment(any(), any())).thenAnswer {
            flow<CardPaymentStatus> { }
        }
        whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
            flow<BluetoothCardReaderMessages> {}
        }
        whenever(cardReaderFacade.readerStatus).thenAnswer { cardReaderManager.readerStatus }
    }

    @Test
    fun `initial state is loading`() = runTest {
        // GIVEN
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(MutableStateFlow(ParentToChildrenEvent.BackFromCheckoutToCartClicked))
        }
        val savedState = createMockSavedStateHandle()

        // WHEN
        val viewModel = createViewModel(
            savedState = savedState,
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
        )

        // THEN
        assertThat(viewModel.state.value).isEqualTo(WooPosTotalsViewState.Loading)
    }

    @Test
    fun `given checkout started, when vm created, then order creation is started`() = runTest {
        // GIVEN
        val productIds = listOf(1L, 2L, 3L)
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }

        val order = Order.getEmptyOrder(
            dateCreated = Date(),
            dateModified = Date()
        ).copy(
            id = 123L,
            totalTax = BigDecimal("2.00"),
            items = listOf(
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                )
            ),
            productsTotal = BigDecimal("3.00"),
            total = BigDecimal("5.00"),
        )

        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking { createOrderWithProducts(productIds) }.thenReturn(Result.success(order))
        }

        val priceFormat: WooPosFormatPrice = mock {
            onBlocking { invoke(BigDecimal("1.00")) }.thenReturn("$1.00")
            onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("$2.00")
            onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("$3.00")
            onBlocking { invoke(BigDecimal("5.00")) }.thenReturn("$5.00")
        }

        // WHEN
        val viewModel = createViewModel(
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            totalsRepository = totalsRepository,
            priceFormat = priceFormat,
        )

        // THEN
        val state = viewModel.state.value as WooPosTotalsViewState.Totals
        assertThat(state.orderTotalText).isEqualTo("$5.00")
        assertThat(state.orderTaxText).isEqualTo("$2.00")
        assertThat(state.orderSubtotalText).isEqualTo("$3.00")
        verify(totalsRepository).createOrderWithProducts(productIds)
    }

    @Test
    fun `given checkout started and successfully created order, when vm created, then totals state correctly calculated`() =
        runTest {
            // GIVEN
            val productIds = listOf(1L, 2L, 3L)
            val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
            val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
                on { events }.thenReturn(parentToChildrenEventFlow)
            }
            val order = Order.getEmptyOrder(
                dateCreated = Date(),
                dateModified = Date()
            ).copy(
                totalTax = BigDecimal("2.00"),
                items = listOf(
                    Order.Item.EMPTY.copy(
                        subtotal = BigDecimal("1.00"),
                    ),
                    Order.Item.EMPTY.copy(
                        subtotal = BigDecimal("1.00"),
                    ),
                    Order.Item.EMPTY.copy(
                        subtotal = BigDecimal("1.00"),
                    )
                ),
                total = BigDecimal("5.00"),
                productsTotal = BigDecimal("3.00"),
            )
            val totalsRepository: WooPosTotalsRepository = mock {
                onBlocking { createOrderWithProducts(productIds = productIds) }.thenReturn(
                    Result.success(order)
                )
            }
            val priceFormat: WooPosFormatPrice = mock {
                onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("2.00$")
                onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("3.00$")
                onBlocking { invoke(BigDecimal("5.00")) }.thenReturn("5.00$")
            }

            // WHEN
            val viewModel = createViewModel(
                parentToChildrenEventReceiver = parentToChildrenEventReceiver,
                totalsRepository = totalsRepository,
                priceFormat = priceFormat,
            )

            // THEN
            val totals = viewModel.state.value as WooPosTotalsViewState.Totals
            assertThat(totals.orderTotalText).isEqualTo("5.00$")
            assertThat(totals.orderTaxText).isEqualTo("2.00$")
            assertThat(totals.orderSubtotalText).isEqualTo("3.00$")
        }

    @Test
    fun `given OnNewTransactionClicked, should send NewTransactionClicked event and reset state to initial`() =
        runTest {
            // GIVEN
            val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
                on { events }.thenReturn(mock())
            }
            whenever(resourceProvider.getString(R.string.woopos_success_totals_payment_failed_title))
                .thenReturn("Payment failed")
            whenever(resourceProvider.getString(R.string.woopos_success_totals_payment_failed_subtitle))
                .thenReturn("Unfortunately, this payment has been declined.")
            val savedState = createMockSavedStateHandle()

            val viewModel = createViewModel(
                savedState = savedState,
                parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            )

            // WHEN
            viewModel.onUIEvent(WooPosTotalsUIEvent.OnNewTransactionClicked)

            // THEN
            assertThat(viewModel.state.value).isEqualTo(WooPosTotalsViewState.Loading)
            verify(childrenToParentEventSender).sendToParent(ChildToParentEvent.NewTransactionClicked)
        }

    @Test
    fun `given order creation fails, when vm created, then error state is shown`() = runTest {
        // GIVEN
        val productIds = listOf(1L, 2L, 3L)
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }
        val errorMessage = "Order creation failed"
        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking { createOrderWithProducts(productIds = productIds) }.thenReturn(
                Result.failure(Exception(errorMessage))
            )
        }

        val resourceProvider: ResourceProvider = mock {
            on { getString(any()) }.thenReturn(errorMessage)
        }

        val savedState = createMockSavedStateHandle()

        // WHEN
        val viewModel = createViewModel(
            resourceProvider = resourceProvider,
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            totalsRepository = totalsRepository,
            savedState = savedState,
        )

        // THEN
        val state = viewModel.state.value
        assertThat(state).isInstanceOf(WooPosTotalsViewState.Error::class.java)
        state as WooPosTotalsViewState.Error
        assertThat(state.message).isEqualTo(errorMessage)
    }

    @Test
    fun `when RetryClicked event is triggered, should retry creating order and show loading state`() = runTest {
        // GIVEN
        val productIds = listOf(1L, 2L, 3L)
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }
        val errorMessage = "Order creation failed"
        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking { createOrderWithProducts(productIds) }.thenReturn(
                Result.failure(Exception(errorMessage))
            )
        }

        val resourceProvider: ResourceProvider = mock {
            on { getString(any()) }.thenReturn(errorMessage)
        }

        val savedState = createMockSavedStateHandle()
        val priceFormat: WooPosFormatPrice = mock {
            onBlocking { invoke(BigDecimal("1.00")) }.thenReturn("$1.00")
            onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("$2.00")
            onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("$3.00")
            onBlocking { invoke(BigDecimal("5.00")) }.thenReturn("$5.00")
        }

        val viewModel = createViewModel(
            resourceProvider = resourceProvider,
            savedState = savedState,
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            totalsRepository = totalsRepository,
            priceFormat = priceFormat,
        )

        // WHEN
        viewModel.onUIEvent(WooPosTotalsUIEvent.RetryOrderCreationClicked)

        // Ensure that the view model state transitions to error state
        assertThat(viewModel.state.value).isInstanceOf(WooPosTotalsViewState.Error::class.java)

        // Mock repository to simulate success on retry
        val order = Order.getEmptyOrder(
            dateCreated = Date(),
            dateModified = Date()
        ).copy(
            totalTax = BigDecimal("2.00"),
            items = listOf(
                Order.Item.EMPTY.copy(subtotal = BigDecimal("1.00")),
                Order.Item.EMPTY.copy(subtotal = BigDecimal("1.00")),
                Order.Item.EMPTY.copy(subtotal = BigDecimal("1.00"))
            ),
            total = BigDecimal("5.00"),
            productsTotal = BigDecimal("3.00"),
        )

        whenever(totalsRepository.createOrderWithProducts(productIds)).thenReturn(
            Result.success(order)
        )

        // Trigger RetryOrderCreationClicked again to simulate a successful retry
        viewModel.onUIEvent(WooPosTotalsUIEvent.RetryOrderCreationClicked)

        // Ensure the view model state transitions to the success state with correct totals
        val state = viewModel.state.value as WooPosTotalsViewState.Totals
        assertThat(state.orderTotalText).isEqualTo("$5.00")
        assertThat(state.orderTaxText).isEqualTo("$2.00")
        assertThat(state.orderSubtotalText).isEqualTo("$3.00")
    }

    @Test
    fun `when order is created, then should track order creation success`() = runTest {
        // GIVEN
        val productIds = listOf(1L, 2L, 3L)
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }

        val order = Order.getEmptyOrder(
            dateCreated = Date(),
            dateModified = Date()
        ).copy(
            id = 123L,
            totalTax = BigDecimal("2.00"),
            items = listOf(
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                )
            ),
            productsTotal = BigDecimal("3.00"),
            total = BigDecimal("5.00"),
        )

        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking { createOrderWithProducts(productIds = productIds) }.thenReturn(Result.success(order))
        }

        val priceFormat: WooPosFormatPrice = mock {
            onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("2.00$")
            onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("3.00$")
            onBlocking { invoke(BigDecimal("5.00")) }.thenReturn("5.00$")
        }

        // WHEN
        val viewModel = createViewModel(
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            totalsRepository = totalsRepository,
            priceFormat = priceFormat,
        )

        // THEN
        val state = viewModel.state.value as WooPosTotalsViewState.Totals
        assertThat(state.orderTotalText).isEqualTo("5.00$")
        assertThat(state.orderTaxText).isEqualTo("2.00$")
        assertThat(state.orderSubtotalText).isEqualTo("3.00$")
        verify(totalsRepository).createOrderWithProducts(productIds)
    }

    @Test
    fun `when fails to create order, then should track order creation failure`() = runTest {
        val productIds = listOf(1L, 2L, 3L)
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }
        val errorMessage = "Order creation failed"
        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking { createOrderWithProducts(productIds = productIds) }.thenReturn(
                Result.failure(Exception(errorMessage))
            )
        }

        val resourceProvider: ResourceProvider = mock {
            on { getString(any()) }.thenReturn(errorMessage)
        }

        createViewModel(
            resourceProvider = resourceProvider,
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            totalsRepository = totalsRepository,
        )

        verify(
            analyticsTracker
        ).track(
            WooPosAnalyticsEvent.Error.OrderCreationError(
                WooPosTotalsViewModel::class,
                Exception::class.java.simpleName,
                errorMessage
            )
        )
    }

    @Test
    fun `given reader connected, when order created, then payment collection started`() = runTest {
        // GIVEN
        whenever(networkStatus.isConnected()).thenReturn(true)
        val productIds = listOf(1L, 2L, 3L)
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }

        val order = Order.getEmptyOrder(
            dateCreated = Date(),
            dateModified = Date()
        ).copy(
            id = 123L,
            totalTax = BigDecimal("2.00"),
            items = listOf(
                Order.Item.EMPTY.copy(subtotal = BigDecimal("1.00")),
            ),
            total = BigDecimal("3.00"),
            productsTotal = BigDecimal("1.00"),
        )

        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking { createOrderWithProducts(any()) }.thenReturn(Result.success(order))
        }

        val savedState = createMockSavedStateHandle()
        val priceFormat: WooPosFormatPrice = mock {
            onBlocking { invoke(BigDecimal("1.00")) }.thenReturn("$1.00")
            onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("$2.00")
            onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("$3.00")
        }
        whenever(cardReaderFacade.readerStatus).thenReturn(MutableStateFlow(CardReaderStatus.Connected(mock())))

        val viewModel = createViewModel(
            savedState = savedState,
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            totalsRepository = totalsRepository,
            priceFormat = priceFormat,
        )

        // WHEN
        advanceUntilIdle()

        // THEN
        val state = viewModel.state.value as WooPosTotalsViewState.Totals
        assertThat(state.orderTotalText).isEqualTo("$3.00")
        assertThat(state.paymentStateText).isNotNull()
    }

    @org.junit.Test
    fun `given there is no internet, then trigger proper event`() = runTest {
        // GIVEN
        whenever(networkStatus.isConnected()).thenReturn(false)
        val readerStatus = MutableStateFlow<CardReaderStatus>(CardReaderStatus.Connected(mock()))
        whenever(cardReaderFacade.readerStatus).thenReturn(readerStatus)

        // WHEN
        createViewModelAndSetupForSuccessfulOrderCreation()

        // THEN
        verify(childrenToParentEventSender, atLeastOnce()).sendToParent(ChildToParentEvent.NoInternet)
    }

    @org.junit.Test
    fun `given there is no internet, then collect payment method is not called`() = runTest {
        // GIVEN
        whenever(networkStatus.isConnected()).thenReturn(false)
        val productIds = listOf(1L, 2L, 3L)
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }
        val order = Order.getEmptyOrder(
            dateCreated = Date(),
            dateModified = Date()
        ).copy(
            totalTax = BigDecimal("2.00"),
            items = listOf(
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                )
            ),
            productsTotal = BigDecimal("3.00"),
            total = BigDecimal("5.00"),
        )
        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking { createOrderWithProducts(productIds = productIds) }.thenReturn(
                Result.success(order)
            )
        }
        val priceFormat: WooPosFormatPrice = mock {
            onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("2.00$")
            onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("3.00$")
            onBlocking { invoke(BigDecimal("5.00")) }.thenReturn("5.00$")
        }

        // WHEN
        createViewModel(
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            totalsRepository = totalsRepository,
            priceFormat = priceFormat,
        )

        // THEN
        verify(cardReaderManager, never()).collectPayment(any())
    }

    @Test
    fun `given reader not connected, when checkout clicked, then should show error`() = runTest {
        // GIVEN
        val readerStatus: StateFlow<CardReaderStatus> =
            MutableStateFlow<CardReaderStatus>(CardReaderStatus.NotConnected())
        whenever(cardReaderManager.readerStatus).thenReturn(readerStatus)
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_title))
            .thenReturn("Reader not connected")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_subtitle))
            .thenReturn("To process this payment, please connect your reader.")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_cta_button_label))
            .thenReturn("Connect to reader")

        val productIds = listOf(1L, 2L, 3L)
        val order = Order.getEmptyOrder(
            dateCreated = Date(),
            dateModified = Date()
        ).copy(
            totalTax = BigDecimal("2.00"),
            items = listOf(
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                )
            ),
            productsTotal = BigDecimal("3.00"),
            total = BigDecimal("5.00"),
        )
        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking {
                createOrderWithProducts(productIds = productIds)
            }.thenReturn(Result.success(order))
        }
        val priceFormat: WooPosFormatPrice = mock {
            onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("2.00$")
            onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("3.00$")
            onBlocking { invoke(BigDecimal("5.00")) }.thenReturn("5.00$")
        }
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }

        // WHEN
        val viewModel = createViewModel(
            totalsRepository = totalsRepository,
            priceFormat = priceFormat,
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
        )

        // THEN
        assertThat(viewModel.state.value).isInstanceOf(WooPosTotalsViewState.Totals::class.java)
        val state = viewModel.state.value as WooPosTotalsViewState.Totals
        assertThat(state.error).isNotNull()
        with(state.error!!) {
            assertThat(title).isEqualTo("Reader not connected")
            assertThat(subtitle).isEqualTo("To process this payment, please connect your reader.")
            assertThat(actionButonLabel).isEqualTo("Connect to reader")
        }
    }

    @Test
    fun `given reader connected, when checkout clicked, then should hide error`() {
        // GIVEN
        val readerStatus: StateFlow<CardReaderStatus> =
            MutableStateFlow<CardReaderStatus>(CardReaderStatus.Connected(mock()))
        whenever(cardReaderManager.readerStatus).thenReturn(readerStatus)
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_title))
            .thenReturn("Reader not connected")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_subtitle))
            .thenReturn("To process this payment, please connect your reader.")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_cta_button_label))
            .thenReturn("Connect to reader")

        val productIds = listOf(1L, 2L, 3L)
        val order = Order.getEmptyOrder(
            dateCreated = Date(),
            dateModified = Date()
        ).copy(
            totalTax = BigDecimal("2.00"),
            items = listOf(
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                )
            ),
            productsTotal = BigDecimal("3.00"),
            total = BigDecimal("5.00"),
        )
        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking {
                createOrderWithProducts(productIds = productIds)
            }.thenReturn(Result.success(order))
        }
        val priceFormat: WooPosFormatPrice = mock {
            onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("2.00$")
            onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("3.00$")
            onBlocking { invoke(BigDecimal("5.00")) }.thenReturn("5.00$")
        }
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }

        // WHEN
        val viewModel = createViewModel(
            totalsRepository = totalsRepository,
            priceFormat = priceFormat,
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
        )

        // THEN
        assertThat(viewModel.state.value).isInstanceOf(WooPosTotalsViewState.Totals::class.java)
        val state = viewModel.state.value as WooPosTotalsViewState.Totals
        assertThat(state.error).isNull()
    }

    @Test
    fun `given reader not connected, when checkout clicked and error CTA clicked, then should try connecting to reader`() = runTest {
        // GIVEN
        val readerStatus: StateFlow<CardReaderStatus> =
            MutableStateFlow<CardReaderStatus>(CardReaderStatus.NotConnected())
        whenever(cardReaderManager.readerStatus).thenReturn(readerStatus)

        // WHEN
        val viewModel = createViewModelAndSetupForSuccessfulOrderCreation()

        (viewModel.state.value as WooPosTotalsViewState.Totals).error!!.onAction()

        // THEN
        verify(cardReaderFacade).connectToReader()
    }

    @Test
    fun `given order draft created, when reader connects, then start payment automatically`() = runTest {
        // GIVEN
        whenever(networkStatus.isConnected()).thenReturn(true)
        val readerStatus = MutableStateFlow<CardReaderStatus>(CardReaderStatus.NotConnected())
        whenever(cardReaderFacade.readerStatus).thenReturn(readerStatus)

        val mockCardReaderPaymentController: CardReaderPaymentController = mock()
        val factory: CardReaderPaymentControllerFactory = mock()
        whenever(factory.create(any(), any(), any(), any())).thenReturn(mockCardReaderPaymentController)
        createViewModelAndSetupForSuccessfulOrderCreation(controllerFactory = factory)

        readerStatus.value = CardReaderStatus.Connected(mock())

        // THEN
        verify(mockCardReaderPaymentController).start()
    }

    @Test
    fun `given order draft created, when reader disconnects, then should abort payment action`() = runTest {
        // GIVEN
        whenever(networkStatus.isConnected()).thenReturn(true)
        val readerStatus = MutableStateFlow<CardReaderStatus>(CardReaderStatus.Connected(mock()))
        whenever(cardReaderFacade.readerStatus).thenReturn(readerStatus)

        val mockCardReaderPaymentController: CardReaderPaymentController = mock()
        val factory: CardReaderPaymentControllerFactory = mock()
        whenever(factory.create(any(), any(), any(), any())).thenReturn(mockCardReaderPaymentController)
        val vm = createViewModelAndSetupForSuccessfulOrderCreation(controllerFactory = factory)

        // WHEN
        readerStatus.value = CardReaderStatus.NotConnected()

        // THEN
        verify(mockCardReaderPaymentController).onCleared()
        verify(mockCardReaderPaymentController).onBackPressed()
        assertThat(vm.paymentScope!!.isActive).isFalse
    }

    @Test
    fun `given order draft created and reader connected, when card tapped, should show payment processing screen`() = runTest {
        // GIVEN
        whenever(
            resourceProvider.getString(
                R.string.woopos_success_totals_payment_processing_title
            )
        ).thenReturn("Processing payment")
        whenever(
            resourceProvider.getString(
                R.string.woopos_success_totals_payment_processing_subtitle
            )
        ).thenReturn("Please wait…")
        whenever(networkStatus.isConnected()).thenReturn(true)
        val readerStatus = MutableStateFlow<CardReaderStatus>(CardReaderStatus.Connected(mock()))
        whenever(cardReaderFacade.readerStatus).thenReturn(readerStatus)
        val mockCardReaderPaymentController: CardReaderPaymentController = mock()
        val factory: CardReaderPaymentControllerFactory = mock()
        whenever(factory.create(any(), any(), any(), any())).thenReturn(mockCardReaderPaymentController)
        val paymentState =
            MutableStateFlow<CardReaderPaymentOrRefundState>(
                CardReaderPaymentState.CollectingPayment.ExternalReaderCollectPaymentState("") {}
            )
        whenever(mockCardReaderPaymentController.paymentState).thenReturn(paymentState)
        val vm = createViewModelAndSetupForSuccessfulOrderCreation(controllerFactory = factory)

        // WHEN
        paymentState.value = CardReaderPaymentState.ProcessingPayment.ExternalReaderProcessingPayment("") {}

        // THEN
        assertThat(vm.state.value).isInstanceOf(WooPosTotalsViewState.PaymentProcessing::class.java)
    }

    @Test
    fun `given payment failed, when retry clicked, then should retry`() {
        // GIVEN
        whenever(resourceProvider.getString(R.string.woopos_success_totals_payment_processing_title))
            .thenReturn("Processing payment")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_payment_processing_subtitle))
            .thenReturn("Please wait…")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_payment_failed_title))
            .thenReturn("Payment failed")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_payment_failed_subtitle))
            .thenReturn("Unfortunately, this payment has been declined.")

        whenever(networkStatus.isConnected()).thenReturn(true)
        val readerStatus = MutableStateFlow<CardReaderStatus>(CardReaderStatus.Connected(mock()))
        whenever(cardReaderFacade.readerStatus).thenReturn(readerStatus)
        val mockCardReaderPaymentController: CardReaderPaymentController = mock()
        val factory: CardReaderPaymentControllerFactory = mock()
        whenever(factory.create(any(), any(), any(), any())).thenReturn(mockCardReaderPaymentController)
        val paymentState =
            MutableStateFlow<CardReaderPaymentOrRefundState>(
                CardReaderPaymentState.CollectingPayment.ExternalReaderCollectPaymentState("") {}
            )
        whenever(mockCardReaderPaymentController.paymentState).thenReturn(paymentState)
        val vm = createViewModelAndSetupForSuccessfulOrderCreation(controllerFactory = factory)
        paymentState.value = CardReaderPaymentState.ProcessingPayment.ExternalReaderProcessingPayment("") {}
        paymentState.value = CardReaderPaymentState.PaymentFailed.ExternalReaderFailedPayment.NonCancelable(
            errorType = PaymentFlowError.NoNetwork, {})
        assertThat(vm.state.value).isInstanceOf(WooPosTotalsViewState.PaymentFailed::class.java)

        // WHEN
        paymentState.value = CardReaderPaymentState.CollectingPayment.ExternalReaderCollectPaymentState("") {}
        vm.onUIEvent(WooPosTotalsUIEvent.RetryFailedTransactionClicked)

        // THEN
        assertThat(vm.state.value).isInstanceOf(WooPosTotalsViewState.Totals::class.java)
    }

    @Test
    fun `given payment failed, when exit order clicked, then should inform home about the situation`() {

    }

    private fun createViewModelAndSetupForSuccessfulOrderCreation(
        controllerFactory: CardReaderPaymentControllerFactory = paymentControllerFactory
    ): WooPosTotalsViewModel {
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_title))
            .thenReturn("Reader not connected")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_subtitle))
            .thenReturn("To process this payment, please connect your reader.")
        whenever(resourceProvider.getString(R.string.woopos_success_totals_error_reader_not_connected_cta_button_label))
            .thenReturn("Connect to reader")

        val productIds = listOf(1L, 2L, 3L)
        val orderId = 23L
        val order = Order.getEmptyOrder(
            dateCreated = Date(),
            dateModified = Date()
        ).copy(
            id = orderId,
            totalTax = BigDecimal("2.00"),
            items = listOf(
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                ),
                Order.Item.EMPTY.copy(
                    subtotal = BigDecimal("1.00"),
                )
            ),
            productsTotal = BigDecimal("3.00"),
            total = BigDecimal("5.00"),
        )
        val totalsRepository: WooPosTotalsRepository = mock {
            onBlocking {
                createOrderWithProducts(productIds = productIds)
            }.thenReturn(Result.success(order))
        }
        val priceFormat: WooPosFormatPrice = mock {
            onBlocking { invoke(BigDecimal("2.00")) }.thenReturn("2.00$")
            onBlocking { invoke(BigDecimal("3.00")) }.thenReturn("3.00$")
            onBlocking { invoke(BigDecimal("5.00")) }.thenReturn("5.00$")
        }
        val parentToChildrenEventFlow = MutableStateFlow(ParentToChildrenEvent.CheckoutClicked(productIds))
        val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock {
            on { events }.thenReturn(parentToChildrenEventFlow)
        }
        return createViewModel(
            totalsRepository = totalsRepository,
            priceFormat = priceFormat,
            parentToChildrenEventReceiver = parentToChildrenEventReceiver,
            cardReaderPaymentControllerFactory = controllerFactory,
        )
    }

    private fun createViewModel(
        resourceProvider: ResourceProvider = this.resourceProvider,
        parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock(),
        totalsRepository: WooPosTotalsRepository = mock(),
        priceFormat: WooPosFormatPrice = mock(),
        savedState: SavedStateHandle = SavedStateHandle(),
        cardReaderPaymentControllerFactory: CardReaderPaymentControllerFactory = paymentControllerFactory,
    ) = WooPosTotalsViewModel(
        resourceProvider = resourceProvider,
        parentToChildrenEventReceiver = parentToChildrenEventReceiver,
        childrenToParentEventSender = childrenToParentEventSender,
        cardReaderFacade = cardReaderFacade,
        totalsRepository = totalsRepository,
        priceFormat = priceFormat,
        analyticsTracker = analyticsTracker,
        networkStatus = networkStatus,
        cardReaderPaymentControllerFactory = cardReaderPaymentControllerFactory,
        savedState = savedState,
    )
}
