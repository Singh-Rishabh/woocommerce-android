package com.woocommerce.android.ui.woopos.common.composeui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.woopos.util.ext.getLongestScreenSideDp

object Radius {
    val None = 0.dp
    val XSmall = 2.dp
    val Small = 4.dp
    val Medium = 8.dp
    val Large = 16.dp
    val XLarge = 24.dp
}

object Spacing {
    val None = 0.dp
    val XSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val XLarge = 32.dp
}

object Elevation {
    val None = 0.dp
    val Medium = 4.dp
    val Large = 8.dp
}

@Composable
fun Dp.toAdaptivePadding(): Dp {
    val longestSide = LocalContext.current.getLongestScreenSideDp()
    return when {
        longestSide < 880.dp -> {
            val calculatedMargin = this * 0.5f
            calculatedMargin.makeDividableByFour()
        }

        longestSide < 1200.dp -> {
            val calculatedMargin = this * 0.75f
            calculatedMargin.makeDividableByFour()
        }

        else -> this
    }
}

@Composable
private fun Dp.makeDividableByFour(): Dp {
    val remainder = this.value % 4
    return if (remainder == 0f) {
        this
    } else {
        this + (4 - remainder).dp
    }
}
