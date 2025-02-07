@file:Suppress("MagicNumber")

package com.woocommerce.android.ui.woopos.common.composeui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class CustomColors(
    val loadingSkeleton: Color,
    val border: Color,

    val shadow: Color = WooPosColors.Black,

    val success: Color = WooPosColors.VividGreen,
    val onSuccess: Color = WooPosColors.White,

    val alert: Color = WooPosColors.Amber,
    val onAlert: Color = WooPosColors.White,

    val disabledContainer: Color,
    val onDisabledContainer: Color,

    val outline: Color,
    val outlineVariable: Color,

    val onSurfaceVariantLow: Color,
    val onSurfaceVariantHigh: Color,

    val fontPrimary: Color,
    val fontSecondary: Color,
    val fontTertiary: Color,

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

object WooPosColors {
    // Woo POS specific colors:

    // Adding missing colors from the old code to match exactly
    val oldGrayLight = Color(0xFFF2EBFF)
    val oldGrayMedium = Color(0xFF8D8D8D)

    // DarkCustomColors
    val darkCustomLoadingSkeleton = Color(0xFF616161)
    val darkCustomColorsHomeBackground = Color(0xFF1E1E1E)
    val darkQuaternaryBackground = Color(0xFF111111)
    val darkTotalsBackground = Color(0xFF1C1C1E)

    // LightCustomColors
    val lightCustomColorsLoadingSkeleton = Color(0xFFE1E1E1)
    val lightCustomColorsBorder = Color(0xFFC6C6C8)
    val lightQuaternaryBackground = Color(0x14747480)

    val WooPurple40 = Color(0xFF873EFF)
    val WooPurple70 = Color(0xFF5007AA)
    val WooPurple90 = Color(0xFF2C045D)

    val WooRed50 = Color(0xFFD63638)

    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)

    val Amber = Color(0xFFF16618)

    val VividGreen = Color(0xFF06E782)

    val Gray0 = Color(0xFFF6F7F7)
    val Gray5 = Color(0xFFDCDCDE)
    val Gray20 = Color(0xFFA7AAAD)
    val Gray30 = Color(0xFF8C8F94)
    val Gray40 = Color(0xFF787C82)
    val Gray50 = Color(0xFF646970)
    val Gray60 = Color(0xFF50575E)
    val Gray70 = Color(0xFF3C434A)
    val Gray80 = Color(0xFF373A3E)
    val Gray90 = Color(0xFF292B2E)
    val Gray100 = Color(0xFF101517)
}

private val LightColorScheme = lightColorScheme(
    primary = WooPosColors.WooPurple40,
    onPrimary = Color.White,

    primaryContainer = WooPosColors.WooPurple40,
    onPrimaryContainer = WooPosColors.White,

    secondaryContainer = WooPosColors.Black,
    onSecondaryContainer = WooPosColors.White,

    surface = WooPosColors.Gray0,
    surfaceDim = WooPosColors.Gray80,
    surfaceBright = WooPosColors.Gray90,

    surfaceContainerLowest = WooPosColors.White,
    surfaceContainerLow = WooPosColors.White,
    surfaceContainerHigh = WooPosColors.Gray30,

    onSurface = WooPosColors.Gray100,

    inverseSurface = WooPosColors.Gray100,
    inverseOnSurface = WooPosColors.White,

    error = WooPosColors.WooRed50,
    onError = WooPosColors.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = WooPosColors.WooPurple40,
    onPrimary = Color.White,

    primaryContainer = WooPosColors.WooPurple40,
    onPrimaryContainer = WooPosColors.White,

    secondaryContainer = WooPosColors.Black,
    onSecondaryContainer = WooPosColors.White,

    surface = WooPosColors.Gray100,
    surfaceDim = WooPosColors.Gray0,
    surfaceBright = WooPosColors.White,

    surfaceContainerLowest = WooPosColors.Gray70,
    surfaceContainerLow = WooPosColors.Gray80,
    surfaceContainerHigh = WooPosColors.Gray100,

    onSurface = WooPosColors.White,

    inverseSurface = WooPosColors.White,
    inverseOnSurface = WooPosColors.Gray100,

    error = WooPosColors.WooRed50,
    onError = WooPosColors.White,
)

private val LocalCustomColors = staticCompositionLocalOf {
    LightCustomColors
}

private val LightCustomColors = CustomColors(
    loadingSkeleton = WooPosColors.lightCustomColorsLoadingSkeleton,
    border = WooPosColors.lightCustomColorsBorder,
    totalsErrorBackground = WooPosColors.lightQuaternaryBackground,
    totalsBackground = WooPosColors.Gray0,
    paymentSuccessBackground = WooPosColors.White,
    paymentSuccessText = WooPosColors.WooPurple90,
    paymentProcessingText = WooPosColors.White,
    paymentSuccessIcon = Color.White,
    homeBackground = WooPosColors.Gray0,
    paymentProcessingBackground = WooPosColors.WooPurple70,

    disabledContainer = WooPosColors.Gray5,
    onDisabledContainer = WooPosColors.Gray20,

    outline = WooPosColors.Gray40,
    outlineVariable = WooPosColors.Gray5,

    onSurfaceVariantLow = WooPosColors.Gray30,
    onSurfaceVariantHigh = WooPosColors.Gray60,

    fontPrimary = WooPosColors.Black,
    fontSecondary = WooPosColors.Gray60,
    fontTertiary = WooPosColors.Gray20,
)

private val DarkCustomColors = CustomColors(
    loadingSkeleton = WooPosColors.darkCustomLoadingSkeleton,
    border = WooPosColors.oldGrayMedium,
    totalsErrorBackground = WooPosColors.darkQuaternaryBackground,
    totalsBackground = WooPosColors.darkTotalsBackground,
    paymentSuccessBackground = WooPosColors.darkCustomColorsHomeBackground,
    paymentSuccessText = WooPosColors.oldGrayLight,
    paymentSuccessIcon = WooPosColors.darkCustomColorsHomeBackground,
    paymentProcessingText = WooPosColors.White,
    homeBackground = WooPosColors.darkCustomColorsHomeBackground,
    paymentProcessingBackground = WooPosColors.WooPurple70,

    disabledContainer = WooPosColors.Gray60,
    onDisabledContainer = WooPosColors.Gray40,

    outline = WooPosColors.Gray30,
    outlineVariable = WooPosColors.Gray60,

    onSurfaceVariantLow = WooPosColors.Gray50,
    onSurfaceVariantHigh = WooPosColors.Gray20,

    fontPrimary = WooPosColors.White,
    fontSecondary = WooPosColors.Gray20,
    fontTertiary = WooPosColors.Gray60,
)

@Composable
fun WooPosTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    val customColors = if (isSystemInDarkTheme()) {
        DarkCustomColors
    } else {
        LightCustomColors
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(colorScheme = colorScheme) {
            SurfacedContent(content)
        }
    }
}

@Composable
private fun SurfacedContent(content: @Composable () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.background) {
        content()
    }
}

object WooPosTheme {
    val colors: CustomColors
        @Composable
        get() = LocalCustomColors.current
}
