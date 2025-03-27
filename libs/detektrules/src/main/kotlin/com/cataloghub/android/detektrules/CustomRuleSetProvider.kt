package com.cataloghub.android.detektrules

import com.cataloghub.android.detektrules.woopos.WooPosDesignSystemButtonUsageRule
import com.cataloghub.android.detektrules.woopos.WooPosDesignSystemColorUsageRule
import com.cataloghub.android.detektrules.woopos.WooPosDesignSystemCornerRadiusUsageRule
import com.cataloghub.android.detektrules.woopos.WooPosDesignSystemSpacingUsageRule
import com.cataloghub.android.detektrules.woopos.WooPosDesignSystemTextUsageRule
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CustomRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "WooPosRules"

    override fun instance(config: Config) = RuleSet(
        ruleSetId,
        listOf(
            WooPosDesignSystemSpacingUsageRule(config),
            WooPosDesignSystemCornerRadiusUsageRule(config),
            WooPosDesignSystemColorUsageRule(config),
            WooPosDesignSystemTextUsageRule(config),
            WooPosDesignSystemButtonUsageRule(config),
        )
    )
}
