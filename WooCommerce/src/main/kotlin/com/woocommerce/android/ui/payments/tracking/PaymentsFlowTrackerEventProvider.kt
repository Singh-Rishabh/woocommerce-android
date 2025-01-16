package com.woocommerce.android.ui.payments.tracking

import com.woocommerce.android.analytics.IAnalyticsEvent

interface PaymentsFlowTrackerEventProvider {
    val CARD_PRESENT_ONBOARDING_LEARN_MORE_TAPPED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_COMPLETED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_NOT_COMPLETED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_STEP_SKIPPED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_CTA_TAPPED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_CTA_FAILED: IAnalyticsEvent
    val PAYMENTS_HUB_CASH_ON_DELIVERY_TOGGLED: IAnalyticsEvent
    val ENABLE_CASH_ON_DELIVERY_SUCCESS: IAnalyticsEvent
    val ENABLE_CASH_ON_DELIVERY_FAILED: IAnalyticsEvent
    val DISABLE_CASH_ON_DELIVERY_SUCCESS: IAnalyticsEvent
    val DISABLE_CASH_ON_DELIVERY_FAILED: IAnalyticsEvent
    val PAYMENTS_HUB_CASH_ON_DELIVERY_TOGGLED_LEARN_MORE_TAPPED: IAnalyticsEvent
    val CARD_PRESENT_PAYMENT_GATEWAY_SELECTED: IAnalyticsEvent
    val CARD_READER_SOFTWARE_UPDATE_STARTED: IAnalyticsEvent
}
