package com.woocommerce.android.detektrules

import com.woocommerce.android.detektrules.woopos.WooPosCornerRadiusUsageRule
import com.woocommerce.android.detektrules.woopos.WooPosDesignSystemUsageSpacing
import com.woocommerce.android.detektrules.woopos.WooPosTypographyUsageRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CustomRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "WooPosRules"

    override fun instance(config: Config) = RuleSet(
        ruleSetId,
        listOf(
            WooPosDesignSystemUsageSpacing(config),
            WooPosTypographyUsageRule(config),
            WooPosCornerRadiusUsageRule(config),
        )
    )
}
