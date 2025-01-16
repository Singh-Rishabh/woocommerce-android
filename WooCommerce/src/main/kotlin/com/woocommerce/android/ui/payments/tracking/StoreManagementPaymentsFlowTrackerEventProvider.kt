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
}
