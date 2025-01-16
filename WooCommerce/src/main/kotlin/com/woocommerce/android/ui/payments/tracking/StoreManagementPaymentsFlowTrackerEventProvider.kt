package com.woocommerce.android.ui.payments.tracking

import com.woocommerce.android.analytics.AnalyticsEvent
import com.woocommerce.android.analytics.IAnalyticsEvent

class StoreManagementPaymentsFlowTrackerEventProvider : PaymentsFlowTrackerEventProvider {
    override val CARD_PRESENT_ONBOARDING_LEARN_MORE_TAPPED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_PRESENT_ONBOARDING_LEARN_MORE_TAPPED

    override val CARD_PRESENT_ONBOARDING_COMPLETED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_PRESENT_ONBOARDING_COMPLETED

    override val CARD_PRESENT_ONBOARDING_NOT_COMPLETED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_PRESENT_ONBOARDING_NOT_COMPLETED

    override val CARD_PRESENT_ONBOARDING_STEP_SKIPPED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_PRESENT_ONBOARDING_STEP_SKIPPED

    override val CARD_PRESENT_ONBOARDING_CTA_TAPPED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_PRESENT_ONBOARDING_CTA_TAPPED

    override val CARD_PRESENT_ONBOARDING_CTA_FAILED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_PRESENT_ONBOARDING_CTA_FAILED

    override val PAYMENTS_HUB_CASH_ON_DELIVERY_TOGGLED: IAnalyticsEvent
        get() = AnalyticsEvent.PAYMENTS_HUB_CASH_ON_DELIVERY_TOGGLED

    override val ENABLE_CASH_ON_DELIVERY_SUCCESS: IAnalyticsEvent
        get() = AnalyticsEvent.ENABLE_CASH_ON_DELIVERY_SUCCESS

    override val ENABLE_CASH_ON_DELIVERY_FAILED: IAnalyticsEvent
        get() = AnalyticsEvent.ENABLE_CASH_ON_DELIVERY_FAILED

    override val DISABLE_CASH_ON_DELIVERY_SUCCESS: IAnalyticsEvent
        get() = AnalyticsEvent.DISABLE_CASH_ON_DELIVERY_SUCCESS

    override val DISABLE_CASH_ON_DELIVERY_FAILED: IAnalyticsEvent
        get() = AnalyticsEvent.DISABLE_CASH_ON_DELIVERY_FAILED

    override val PAYMENTS_HUB_CASH_ON_DELIVERY_TOGGLED_LEARN_MORE_TAPPED: IAnalyticsEvent
        get() = AnalyticsEvent.PAYMENTS_HUB_CASH_ON_DELIVERY_TOGGLED_LEARN_MORE_TAPPED

    override val CARD_PRESENT_PAYMENT_GATEWAY_SELECTED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_PRESENT_PAYMENT_GATEWAY_SELECTED

    override val CARD_READER_SOFTWARE_UPDATE_STARTED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_SOFTWARE_UPDATE_STARTED

    override val CARD_READER_SOFTWARE_UPDATE_ALERT_SHOWN: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_SOFTWARE_UPDATE_ALERT_SHOWN

    override val CARD_READER_SOFTWARE_UPDATE_ALERT_INSTALL_CLICKED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_SOFTWARE_UPDATE_ALERT_INSTALL_CLICKED

    override val CARD_READER_SOFTWARE_UPDATE_FAILED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_SOFTWARE_UPDATE_FAILED

    override val CARD_READER_SOFTWARE_UPDATE_SUCCESS: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_SOFTWARE_UPDATE_SUCCESS

    override val CARD_READER_DISCOVERY_READER_DISCOVERED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_DISCOVERY_READER_DISCOVERED

    override val CARD_READER_DISCOVERY_FAILED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_DISCOVERY_FAILED

    override val CARD_READER_DISCOVERY_TAPPED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_DISCOVERY_TAPPED

    override val CARD_READER_AUTO_CONNECTION_STARTED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_AUTO_CONNECTION_STARTED

    override val CARD_READER_CONNECTION_TAPPED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_CONNECTION_TAPPED

    override val CARD_READER_LOCATION_SUCCESS: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_READER_LOCATION_SUCCESS

}
