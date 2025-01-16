package com.woocommerce.android.ui.woopos.util.analytics

import com.woocommerce.android.analytics.IAnalyticsEvent
import com.woocommerce.android.ui.payments.tracking.PaymentsFlowTrackerEventProvider
import com.woocommerce.android.ui.woopos.util.analytics.WooPosAnalyticsEvent.PaymentFlowTrackerEvent

class WooPosPaymentsFlowTrackerEventProvider : PaymentsFlowTrackerEventProvider {
    override val CARD_PRESENT_ONBOARDING_LEARN_MORE_TAPPED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardPresentOnboardingLearnMoreTapped

    override val CARD_PRESENT_ONBOARDING_COMPLETED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardPresentOnboardingCompleted

    override val CARD_PRESENT_ONBOARDING_NOT_COMPLETED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardPresentOnboardingNotCompleted

    override val CARD_PRESENT_ONBOARDING_STEP_SKIPPED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardPresentOnboardingStepSkipped

    override val CARD_PRESENT_ONBOARDING_CTA_TAPPED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardPresentOnboardingCtaTapped

    override val CARD_PRESENT_ONBOARDING_CTA_FAILED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardPresentOnboardingCtaFailed

    override val PAYMENTS_HUB_CASH_ON_DELIVERY_TOGGLED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.PaymentsHubCashOnDeliveryToggled

    override val ENABLE_CASH_ON_DELIVERY_SUCCESS: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.EnableCashOnDeliverySuccess

    override val ENABLE_CASH_ON_DELIVERY_FAILED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.EnableCashOnDeliveryFailed

    override val DISABLE_CASH_ON_DELIVERY_SUCCESS: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.DisableCashOnDeliverySuccess

    override val DISABLE_CASH_ON_DELIVERY_FAILED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.DisableCashOnDeliveryFailed

    override val PAYMENTS_HUB_CASH_ON_DELIVERY_TOGGLED_LEARN_MORE_TAPPED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.PaymentsHubCashOnDeliveryToggledLearnMoreTapped

    override val CARD_PRESENT_PAYMENT_GATEWAY_SELECTED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardPresentPaymentGatewaySelected

    override val CARD_READER_SOFTWARE_UPDATE_STARTED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderSoftwareUpdateStarted

    override val CARD_READER_SOFTWARE_UPDATE_ALERT_SHOWN: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderSoftwareUpdateAlertShown

    override val CARD_READER_SOFTWARE_UPDATE_ALERT_INSTALL_CLICKED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderSoftwareUpdateAlertInstallClicked

    override val CARD_READER_SOFTWARE_UPDATE_FAILED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderSoftwareUpdateFailed

    override val CARD_READER_SOFTWARE_UPDATE_SUCCESS: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderSoftwareUpdateSuccess

    override val CARD_READER_DISCOVERY_READER_DISCOVERED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderDiscoveryReaderDiscovered

    override val CARD_READER_DISCOVERY_FAILED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderDiscoveryFailed

    override val CARD_READER_DISCOVERY_TAPPED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderDiscoveryTapped

    override val CARD_READER_AUTO_CONNECTION_STARTED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderAutoConnectionStarted

    override val CARD_READER_CONNECTION_TAPPED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderConnectionTapped

    override val CARD_READER_LOCATION_SUCCESS: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardReaderLocationSuccess

}
