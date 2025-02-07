package com.woocommerce.android.ui.woopos.common.composeui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object WooPosTypography {
    val Heading: TextStyle
        @Composable
        get() = TextStyle(
            fontSize = 36.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.0.sp,
            fontWeight = FontWeight.W700,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodyXLarge: TextStyle
        @Composable
        get() = TextStyle(
            fontSize = 30.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.W700,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodyLargeBold: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W700,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.0.sp,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodyLargeRegular: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.0.sp,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodyLargeUnderline: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.0.sp,
            textDecoration = TextDecoration.Underline,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodyMediumBold: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W700,
            fontSize = 20.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.0.sp,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodyMediumRegular: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 20.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.0.sp,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodyMediumUnderline: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 20.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.0.sp,
            textDecoration = TextDecoration.Underline,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodySmallBold: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W700,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.0.sp,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodySmallRegular: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.0.sp,
            color = WooPosTheme.colors.fontPrimary,
        )

    val BodySmallUnderline: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.0.sp,
            textDecoration = TextDecoration.Underline,
            color = WooPosTheme.colors.fontPrimary,
        )

    val CaptionBold: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W700,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.0.sp,
            color = WooPosTheme.colors.fontPrimary,
        )

    val CaptionRegular: TextStyle
        @Composable
        get() = TextStyle(
            fontWeight = FontWeight.W400,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.0.sp,
            color = WooPosTheme.colors.fontPrimary,
        )
}

@Composable
@WooPosPreview
fun PreviewTextReferenceTable() {
    WooPosTheme {
        Surface {
            TextReferenceTable()
        }
    }
}

@Composable
private fun TextReferenceTable() {
    val tableData = listOf(
        "Heading" to listOf("Bold" to WooPosTypography.Heading),
        "Body X Large" to listOf("Bold" to WooPosTypography.BodyXLarge),
        "Body Large" to listOf(
            "Bold" to WooPosTypography.BodyLargeBold,
            "Regular" to WooPosTypography.BodyLargeRegular,
            "Regular Underline" to WooPosTypography.BodyLargeUnderline
        ),
        "Body Medium" to listOf(
            "Bold" to WooPosTypography.BodyMediumBold,
            "Regular" to WooPosTypography.BodyMediumRegular,
            "Regular Underline" to WooPosTypography.BodyMediumUnderline
        ),
        "Body Small" to listOf(
            "Bold" to WooPosTypography.BodySmallBold,
            "Regular" to WooPosTypography.BodySmallRegular,
            "Regular Underline" to WooPosTypography.BodySmallUnderline
        ),
        "Caption" to listOf(
            "Bold" to WooPosTypography.CaptionBold,
            "Regular" to WooPosTypography.CaptionRegular
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Text reference",
            style = WooPosTypography.Heading,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Examples composed text styles.",
            style = WooPosTypography.BodyMediumRegular,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Surface(
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 4.dp
        ) {
            Column {
                tableData.forEach { (category, styles) ->
                    TableRow(category, styles)
                }
            }
        }
    }
}

@Composable
private fun TableRow(category: String, styles: List<Pair<String, TextStyle>>) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Text(
                text = category,
                style = WooPosTypography.BodyMediumBold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                textAlign = TextAlign.Start
            )
        }

        styles.forEach { (label, textStyle) ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.background
            ) {
                Text(
                    text = label,
                    style = textStyle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
