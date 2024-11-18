package com.woocommerce.android.ui.payments.cardreader.payment.controller

import com.woocommerce.android.AppPrefs
import com.woocommerce.android.R
import com.woocommerce.android.cardreader.CardReaderManager
import com.woocommerce.android.cardreader.connection.CardReaderStatus
import com.woocommerce.android.cardreader.connection.event.BluetoothCardReaderMessages
import com.woocommerce.android.cardreader.connection.event.CardReaderBatteryStatus
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.CARD_REMOVED_TOO_EARLY
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.INSERT_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.INSERT_OR_SWIPE_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.REMOVE_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.SWIPE_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.TRY_ANOTHER_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.TRY_ANOTHER_READ_METHOD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.CollectingPayment
import com.woocommerce.android.model.Address
import com.woocommerce.android.model.Order
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.payments.cardreader.CardReaderCountryConfigProvider
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderFlowParam
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderFlowParam.PaymentOrRefund.Payment.PaymentType.ORDER
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderOnboardingChecker
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderType
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderInteracRefundErrorMapper
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderInteracRefundableChecker
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentCollectibilityChecker
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentErrorMapper
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentOrderHelper
import com.woocommerce.android.ui.payments.cardreader.payment.ViewState.ExternalReaderCollectPaymentState
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptHelper
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptShare
import com.woocommerce.android.ui.payments.tracking.CardReaderTrackingInfoKeeper
import com.woocommerce.android.ui.payments.tracking.PaymentsFlowTracker
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.WooCommerceStore
import java.math.BigDecimal
import kotlin.reflect.KMutableProperty0

@OptIn(ExperimentalCoroutinesApi::class)
class CardReaderPaymentControllerTest : BaseUnitTest() {
    private lateinit var controller: CardReaderPaymentController

    private val cardReaderManager: CardReaderManager = mock()
    private val orderRepository: OrderDetailRepository = mock()
    private val selectedSite: SelectedSite = mock()
    private val paymentCollectibilityChecker: CardReaderPaymentCollectibilityChecker = mock()
    private val tracker: PaymentsFlowTracker = mock()
    private val trackCanceledFlow = CardReaderTrackCanceledFlow(tracker)
    private val appPrefs: AppPrefs = mock()
    private val currencyFormatter: CurrencyFormatter = mock()
    private val wooStore: WooCommerceStore = mock()
    private val errorMapper: CardReaderPaymentErrorMapper = mock()
    private val cardReaderTrackingInfoKeeper: CardReaderTrackingInfoKeeper = mock()
    private val interacRefundErrorMapper: CardReaderInteracRefundErrorMapper = mock()
    private val interacRefundableChecker: CardReaderInteracRefundableChecker = mock()
    private val paymentStateProvider = CardReaderPaymentStateProvider()
    private val cardReaderPaymentOrderHelper: CardReaderPaymentOrderHelper = mock()
    private val paymentReceiptHelper: PaymentReceiptHelper = mock()
    private val cardReaderOnboardingChecker: CardReaderOnboardingChecker = mock()
    private val cardReaderConfigProvider: CardReaderCountryConfigProvider = mock()
    private val paymentReceiptShare: PaymentReceiptShare = mock()

    private var isTTPinProgress = false
    private val isTTPinProgressProp: KMutableProperty0<Boolean> = ::isTTPinProgress

    private val paymentParam = CardReaderFlowParam.PaymentOrRefund.Payment(ORDER_ID, ORDER)

    private val mockedOrder = mock<Order>()
    private val mockedAddress = mock<Address>()

    @OptIn(InternalCoroutinesApi::class)
    @Before
    fun setUp() = testBlocking {
        createController()
        whenever(cardReaderManager.readerStatus).thenReturn(MutableStateFlow(CardReaderStatus.Connected(mock())))
        whenever(orderRepository.fetchOrderById(ORDER_ID)).thenReturn(mockedOrder)

        whenever(mockedOrder.total).thenReturn(DUMMY_TOTAL)
        whenever(mockedOrder.currency).thenReturn("GBP")
        whenever(mockedOrder.billingAddress).thenReturn(mockedAddress)
        whenever(mockedAddress.email).thenReturn("")
        whenever(mockedAddress.firstName).thenReturn("Tester")
        whenever(mockedAddress.lastName).thenReturn("Test")
        whenever(mockedOrder.orderKey).thenReturn("wc_order_j0LMK3bFhalEL")
        whenever(mockedOrder.id).thenReturn(ORDER_ID)

        whenever(paymentCollectibilityChecker.isCollectable(any())).thenReturn(true)
        whenever(selectedSite.get()).thenReturn(siteModel)
        whenever(wooStore.getStoreCountryCode(any())).thenReturn("US")
        whenever(appPrefs.getCardReaderStatementDescriptor(anyOrNull(), anyOrNull(), anyOrNull()))
            .thenReturn("test statement descriptor")
        whenever(paymentReceiptHelper.isPluginCanSendReceipt(siteModel)).thenReturn(true)

        whenever(paymentReceiptHelper.isPluginCanSendReceipt(siteModel)).thenReturn(true)
        whenever(cardReaderPaymentOrderHelper.getPaymentDescription(mockedOrder)).thenReturn("test description")
        whenever(cardReaderPaymentOrderHelper.getAmountLabel(mockedOrder))
            .thenReturn("${DUMMY_CURRENCY_SYMBOL}${DUMMY_TOTAL}")
        whenever(cardReaderManager.batteryStatus).thenAnswer { flow { emit(CardReaderBatteryStatus.Unknown) } }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createController() {
        controller = CardReaderPaymentController(
            scope = TestScope(coroutinesTestRule.testDispatcher),
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
            paymentOrRefund = paymentParam,
            cardReaderType = CardReaderType.EXTERNAL,
            isTTPPaymentInProgress = isTTPinProgressProp,
        )
    }

    @Test
    fun `given collect payment shown, when RETRY message received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    delay(1) // make sure it's run after collecting payment starts
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(AdditionalInfoType.RETRY_CARD))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()
            advanceUntilIdle()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_retry_card_prompt)
        }

    @Test
    fun `given collect payment shown, when INSERT_CARD received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(INSERT_CARD))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_collect_payment_hint)
        }

    @Test
    fun `given collect payment shown, when INSERT_OR_SWIPE_CARD received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(INSERT_OR_SWIPE_CARD))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_collect_payment_hint)
        }

    @Test
    fun `given collect payment shown, when SWIPE_CARD received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(SWIPE_CARD))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_collect_payment_hint)
        }

    @Test
    fun `given collect payment shown, when REMOVE_CARD received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    delay(1) // make sure it's run after collecting payment starts
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(REMOVE_CARD))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()
            advanceUntilIdle()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_remove_card_prompt)
        }

    @Test
    fun `given collect payment shown, when TRY_OTHER_CARD message received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    delay(1) // make sure it's run after collecting payment starts
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(TRY_ANOTHER_CARD))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()
            advanceUntilIdle()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_try_another_card_prompt)
        }

    @Test
    fun `given collect payment shown, when CARD_REMOVED_TOO_EARLY message received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    delay(1) // make sure it runs after collecting payment starts
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(CARD_REMOVED_TOO_EARLY))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()
            advanceUntilIdle()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_card_removed_too_early)
        }

    @Test
    fun `given collect payment shown, when TRY_OTHER_READ message received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    delay(1) // make sure it's run after collecting payment starts
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(TRY_ANOTHER_READ_METHOD))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()
            advanceUntilIdle()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_try_another_read_method_prompt)
        }

    companion object {
        private const val ORDER_ID = 1L
        private val siteModel = SiteModel().apply { name = "testName" }.apply { url = "testUrl.com" }
        private val DUMMY_TOTAL = BigDecimal(10.72)
        private const val DUMMY_CURRENCY_SYMBOL = "£"
    }
}
