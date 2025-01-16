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
}
