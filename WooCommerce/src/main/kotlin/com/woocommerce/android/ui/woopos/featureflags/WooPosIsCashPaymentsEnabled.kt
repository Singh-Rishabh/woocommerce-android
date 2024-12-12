package com.woocommerce.android.ui.woopos.featureflags

import com.woocommerce.android.util.FeatureFlag
import javax.inject.Inject

class WooPosIsCashPaymentsEnabled @Inject constructor() {
    operator fun invoke(): Boolean {
        return FeatureFlag.POS_CASH_PAYMENTS.isEnabled()
    }
}
