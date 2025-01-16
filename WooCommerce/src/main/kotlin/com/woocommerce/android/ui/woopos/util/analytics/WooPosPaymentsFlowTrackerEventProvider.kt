package com.woocommerce.android.ui.woopos.util.analytics

import com.woocommerce.android.analytics.IAnalyticsEvent
import com.woocommerce.android.ui.payments.tracking.PaymentsFlowTrackerEventProvider
import com.woocommerce.android.ui.woopos.util.analytics.WooPosAnalyticsEvent.PaymentFlowTrackerEvent

class WooPosPaymentsFlowTrackerEventProvider : PaymentsFlowTrackerEventProvider {
    override val CARD_PRESENT_ONBOARDING_LEARN_MORE_TAPPED: IAnalyticsEvent
        get() = PaymentFlowTrackerEvent.CardPresentOnboardingLearnMoreTapped
}
