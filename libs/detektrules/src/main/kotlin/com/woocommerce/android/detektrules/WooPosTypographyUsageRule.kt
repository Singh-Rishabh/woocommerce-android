package com.woocommerce.android.detektrules

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

class WooPosTypographyUsageRule(config: Config) : Rule(config) {
    private val targetPackagePrefix = "com.woocommerce.android.ui.woopos"
    private val typographyFile = "WooPosTypography"

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Use text styles from $typographyFile instead of hardcoded styles.",
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

        if (callName == "Text") {
            val hasValidStyle = expression.valueArguments.any { argument ->
                val argumentExpression = argument.getArgumentExpression()
                val argumentText = argumentExpression?.text ?: return@any false

                argumentText.startsWith(typographyFile)
            }

            if (!hasValidStyle) {
                report(
                    CodeSmell(
                        issue,
                        Entity.from(expression),
                        "Text should use styles from WooPosTypography instead of hardcoded values or defaults."
                    )
                )
            }
        }
    }
}
