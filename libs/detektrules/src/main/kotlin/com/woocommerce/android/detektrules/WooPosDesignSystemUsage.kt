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
import org.jetbrains.kotlin.psi.KtProperty

class WooPosDesignSystemUsage(config: Config) : Rule(config) {
    override val issue = Issue(
        javaClass.simpleName,
        Severity.Style,
        "Design system elements " +
            "(colors, elevations, margins, text styles) should be used from the declared design system.",
        Debt.TWENTY_MINS
    )

    private val allowedColors = setOf(
        "WooPosColors",
        "LightColorScheme",
        "DarkColorScheme",
        "LightCustomColors",
        "DarkCustomColors"
    )
    private val allowedElevations = setOf("WooPosElevation")
    private val allowedSpacing = setOf("WooPosSpacing")
    private val allowedTypography = setOf("WooPosTypography")

    private val targetPackagePrefix = "com.woocommerce.android.ui.woopos"

    override fun visitKtFile(file: KtFile) {
        if (file.packageFqName.asString().startsWith(targetPackagePrefix)) {
            super.visitKtFile(file)
        }
    }

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val calleeExpression = expression.calleeExpression as? KtNameReferenceExpression
        val callName = calleeExpression?.getReferencedName()

        if (callName == "Color" && expression.valueArguments.any {
                it.getArgumentExpression()?.text !in allowedColors
            }
        ) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Color should be used from the declared design system."
                )
            )
        }

        if (callName == "elevation" && expression.valueArguments.any {
                it.getArgumentExpression()?.text !in allowedElevations
            }
        ) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Elevation should be used from the declared design system."
                )
            )
        }

        if (callName in setOf("padding") && expression.valueArguments.any {
                it.getArgumentExpression()?.text !in allowedSpacing
            }
        ) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Margin/Padding should be used from the declared design system."
                )
            )
        }

        if (callName == "Text" && expression.valueArguments.any {
                it.getArgumentExpression()?.text !in allowedTypography
            }
        ) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Text style should be used from the declared design system."
                )
            )
        }
    }

    override fun visitProperty(property: KtProperty) {
        super.visitProperty(property)

        if (property.typeReference?.text == "Color" && property.initializer?.text !in allowedColors) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(property),
                    "Color should be used from the declared design system."
                )
            )
        }
    }
}
