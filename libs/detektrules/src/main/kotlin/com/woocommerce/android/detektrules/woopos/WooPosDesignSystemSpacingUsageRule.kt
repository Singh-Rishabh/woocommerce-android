package com.woocommerce.android.detektrules.woopos

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression

class WooPosDesignSystemSpacingUsageRule(config: Config) : Rule(config) {
    private val targetPackagePrefix = "com.woocommerce.android.ui.woopos"

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Use spacing from the WooPosSpacing design system.",
        Debt.FIVE_MINS
    )

    override fun visitKtFile(file: KtFile) {
        if (file.packageFqName.asString().startsWith(targetPackagePrefix)) {
            super.visitKtFile(file)
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val calleeExpression = expression.calleeExpression as? KtNameReferenceExpression
        val callName = calleeExpression?.getReferencedName()

        if (callName == "padding") {
            expression.valueArguments.forEach { argument ->
                val argumentExpression = argument.getArgumentExpression()
                val argumentText = argumentExpression?.text ?: return@forEach

                if (!argumentText.startsWith("WooPosSpacing") && argumentText.matches(Regex("\\d+\\.dp"))) {
                    report(
                        CodeSmell(
                            issue,
                            Entity.from(expression),
                            "Use WooPosSpacing for padding/margins instead of hardcoded values. Found: $argumentText"
                        )
                    )
                }
            }
        } else if (callName == "Spacer") {
            expression.valueArguments.forEach { argument ->
                val argumentExpression = argument.getArgumentExpression()
                val argumentText = argumentExpression?.text ?: return@forEach

                val hardcodedDpRegex = Regex("\\b\\d+\\.dp\\b")
                val matches = hardcodedDpRegex.findAll(argumentText)
                for (match in matches) {
                    if (!argumentText.contains("WooPosSpacing")) {
                        report(
                            CodeSmell(
                                issue,
                                Entity.from(expression),
                                "Use WooPosSpacing for Spacer dimensions instead of hardcoded values. " +
                                        "Found: ${match.value}"
                            )
                        )
                    }
                }
            }
        }
    }
}
