package com.woocommerce.android.ui.payments.cardreader.payment

import com.woocommerce.android.AppPrefs
import com.woocommerce.android.cardreader.CardReaderManager
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.details.OrderDetailRepository
import com.woocommerce.android.ui.payments.cardreader.CardReaderCountryConfigProvider
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderFlowParam
import com.woocommerce.android.ui.payments.cardreader.onboarding.CardReaderOnboardingChecker
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptHelper
import com.woocommerce.android.ui.payments.receipt.PaymentReceiptShare
import com.woocommerce.android.ui.payments.tracking.CardReaderTrackingInfoKeeper
import com.woocommerce.android.ui.payments.tracking.PaymentsFlowTracker
import com.woocommerce.android.util.CoroutineDispatchers
import com.woocommerce.android.util.CurrencyFormatter
import kotlinx.coroutines.CoroutineScope
import org.wordpress.android.fluxc.store.WooCommerceStore

class CardReaderPaymentController(
    private val scope: CoroutineScope,
    private val cardReaderManager: CardReaderManager,
    private val orderRepository: OrderDetailRepository,
    private val selectedSite: SelectedSite,
    private val appPrefs: AppPrefs = AppPrefs,
    private val paymentCollectibilityChecker: CardReaderPaymentCollectibilityChecker,
    private val interacRefundableChecker: CardReaderInteracRefundableChecker,
    private val tracker: PaymentsFlowTracker,
    private val currencyFormatter: CurrencyFormatter,
    private val errorMapper: CardReaderPaymentErrorMapper,
    private val interacRefundErrorMapper: CardReaderInteracRefundErrorMapper,
    private val wooStore: WooCommerceStore,
    private val dispatchers: CoroutineDispatchers,
    private val cardReaderTrackingInfoKeeper: CardReaderTrackingInfoKeeper,
    private val cardReaderPaymentReaderTypeStateProvider: CardReaderPaymentReaderTypeStateProvider,
    private val cardReaderPaymentOrderHelper: CardReaderPaymentOrderHelper,
    private val paymentReceiptHelper: PaymentReceiptHelper,
    private val cardReaderOnboardingChecker: CardReaderOnboardingChecker,
    private val cardReaderConfigProvider: CardReaderCountryConfigProvider,
    private val paymentReceiptShare: PaymentReceiptShare,
    private val paymentOrRefund: CardReaderFlowParam.PaymentOrRefund,
    private val isTTPPaymentInProgress: Boolean,
) {

}