package com.woocommerce.android.ui.payments.cardreader.payment.controller

import com.woocommerce.android.AppPrefs
import com.woocommerce.android.R
import com.woocommerce.android.cardreader.CardReaderManager
import com.woocommerce.android.cardreader.config.CardReaderConfigForSupportedCountry
import com.woocommerce.android.cardreader.config.CardReaderConfigForUSA
import com.woocommerce.android.cardreader.connection.CardReaderStatus
import com.woocommerce.android.cardreader.connection.event.BluetoothCardReaderMessages
import com.woocommerce.android.cardreader.connection.event.CardReaderBatteryStatus
import com.woocommerce.android.cardreader.payments.CardPaymentStatus
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.CARD_REMOVED_TOO_EARLY
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.INSERT_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.INSERT_OR_SWIPE_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.MULTIPLE_CONTACTLESS_CARDS_DETECTED
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.REMOVE_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.SWIPE_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.TRY_ANOTHER_CARD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType.TRY_ANOTHER_READ_METHOD
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.CapturingPayment
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.CardPaymentStatusErrorType.DeclinedByBackendError
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.CardPaymentStatusErrorType.Generic
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.CardPaymentStatusErrorType.NoNetwork
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.CardPaymentStatusErrorType.Server
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.CollectingPayment
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.InitializingPayment
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.PaymentCompleted
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.PaymentFailed
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.PaymentMethodType
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.ProcessingPayment
import com.woocommerce.android.cardreader.payments.CardPaymentStatus.ProcessingPaymentCompleted
import com.woocommerce.android.cardreader.payments.PaymentData
import com.woocommerce.android.cardreader.payments.PaymentInfo
import com.woocommerce.android.model.Address
import com.woocommerce.android.model.Order
import com.woocommerce.android.model.UiString.UiStringText
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.payments.cardreader.CardReaderCountryConfigProvider
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderFlowParam
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderFlowParam.PaymentOrRefund.Payment.PaymentType.ORDER
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderOnboardingChecker
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderType
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderType.BUILT_IN
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderInteracRefundErrorMapper
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderInteracRefundableChecker
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentCollectibilityChecker
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentErrorMapper
import com.woocommerce.android.ui.payments.cardreader.payment.CardReaderPaymentOrderHelper
import com.woocommerce.android.ui.payments.cardreader.payment.PaymentFlowError
import com.woocommerce.android.ui.payments.cardreader.payment.PaymentFlowError.AmountTooSmall
import com.woocommerce.android.ui.payments.cardreader.payment.PaymentFlowError.Unknown
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentEvent.PlaySuccessfulPaymentSound
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentEvent.ShowErrorMessage
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState
import com.woocommerce.android.ui.payments.cardreader.payment.controller.CardReaderPaymentOrRefundState.CardReaderPaymentState.PaymentFailed.*
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptHelper
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptShare
import com.woocommerce.android.ui.payments.tracking.CardReaderTrackingInfoKeeper
import com.woocommerce.android.ui.payments.tracking.PaymentsFlowTracker
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.util.runAndCaptureValues
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.WooCommerceStore
import java.math.BigDecimal
import kotlin.reflect.KMutableProperty0
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

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

    private val cardReaderConfig: CardReaderConfigForSupportedCountry = CardReaderConfigForUSA
    private val paymentFailedWithEmptyDataForRetry = PaymentFailed(Generic, null, "dummy msg")
    private val paymentFailedWithValidDataForRetry = PaymentFailed(Generic, mock(), "dummy msg")
    private val paymentFailedWithNoNetwork = PaymentFailed(NoNetwork, mock(), "dummy msg")
    private val paymentFailedWithPaymentDeclined = PaymentFailed(DeclinedByBackendError.Unknown, mock(), "dummy msg")
    private val paymentFailedWithCardReadTimeOut = PaymentFailed(Generic, mock(), "dummy msg")
    private val paymentFailedWithServerError = PaymentFailed(Server(""), mock(), "dummy msg")
    private val paymentFailedWithAmountTooSmall = PaymentFailed(
        DeclinedByBackendError.AmountTooSmall,
        mock(),
        "dummy msg"
    )

    @OptIn(InternalCoroutinesApi::class)
    @Before
    fun setUp() = testBlocking {
        createController()
        whenever(cardReaderConfigProvider.provideCountryConfigFor("US"))
            .thenReturn(CardReaderConfigForUSA)
        whenever(cardReaderManager.readerStatus).thenReturn(MutableStateFlow(CardReaderStatus.Connected(mock())))
        whenever(orderRepository.fetchOrderById(ORDER_ID)).thenReturn(mockedOrder)
        whenever(orderRepository.getOrderById(any())).thenReturn(mockedOrder)

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

        whenever(cardReaderPaymentOrderHelper.getAmountLabel(mockedOrder))
            .thenReturn("${DUMMY_CURRENCY_SYMBOL}${DUMMY_TOTAL}")
        whenever(cardReaderManager.batteryStatus).thenAnswer { flow { emit(CardReaderBatteryStatus.Unknown) } }
        whenever(currencyFormatter.formatAmountWithCurrency(DUMMY_TOTAL.toDouble(), "GBP"))
            .thenReturn("${DUMMY_CURRENCY_SYMBOL}${DUMMY_TOTAL}")
        whenever(mockedOrder.billingAddress).thenReturn(mockedAddress)
        whenever(mockedAddress.email).thenReturn("")
        whenever(mockedAddress.firstName).thenReturn("Tester")
        whenever(mockedAddress.lastName).thenReturn("Test")
        whenever(mockedOrder.orderKey).thenReturn("wc_order_j0LMK3bFhalEL")
        whenever(mockedOrder.chargeId).thenReturn("chargeId")
        whenever(cardReaderManager.collectPayment(any())).thenAnswer {
            flow<CardPaymentStatus> { }
        }
        whenever(cardReaderManager.retryCollectPayment(any(), any())).thenAnswer {
            flow<CardPaymentStatus> { }
        }
        whenever(interacRefundableChecker.isRefundable(any())).thenReturn(true)
        whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
            flow<BluetoothCardReaderMessages> {}
        }
        whenever(paymentReceiptHelper.getReceiptUrl(ORDER_ID)).thenReturn(Result.success("test url"))
        whenever(cardReaderPaymentOrderHelper.getPaymentDescription(mockedOrder)).thenReturn("test description")
        whenever(cardReaderPaymentOrderHelper.getReceiptDocumentName(mockedOrder.id)).thenReturn("receipt-order-1")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun createController(cardReaderType: CardReaderType = CardReaderType.EXTERNAL) {
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
            cardReaderType = cardReaderType,
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

    @Test
    fun `given collect payment shown, when MULTIPLE_CARDS_DETECTED received, then collect payment hint updated`() =
        testBlocking {
            whenever(cardReaderManager.displayBluetoothCardReaderMessages).thenAnswer {
                flow {
                    delay(1) // make sure it's run after collecting payment starts
                    emit(BluetoothCardReaderMessages.CardReaderDisplayMessage(MULTIPLE_CONTACTLESS_CARDS_DETECTED))
                }
            }

            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()
            advanceUntilIdle()

            assertThat((controller.paymentState.value as CardReaderPaymentState.CollectingPayment).cardReaderHint)
                .isEqualTo(R.string.card_reader_payment_multiple_contactless_cards_detected_prompt)
        }

    @Test
    fun `given fetching order fails, when payment screen shown, then ExternalReaderFailedPayment state is shown`() =
        testBlocking {
            whenever(orderRepository.fetchOrderById(ORDER_ID)).thenReturn(null)

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(ExternalReaderFailedPayment::class.java)
        }

    @Test
    fun `given fetching order fails and tpp, when payment screen shown, then BuiltInReaderFailedPayment state is shown`() =
        testBlocking {
            whenever(orderRepository.fetchOrderById(ORDER_ID)).thenReturn(null)

            createController(cardReaderType = BUILT_IN)

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(BuiltInReaderFailedPayment::class.java)
        }

    @Test
    fun `when fetching order fails, then event tracked`() =
        testBlocking {
            whenever(orderRepository.fetchOrderById(ORDER_ID)).thenReturn(null)

            controller.start()

            verify(tracker).trackPaymentFailed(anyOrNull(), anyOrNull())
        }

    @Test
    fun `given fetching order succeeds, when payment screen shown, then order currency stored `() =
        testBlocking {
            controller.start()

            verify(cardReaderTrackingInfoKeeper).setCurrency(("GBP"))
        }

    @Test
    fun `when payment screen shown, then loading data state is emitted`() {
        controller.start()

        assertThat(controller.paymentState.value).isInstanceOf(CardReaderPaymentState.LoadingData::class.java)
    }

    @Test
    fun `when payment not collectable, then error event emitted and flow terminated`() =
        testBlocking {
            whenever(paymentCollectibilityChecker.isCollectable(any())).thenReturn(false)
            val events = mutableListOf<CardReaderPaymentEvent>()
            val job = launch {
                controller.event.collect {
                    events.add(it)
                }
            }

            controller.start()

            assertThat(
                (events[0] as ShowErrorMessage).message
            ).isEqualTo(
                R.string.card_reader_payment_order_paid_payment_cancelled
            )
            assertThat(events[1]).isInstanceOf(CardReaderPaymentEvent.Exit::class.java)
            job.cancel()
        }

    @Test
    fun `when flow started, then correct payment description is propagated to CardReaderManager`() =
        testBlocking {
            val siteName = "testName"
            val siteId = 12345L
            val expectedResult = "hooray"
            whenever(selectedSite.get()).thenReturn(
                SiteModel().apply {
                    name = siteName
                    url = ""
                    this.siteId = siteId
                }
            )
            whenever(cardReaderPaymentOrderHelper.getPaymentDescription(mockedOrder)).thenReturn(expectedResult)
            val captor = argumentCaptor<PaymentInfo>()

            controller.start()

            verify(cardReaderManager).collectPayment(captor.capture())
            assertThat(captor.firstValue.paymentDescription).isEqualTo(expectedResult)
        }

    @Test
    fun `when flow started, then correct statement descriptor is propagated to CardReaderManager`() =
        testBlocking {
            val expectedResult = "hooray"
            whenever(appPrefs.getCardReaderStatementDescriptor(anyOrNull(), anyOrNull(), anyOrNull()))
                .thenReturn(expectedResult)
            val captor = argumentCaptor<PaymentInfo>()

            controller.start()

            verify(cardReaderManager).collectPayment(captor.capture())
            assertThat(captor.firstValue.statementDescriptor.value).isEqualTo(expectedResult)
        }

    @Test
    fun `when initializing payment, then Loading state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(InitializingPayment) }
            }

            controller.start()

            assertThat(controller.paymentState.value).isInstanceOf(CardReaderPaymentState.LoadingData::class.java)
        }

    @Test
    fun `when collecting payment, then CollectingPayment state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.CollectingPayment.ExternalReaderCollectPaymentState::class.java)
        }

    @Test
    fun `given built in reader,when collecting payment, then CollectingPayment state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }
            createController(BUILT_IN)

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.CollectingPayment.BuiltInReaderCollectPaymentState::class.java)
        }

    @Test
    fun `when processing payment, then ProcessingPayment state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPayment) }
            }

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.ProcessingPayment.ExternalReaderProcessingPayment::class.java)
        }

    @Test
    fun `given built in reader, when processing payment, then ProcessingPayment state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPayment) }
            }
            createController(BUILT_IN)

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.ProcessingPayment.BuiltInReaderProcessingPayment::class.java)
        }

    @Test
    fun `when processing payment completed with card present, then tracking keeper stores payment type`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPaymentCompleted(PaymentMethodType.CARD_PRESENT)) }
            }

            controller.start()

            verify(cardReaderTrackingInfoKeeper).setPaymentMethodType("card")
        }

    @Test
    fun `when processing payment completed with interac present, then tracking keeper stores payment type`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPaymentCompleted(PaymentMethodType.INTERAC_PRESENT)) }
            }

            controller.start()

            verify(cardReaderTrackingInfoKeeper).setPaymentMethodType("card_interac")
        }

    @Test
    fun `when processing payment completed with interac present, then track interac success`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPaymentCompleted(PaymentMethodType.INTERAC_PRESENT)) }
            }

            controller.start()

            verify(tracker).trackInteracPaymentSucceeded()
        }

    @Test
    fun `when processing payment completed with card present, then do not track interac success`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPaymentCompleted(PaymentMethodType.CARD_PRESENT)) }
            }

            controller.start()

            verify(tracker, never()).trackInteracPaymentSucceeded()
        }

    @Test
    fun `when processing payment completed with unknown type, then do not track interac success`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPaymentCompleted(PaymentMethodType.UNKNOWN)) }
            }

            controller.start()

            verify(tracker, never()).trackInteracPaymentSucceeded()
        }

    @Test
    fun `when processing payment completed with unknown, then tracking keeper stores payment type`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPaymentCompleted(PaymentMethodType.UNKNOWN)) }
            }

            controller.start()

            verify(cardReaderTrackingInfoKeeper).setPaymentMethodType("unknown")
        }

    @Test
    fun `when capturing payment, then capturing payment state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CapturingPayment) }
            }

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.PaymentCapturing.ExternalReaderPaymentCapturing::class.java)
        }

    @Test
    fun `given built in reader, when capturing payment, then capturing payment state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CapturingPayment) }
            }
            createController(cardReaderType = BUILT_IN)

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.PaymentCapturing.BuiltInReaderPaymentCapturing::class.java)
        }

    @Test
    fun `given billing email empty, when external payment completed, then payment successful state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentCompleted("")) }
            }

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.PaymentSuccessful.ExternalReaderPaymentSuccessful::class.java)
        }

    @Test
    fun `given billing email empty, when built in payment completed, then payment successful state emitted`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentCompleted("")) }
            }
            createController(cardReaderType = BUILT_IN)

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.PaymentSuccessful.BuiltInReaderPaymentSuccessful::class.java)
        }

    @Test
    fun `given billing not empty, when external payment completed, then payment successful receipt sent state emitted`() =
        testBlocking {
            whenever(mockedAddress.email).thenReturn("nonemptyemail")
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentCompleted("")) }
            }

            controller.start()

            assertThat(controller.paymentState.value).isInstanceOf(
                CardReaderPaymentState.PaymentSuccessful.ExternalReaderPaymentSuccessfulReceiptSentAutomatically::class.java
            )
        }

    @Test
    fun `given billing not empty, when built in payment completed, then built in payment successful receipt sent state emitted`() =
        testBlocking {
            whenever(mockedAddress.email).thenReturn("nonemptyemail")
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentCompleted("")) }
            }
            createController(BUILT_IN)

            controller.start()

            assertThat(controller.paymentState.value).isInstanceOf(
                CardReaderPaymentState.PaymentSuccessful.BuiltInReaderPaymentSuccessfulReceiptSentAutomatically::class.java
            )
        }

    @Test
    fun `when payment completed, then success sound is played`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentCompleted("")) }
            }
            val events = mutableListOf<CardReaderPaymentEvent>()
            val job = launch {
                controller.event.collect {
                    events.add(it)
                }
            }
            controller.start()

            assertThat(events[0]).isInstanceOf(PlaySuccessfulPaymentSound::class.java)
            job.cancel()
        }

    @Test
    fun `when payment completed, then event tracked`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentCompleted("")) }
            }

            controller.start()

            verify(tracker).trackPaymentSucceeded()
        }

    @Test
    fun `given external reader, when payment fails, then failed state emitted`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(ExternalReaderFailedPayment::class.java)
        }

    @Test
    fun `given external reader fails with Unknown error, when flow starts, then CTA with contact support button is provided`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(Unknown)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }

            controller.start()

            val externalReaderFailedPaymentState =
                controller.paymentState.value as ExternalReaderFailedPayment
            assertThat(externalReaderFailedPaymentState.cta).isNotNull
            assertThat(externalReaderFailedPaymentState.cta!!.label).isEqualTo(R.string.support_contact)
        }

    @Test
    fun `given built in reader fails with Unknown error, when view model starts, then CTA with contact support button is provided`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, true))
                .thenReturn(Unknown)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }
            createController(BUILT_IN)

            controller.start()

            val state = controller.paymentState.value as BuiltInReaderFailedPayment
            assertThat(state.cta).isNotNull
            assertThat(state.cta!!.label).isEqualTo(R.string.support_contact)
        }

    @Test
    fun `given external reader fails with generic error, when contact support clicked, then contact support emitted and flow canceled`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Declined.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }

            controller.start()

            val events: List<CardReaderPaymentEvent> = controller.event.runAndCaptureValues {
                (controller.paymentState.value as ExternalReaderFailedPayment).cta!!.onCallToActionTapped()
            }

            assertThat(events[0]).isInstanceOf(CardReaderPaymentEvent.Exit::class.java)
            assertThat(events[1]).isInstanceOf(CardReaderPaymentEvent.ContactSupportTapped::class.java)
        }

    @Test
    fun `when contact support clicked, then contact support event tracked`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Declined.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }

            controller.start()

            (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).cta!!.onCallToActionTapped()

            verify(tracker).trackPaymentFailedContactSupportTapped()
        }

    @Test
    fun `given built in reader fails with generic error, when contact support clicked, then contact support emitted and flow canceled`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, true))
                .thenReturn(PaymentFlowError.Declined.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }
            createController(BUILT_IN)

            controller.start()

            val events = controller.event.runAndCaptureValues {
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).cta!!.onCallToActionTapped()
            }

            assertThat(events[0]).isInstanceOf(CardReaderPaymentEvent.Exit::class.java)
            assertThat(events[1]).isInstanceOf(CardReaderPaymentEvent.ContactSupportTapped::class.java)
        }

    @Test
    fun `given built in reader, when payment fails, then ui updated to built in failed state`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, true))
                .thenReturn(PaymentFlowError.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }
            createController(BUILT_IN)

            controller.start()

            assertThat(controller.paymentState.value)
                .isInstanceOf(CardReaderPaymentState.PaymentFailed.BuiltInReaderFailedPayment::class.java)
        }

    @Test
    fun `when payment fails, then invalidate onboarding cache`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }

            controller.start()

            verify(cardReaderOnboardingChecker).invalidateCache()
        }

    @Test
    fun `when payment fails, then event tracked`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }

            controller.start()

            verify(tracker).trackPaymentFailed(anyOrNull(), anyOrNull())
        }

    @Test
    fun `given external reader, when payment fails because of AMOUNT_TOO_SMALL, then failed state is not retryable`() =
        testBlocking {
            val error = AmountTooSmall(UiStringText("Amount must be at least US$0.50"))
            whenever(
                errorMapper.mapPaymentErrorToUiError(
                    DeclinedByBackendError.AmountTooSmall,
                    cardReaderConfig,
                    false
                )
            ).thenReturn(error)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithAmountTooSmall) }
            }

            controller.start()

            assertNull(
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry
            )
            assertNotNull(
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onCancel
            )
        }

    @Test
    fun `given external reader, when payment fails not because of AMOUNT_TOO_SMALL, then failed state is retryable`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Server(""), cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Server)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithServerError) }
            }

            controller.start()

            assertNotNull(
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry,
            )
        }

    @Test
    fun `given built in reader, when payment fails not because of AMOUNT_TOO_SMALL, then failed state has Try again button`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Server(""), cardReaderConfig, true))
                .thenReturn(PaymentFlowError.Server)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithServerError) }
            }
            createController(cardReaderType = BUILT_IN)

            controller.start()

            assertNotNull(
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry,
            )
        }

    @Test
    fun `given built in reader, when payment fails PIN_REQUIRED, then failed state has purchase card reader cta`() =
        testBlocking {
            whenever(
                errorMapper.mapPaymentErrorToUiError(
                    DeclinedByBackendError.CardDeclined.PinRequired,
                    cardReaderConfig,
                    true
                )
            ).thenReturn(PaymentFlowError.BuiltInReader.PinRequired)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentFailed(DeclinedByBackendError.CardDeclined.PinRequired, mock(), "dummy msg")) }
            }
            createController(BUILT_IN)

            controller.start()

            assertEquals(
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).cta!!.label,
                R.string.card_reader_payment_payment_failed_purchase_hardware_reader
            )
        }

    @Test
    fun `given built in reader, when purchase button clicked, then purchase even emmited`() =
        testBlocking {
            whenever(
                errorMapper.mapPaymentErrorToUiError(
                    DeclinedByBackendError.CardDeclined.PinRequired,
                    cardReaderConfig,
                    true
                )
            ).thenReturn(PaymentFlowError.BuiltInReader.PinRequired)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentFailed(DeclinedByBackendError.CardDeclined.PinRequired, mock(), "dummy msg")) }
            }
            whenever(wooStore.getStoreCountryCode(siteModel)).thenReturn("US")
            createController(cardReaderType = BUILT_IN)
            controller.start()
            val events = controller.event.runAndCaptureValues {
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).cta!!.onCallToActionTapped()
            }

            assertThat(events.last()).isInstanceOf(CardReaderPaymentEvent.PurchaseCardReaderTapped::class.java)
            assertThat((events.last() as CardReaderPaymentEvent.PurchaseCardReaderTapped).url).isEqualTo(
                "https://woocommerce.com/products/hardware/US"
            )
        }

    @Test
    fun `given external reader, when payment fails because of AMOUNT_TOO_SMALL, then clicking on ok button triggers exit event`() =
        testBlocking {
            val error = AmountTooSmall(UiStringText("Amount must be at least US$0.50"))
            whenever(
                errorMapper.mapPaymentErrorToUiError(
                    DeclinedByBackendError.AmountTooSmall,
                    cardReaderConfig,
                    false
                )
            ).thenReturn(error)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithAmountTooSmall) }
            }
            controller.start()
            val events = controller.event.runAndCaptureValues {
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onCancel!!()
            }

            assertThat(events.last()).isInstanceOf(CardReaderPaymentEvent.Exit::class.java)
        }

    @Test
    fun `given built in reader, when payment fails because of AMOUNT_TOO_SMALL, then clicking on ok button triggers exit event`() =
        testBlocking {
            val error = AmountTooSmall(UiStringText("Amount must be at least US$0.50"))
            whenever(
                errorMapper.mapPaymentErrorToUiError(
                    DeclinedByBackendError.AmountTooSmall,
                    cardReaderConfig,
                    true
                )
            ).thenReturn(error)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithAmountTooSmall) }
            }
            createController(cardReaderType = BUILT_IN)

            controller.start()
            val events = controller.event.runAndCaptureValues {
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onCancel!!()
            }

            assertThat(events.last()).isInstanceOf(CardReaderPaymentEvent.Exit::class.java)
        }

    @Test
    fun `given payment fails because of NFC_DISABLED, when primary button clicked, then tracked and enablenfc emitted`() =
        testBlocking {
            whenever(
                errorMapper.mapPaymentErrorToUiError(
                    CardPaymentStatus.CardPaymentStatusErrorType.BuiltInReader.NfcDisabled,
                    cardReaderConfig,
                    true
                )
            ).thenReturn(PaymentFlowError.BuiltInReader.NfcDisabled)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow {
                    emit(
                        PaymentFailed(
                            CardPaymentStatus.CardPaymentStatusErrorType.BuiltInReader.NfcDisabled,
                            null,
                            "message"
                        )
                    )
                }
            }
            createController(cardReaderType = BUILT_IN)

            controller.start()

            val events = controller.event.runAndCaptureValues {
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).cta!!.onCallToActionTapped()
            }

            assertThat(events.last()).isInstanceOf(CardReaderPaymentEvent.EnableNfcTapped::class.java)
            verify(tracker).trackPaymentFailedEnabledNfcTapped()
        }

    @Test
    fun `given user clicks on retry and external, when payment fails and retryData are null, then flow restarted from scratch`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }
            controller.start()
            clearInvocations(cardReaderManager)

            (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry!!()
            advanceUntilIdle()

            verify(cardReaderManager).collectPayment(any())
        }

    @Test
    fun `given user clicks on retry and built in, when payment fails and retryData are null, then flow restarted from scratch`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, true))
                .thenReturn(PaymentFlowError.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithEmptyDataForRetry) }
            }
            createController(cardReaderType = BUILT_IN)
            controller.start()
            clearInvocations(cardReaderManager)

            (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry!!()
            advanceUntilIdle()

            verify(cardReaderManager).collectPayment(any())
        }

    @Test
    fun `given failed payment and external reader, when user retries, then retryCollectPayment invoked`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithValidDataForRetry) }
            }
            controller.start()

            (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry!!()
            advanceUntilIdle()

            verify(cardReaderManager).retryCollectPayment(any(), any())
        }

    @Test
    fun `given failed payment and built-in reader, when user retries, then retryCollectPayment invoked`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, true))
                .thenReturn(PaymentFlowError.Generic)
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(paymentFailedWithValidDataForRetry) }
            }
            createController(cardReaderType = BUILT_IN)
            controller.start()

            (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry!!()
            advanceUntilIdle()

            verify(cardReaderManager).retryCollectPayment(any(), any())
        }

    @Test
    fun `given failed payment and external reader, when user retries, then flow retried with provided PaymentData`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Generic)
            val paymentData = mock<PaymentData>()
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentFailed(Generic, paymentData, "dummy msg")) }
            }
            controller.start()

            (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry!!()
            advanceUntilIdle()

            verify(cardReaderManager).retryCollectPayment(any(), eq(paymentData))
        }

    @Test
    fun `given failed payment and built-in reader, when user retries, then flow retried with provided PaymentData`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, true))
                .thenReturn(PaymentFlowError.Generic)
            val paymentData = mock<PaymentData>()
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentFailed(Generic, paymentData, "dummy msg")) }
            }
            createController(cardReaderType = BUILT_IN)
            controller.start()

            (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onRetry!!()
            advanceUntilIdle()

            verify(cardReaderManager).retryCollectPayment(any(), eq(paymentData))
        }

    @Test
    fun `given external failed payment, when user clicks on secondary button, then exit event is triggered`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, false))
                .thenReturn(PaymentFlowError.Generic)
            val paymentData = mock<PaymentData>()
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentFailed(Generic, paymentData, "dummy msg")) }
            }
            controller.start()

            val events = controller.event.runAndCaptureValues {
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onCancel!!()
            }

            assertThat(events.last()).isInstanceOf(CardReaderPaymentEvent.Exit::class.java)
        }

    @Test
    fun `given built in failed payment, when user clicks on secondary button, then exit event is triggered`() =
        testBlocking {
            whenever(errorMapper.mapPaymentErrorToUiError(Generic, cardReaderConfig, true))
                .thenReturn(PaymentFlowError.Generic)
            val paymentData = mock<PaymentData>()
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(PaymentFailed(Generic, paymentData, "dummy msg")) }
            }
            createController(cardReaderType = BUILT_IN)

            controller.start()

            val events = controller.event.runAndCaptureValues {
                (controller.paymentState.value as CardReaderPaymentState.PaymentFailed).onCancel!!()
            }

            assertThat(events.last()).isInstanceOf(CardReaderPaymentEvent.Exit::class.java)
        }

    @Test
    fun `when loading data, then cancellation is possible`() = testBlocking {
        controller.start()
        val paymentState = controller.paymentState.value

        assertThat(paymentState).isInstanceOf(CardReaderPaymentState.LoadingData::class.java)
        assertNotNull((paymentState as CardReaderPaymentState.LoadingData).onCancel)
    }

    @Test
    fun `when collecting payment, then cancellation is possible`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(CollectingPayment) }
            }

            controller.start()
            val paymentState = controller.paymentState.value as CardReaderPaymentState.CollectingPayment.ExternalReaderCollectPaymentState

            assertNotNull(paymentState.onCancel)
        }

    @Test
    fun `when processing payment, then cancellation is possible`() =
        testBlocking {
            whenever(cardReaderManager.collectPayment(any())).thenAnswer {
                flow { emit(ProcessingPayment) }
            }

            controller.start()
            val paymentState = controller.paymentState.value as CardReaderPaymentState.ProcessingPayment.ExternalReaderProcessingPayment
            assertNotNull(paymentState.onCancel)
        }

    companion object {
        private const val ORDER_ID = 1L
        private val siteModel = SiteModel().apply { name = "testName" }.apply { url = "testUrl.com" }
        private val DUMMY_TOTAL = BigDecimal(10.72)
        private const val DUMMY_CURRENCY_SYMBOL = "£"
    }
}
