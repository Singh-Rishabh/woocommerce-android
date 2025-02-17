package com.woocommerce.android.detektrules.woopos

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtValueArgument

class WooPosDesignSystemColorUsageRule(config: Config) : Rule(config) {
    private val targetPackagePrefix = "com.woocommerce.android.ui.woopos"
    private val allowedColorSources = setOf("MaterialTheme.colorScheme", "WooPosTheme.colors")

    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Use colors from WooPosTheme.colors or MaterialTheme.colorScheme instead of hardcoded colors.",
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

        val colorArguments = setOf(
            "background",
            "containerColor",
            "contentColor",
            "borderColor",
            "tint",
            "indicatorColor"
        )

        if (callName in colorArguments) {
            expression.valueArguments.forEach { argument ->
                checkColorArgument(argument)
            }
        }

        if (expression.parent is KtBinaryExpression) {
            checkColorAssignment(expression)
        }
    }

    private fun checkColorArgument(argument: KtValueArgument) {
        val argumentExpression = argument.getArgumentExpression()
        val argumentText = argumentExpression?.text ?: return

        if (!isAllowedColor(argumentText)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(argument),
                    "Colors should be used from WooPosTheme.colors or MaterialTheme.colorScheme. Found: $argumentText"
                )
            )
        }
    }

    private fun checkColorAssignment(expression: KtCallExpression) {
        val argumentText = expression.text

        if (!isAllowedColor(argumentText)) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Color assignments should use WooPosTheme.colors or MaterialTheme.colorScheme. Found: $argumentText"
                )
            )
        }
    }

    private fun isAllowedColor(colorText: String): Boolean {
        return allowedColorSources.any { colorText.startsWith(it) }
    }
}
