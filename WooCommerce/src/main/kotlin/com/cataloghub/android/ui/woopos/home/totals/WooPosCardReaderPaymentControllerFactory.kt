package com.cataloghub.android.ui.woopos.home.totals

import com.cataloghub.android.AppPrefs
import com.cataloghub.android.cardreader.CardReaderManager
import com.cataloghub.android.di.PointOfSaleMode
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.orders.details.OrderDetailRepository
import com.cataloghub.android.ui.payments.cardreader.CardReaderCountryConfigProvider
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderFlowParam.PaymentOrRefund
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderFlowParam.PaymentOrRefund.Payment.PaymentType
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderOnboardingChecker
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderType
import com.cataloghub.android.ui.payments.cardreader.payment.CardReaderInteracRefundErrorMapper
import com.cataloghub.android.ui.payments.cardreader.payment.CardReaderInteracRefundableChecker
import com.cataloghub.android.ui.payments.cardreader.payment.CardReaderPaymentCollectibilityChecker
import com.cataloghub.android.ui.payments.cardreader.payment.CardReaderPaymentErrorMapper
import com.cataloghub.android.ui.payments.cardreader.payment.CardReaderPaymentOrderHelper
import com.cataloghub.android.ui.payments.cardreader.payment.controller.CardReaderPaymentController
import com.cataloghub.android.ui.payments.cardreader.payment.controller.CardReaderPaymentStateProvider
import com.cataloghub.android.ui.payments.cardreader.payment.controller.CardReaderTrackCanceledFlowAction
import com.cataloghub.android.ui.payments.receipt.PaymentReceiptHelper
import com.cataloghub.android.ui.payments.receipt.PaymentReceiptShare
import com.cataloghub.android.ui.payments.tracking.CardReaderTrackingInfoKeeper
import com.cataloghub.android.ui.payments.tracking.PaymentsFlowTracker
import com.cataloghub.android.util.CoroutineDispatchers
import com.cataloghub.android.util.CurrencyFormatter
import org.wordpress.android.fluxc.store.WooCommerceStore
import javax.inject.Inject
import kotlin.reflect.KMutableProperty0

class WooPosCardReaderPaymentControllerFactory @Inject constructor(
    private val cardReaderManager: CardReaderManager,
    private val orderRepository: OrderDetailRepository,
    private val selectedSite: SelectedSite,
    private val appPrefs: AppPrefs = AppPrefs,
    private val paymentCollectibilityChecker: CardReaderPaymentCollectibilityChecker,
    private val interacRefundableChecker: CardReaderInteracRefundableChecker,
    @PointOfSaleMode private val tracker: PaymentsFlowTracker,
    @PointOfSaleMode private val trackCancelledFlow: CardReaderTrackCanceledFlowAction,
    private val currencyFormatter: CurrencyFormatter,
    private val errorMapper: CardReaderPaymentErrorMapper,
    private val interacRefundErrorMapper: CardReaderInteracRefundErrorMapper,
    private val wooStore: WooCommerceStore,
    private val dispatchers: CoroutineDispatchers,
    private val cardReaderTrackingInfoKeeper: CardReaderTrackingInfoKeeper,
    private val paymentStateProvider: CardReaderPaymentStateProvider,
    private val cardReaderPaymentOrderHelper: CardReaderPaymentOrderHelper,
    private val paymentReceiptHelper: PaymentReceiptHelper,
    private val cardReaderOnboardingChecker: CardReaderOnboardingChecker,
    private val cardReaderConfigProvider: CardReaderCountryConfigProvider,
    private val paymentReceiptShare: PaymentReceiptShare,
) {
    fun create(
        orderId: Long,
        paymentType: PaymentType,
        isTTPPaymentInProgress: KMutableProperty0<Boolean>,
    ): CardReaderPaymentController = CardReaderPaymentController(
        cardReaderManager = cardReaderManager,
        orderRepository = orderRepository,
        selectedSite = selectedSite,
        appPrefs = appPrefs,
        paymentCollectibilityChecker = paymentCollectibilityChecker,
        interacRefundableChecker = interacRefundableChecker,
        tracker = tracker,
        trackCancelledFlow = trackCancelledFlow,
        currencyFormatter = currencyFormatter,
        errorMapper = errorMapper,
        interacRefundErrorMapper = interacRefundErrorMapper,
        wooStore = wooStore,
        dispatchers = dispatchers,
        cardReaderTrackingInfoKeeper = cardReaderTrackingInfoKeeper,
        paymentStateProvider = paymentStateProvider,
        cardReaderPaymentOrderHelper = cardReaderPaymentOrderHelper,
        paymentReceiptHelper = paymentReceiptHelper,
        cardReaderOnboardingChecker = cardReaderOnboardingChecker,
        cardReaderConfigProvider = cardReaderConfigProvider,
        paymentReceiptShare = paymentReceiptShare,
        paymentOrRefund = PaymentOrRefund.Payment(
            orderId = orderId,
            paymentType = paymentType
        ),
        cardReaderType = CardReaderType.EXTERNAL,
        isTTPPaymentInProgress = isTTPPaymentInProgress,
    )
}
