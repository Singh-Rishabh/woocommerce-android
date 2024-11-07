package com.woocommerce.android.ui.woopos.featureflags

import com.woocommerce.android.util.FeatureFlag
import javax.inject.Inject

class WooPosIsPaymentsOnboardingSupportedInternally @Inject constructor() {
    operator fun invoke(): Boolean = FeatureFlag.WOO_POS_PAYMENTS_ONBOARDING.isEnabled()
}
