package com.woocommerce.android.ui.payments.tracking

import com.woocommerce.android.analytics.AnalyticsEvent
import com.woocommerce.android.analytics.IAnalyticsEvent

class StoreManagementPaymentsFlowTrackerEventProvider : PaymentsFlowTrackerEventProvider {
    override val CARD_PRESENT_ONBOARDING_LEARN_MORE_TAPPED: IAnalyticsEvent
        get() = AnalyticsEvent.CARD_PRESENT_ONBOARDING_LEARN_MORE_TAPPED
}
