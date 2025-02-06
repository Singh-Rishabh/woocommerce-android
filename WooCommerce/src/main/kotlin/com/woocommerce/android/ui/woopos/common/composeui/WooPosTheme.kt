@file:Suppress("MagicNumber")

package com.woocommerce.android.ui.woopos.common.composeui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.woocommerce.android.ui.compose.theme.WooTypography

data class CustomColors(
    val loadingSkeleton: Color,
    val border: Color,
    val success: Color,
    val error: Color,
    val totalsBackground: Color,
    val totalsErrorBackground: Color,
    val paymentSuccessBackground: Color,
    val paymentProcessingBackground: Color,
    val paymentSuccessText: Color,
    val paymentSuccessIcon: Color,
    val paymentProcessingText: Color,
    val dialogSubtitleHighlightBackground: Color = WooPosColors.lightQuaternaryBackground,
    val homeBackground: Color,
)

private object WooPosColors {
    // Woo POS specific colors:

    // Adding missing colors from the old code to match exactly
    val oldGrayLight = Color(0xFFF2EBFF)
    val oldGrayMedium = Color(0xFF8D8D8D)

    val primaryVariant = Color(0xFF3700B3)
    val secondary = Color(0xFF0A9400)
    val surface = Color(0xFF2E2E2E)

    // LightColorPalette
    val lightColorPaletteSecondary = Color(0xFF004B3E)
    val lightColorPaletteSecondaryVariant = Color(0xFF50575E)
    val lightColorPaletteBackground = Color(0xFFFDFDFD)

    // DarkCustomColors
    val darkCustomColorsError = Color(0xFFBE4400)
    val darkCustomloadingSkeleton = Color(0xFF616161)
    val darkCustomColorsHomeBackground = Color(0xFF1E1E1E)
    val darkQuaternaryBackground = Color(0xFF111111)
    val darkTotalsBackground = Color(0xFF1C1C1E)

    // LightCustomColors
    val lightCustomColorsError = Color(0xFFF16618)
    val lightCustomColorsLoadingSkeleton = Color(0xFFE1E1E1)
    val lightCustomColorsBorder = Color(0xFFC6C6C8)
    val lightQuaternaryBackground = Color(0x14747480)

    val WooPurple30 = Color(0xFFA77EFF)
    val WooPurple40 = Color(0xFF873EFF)
    val WooPurple70 = Color(0xFF5007AA)
    val WooPurple90 = Color(0xFF2C045D)

    val emerald20 = Color(0xFF98F179)
    val emerald60 = Color(0xFF028C59)

    val White = Color(0xFFFFFFFF)

    val Gray0 = Color(0xFFF6F7F7)

    val Black90 = Color(0xFF121212)
}

private val DarkColorPalette = darkColors(
    primary = WooPosColors.WooPurple30,
    primaryVariant = WooPosColors.primaryVariant,
    onPrimary = Color.Black,
    secondary = WooPosColors.secondary,
    secondaryVariant = WooPosColors.oldGrayMedium,
    surface = WooPosColors.surface,
    onSurface = Color.White,
    background = WooPosColors.Black90,
    onBackground = Color.White,
)

private val LightColorPalette = lightColors(
    primary = WooPosColors.WooPurple40,
    primaryVariant = WooPosColors.primaryVariant,
    onPrimary = Color.White,
    secondary = WooPosColors.lightColorPaletteSecondary,
    secondaryVariant = WooPosColors.lightColorPaletteSecondaryVariant,
    surface = Color.White,
    onSurface = Color.Black,
    background = WooPosColors.lightColorPaletteBackground,
    onBackground = Color.Black,
)

private val DarkCustomColors = CustomColors(
    loadingSkeleton = WooPosColors.darkCustomloadingSkeleton,
    border = WooPosColors.oldGrayMedium,
    success = WooPosColors.emerald60,
    error = WooPosColors.darkCustomColorsError,
    totalsErrorBackground = WooPosColors.darkQuaternaryBackground,
    totalsBackground = WooPosColors.darkTotalsBackground,
    paymentSuccessBackground = WooPosColors.darkCustomColorsHomeBackground,
    paymentSuccessText = WooPosColors.oldGrayLight,
    paymentSuccessIcon = WooPosColors.darkCustomColorsHomeBackground,
    paymentProcessingText = WooPosColors.White,
    homeBackground = WooPosColors.darkCustomColorsHomeBackground,
    paymentProcessingBackground = WooPosColors.WooPurple70,
)

private val LightCustomColors = CustomColors(
    loadingSkeleton = WooPosColors.lightCustomColorsLoadingSkeleton,
    border = WooPosColors.lightCustomColorsBorder,
    success = WooPosColors.emerald20,
    error = WooPosColors.lightCustomColorsError,
    totalsErrorBackground = WooPosColors.lightQuaternaryBackground,
    totalsBackground = WooPosColors.Gray0,
    paymentSuccessBackground = WooPosColors.White,
    paymentSuccessText = WooPosColors.WooPurple90,
    paymentProcessingText = WooPosColors.White,
    paymentSuccessIcon = Color.White,
    homeBackground = WooPosColors.Gray0,
    paymentProcessingBackground = WooPosColors.WooPurple70,
)

private val LocalCustomColors = staticCompositionLocalOf {
    LightCustomColors
}

@Composable
fun WooPosTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) {
        DarkColorPalette
    } else {
        LightColorPalette
    }

    val customColors = if (isSystemInDarkTheme()) {
        DarkCustomColors
    } else {
        LightCustomColors
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colors = colors,
            typography = WooTypography,
        ) {
            SurfacedContent(content)
        }
    }
}

@Composable
private fun SurfacedContent(
    content: @Composable () -> Unit
) {
    Surface(color = MaterialTheme.colors.background) {
        content()
    }
}

object WooPosTheme {
    val colors: CustomColors
        @Composable get() = LocalCustomColors.current
}
