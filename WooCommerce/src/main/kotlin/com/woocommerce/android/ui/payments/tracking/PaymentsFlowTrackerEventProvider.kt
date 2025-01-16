package com.woocommerce.android.ui.payments.tracking

import com.woocommerce.android.analytics.IAnalyticsEvent

interface PaymentsFlowTrackerEventProvider {
    val CARD_PRESENT_ONBOARDING_LEARN_MORE_TAPPED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_COMPLETED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_NOT_COMPLETED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_STEP_SKIPPED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_CTA_TAPPED: IAnalyticsEvent
    val CARD_PRESENT_ONBOARDING_CTA_FAILED: IAnalyticsEvent
}
