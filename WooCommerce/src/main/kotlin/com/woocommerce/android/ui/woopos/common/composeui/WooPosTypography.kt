package com.woocommerce.android.ui.woopos.common.composeui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object WooPosTypography {
    val HeadingPrimary: TextStyle
        @Composable get() = baseHeading(WooPosTheme.colors.fontPrimary, FontWeight.W700, null)
    val HeadingSecondary: TextStyle
        @Composable get() = baseHeading(WooPosTheme.colors.fontSecondary, FontWeight.W700, null)
    val HeadingTertiary: TextStyle
        @Composable get() = baseHeading(WooPosTheme.colors.fontTertiary, FontWeight.W700, null)

    val BodyXLargePrimary: TextStyle
        @Composable get() = baseBodyXLarge(WooPosTheme.colors.fontPrimary, FontWeight.W700, null)
    val BodyXLargeSecondary: TextStyle
        @Composable get() = baseBodyXLarge(WooPosTheme.colors.fontSecondary, FontWeight.W700, null)
    val BodyXLargeTertiary: TextStyle
        @Composable get() = baseBodyXLarge(WooPosTheme.colors.fontTertiary, FontWeight.W700, null)

    val BodyLargeBoldPrimary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontPrimary, FontWeight.W700, null)
    val BodyLargeBoldSecondary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontSecondary, FontWeight.W700, null)
    val BodyLargeBoldTertiary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontTertiary, FontWeight.W700, null)

    val BodyLargeRegularPrimary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontPrimary, FontWeight.W400, null)
    val BodyLargeRegularSecondary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontSecondary, FontWeight.W400, null)
    val BodyLargeRegularTertiary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontTertiary, FontWeight.W400, null)

    val BodyLargeUnderlinePrimary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontPrimary, FontWeight.W400, TextDecoration.Underline)
    val BodyLargeUnderlineSecondary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontSecondary, FontWeight.W400, TextDecoration.Underline)
    val BodyLargeUnderlineTertiary: TextStyle
        @Composable get() = baseBodyLarge(WooPosTheme.colors.fontTertiary, FontWeight.W400, TextDecoration.Underline)

    val BodyMediumBoldPrimary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontPrimary, FontWeight.W700, null)
    val BodyMediumBoldSecondary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontSecondary, FontWeight.W700, null)
    val BodyMediumBoldTertiary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontTertiary, FontWeight.W700, null)

    val BodyMediumRegularPrimary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontPrimary, FontWeight.W400, null)
    val BodyMediumRegularSecondary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontSecondary, FontWeight.W400, null)
    val BodyMediumRegularTertiary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontTertiary, FontWeight.W400, null)

    val BodyMediumUnderlinePrimary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontPrimary, FontWeight.W400, TextDecoration.Underline)
    val BodyMediumUnderlineSecondary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontSecondary, FontWeight.W400, TextDecoration.Underline)
    val BodyMediumUnderlineTertiary: TextStyle
        @Composable get() = baseBodyMedium(WooPosTheme.colors.fontTertiary, FontWeight.W400, TextDecoration.Underline)

    val BodySmallBoldPrimary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontPrimary, FontWeight.W700, null)
    val BodySmallBoldSecondary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontSecondary, FontWeight.W700, null)
    val BodySmallBoldTertiary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontTertiary, FontWeight.W700, null)

    val BodySmallRegularPrimary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontPrimary, FontWeight.W400, null)
    val BodySmallRegularSecondary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontSecondary, FontWeight.W400, null)
    val BodySmallRegularTertiary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontTertiary, FontWeight.W400, null)

    val BodySmallUnderlinePrimary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontPrimary, FontWeight.W400, TextDecoration.Underline)
    val BodySmallUnderlineSecondary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontSecondary, FontWeight.W400, TextDecoration.Underline)
    val BodySmallUnderlineTertiary: TextStyle
        @Composable get() = baseBodySmall(WooPosTheme.colors.fontTertiary, FontWeight.W400, TextDecoration.Underline)

    val CaptionBoldPrimary: TextStyle
        @Composable get() = baseCaption(WooPosTheme.colors.fontPrimary, FontWeight.W700, null)
    val CaptionBoldSecondary: TextStyle
        @Composable get() = baseCaption(WooPosTheme.colors.fontSecondary, FontWeight.W700, null)
    val CaptionBoldTertiary: TextStyle
        @Composable get() = baseCaption(WooPosTheme.colors.fontTertiary, FontWeight.W700, null)

    val CaptionRegularPrimary: TextStyle
        @Composable get() = baseCaption(WooPosTheme.colors.fontPrimary, FontWeight.W400, null)
    val CaptionRegularSecondary: TextStyle
        @Composable get() = baseCaption(WooPosTheme.colors.fontSecondary, FontWeight.W400, null)
    val CaptionRegularTertiary: TextStyle
        @Composable get() = baseCaption(WooPosTheme.colors.fontTertiary, FontWeight.W400, null)

    private fun baseHeading(color: Color, weight: FontWeight, textDecoration: TextDecoration?) = TextStyle(
        fontSize = 36.sp,
        lineHeight = 40.sp,
        fontWeight = weight,
        textDecoration = textDecoration,
        color = color
    )

    private fun baseBodyXLarge(color: Color, weight: FontWeight, textDecoration: TextDecoration?) = TextStyle(
        fontSize = 30.sp,
        lineHeight = 32.sp,
        fontWeight = weight,
        textDecoration = textDecoration,
        color = color
    )

    private fun baseBodyLarge(color: Color, weight: FontWeight, textDecoration: TextDecoration?) = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = weight,
        textDecoration = textDecoration,
        color = color
    )

    private fun baseBodyMedium(color: Color, weight: FontWeight, textDecoration: TextDecoration?) = TextStyle(
        fontSize = 20.sp,
        lineHeight = 32.sp,
        fontWeight = weight,
        textDecoration = textDecoration,
        color = color
    )

    private fun baseBodySmall(color: Color, weight: FontWeight, textDecoration: TextDecoration?) = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = weight,
        textDecoration = textDecoration,
        color = color
    )

    private fun baseCaption(color: Color, weight: FontWeight, textDecoration: TextDecoration?) = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = weight,
        textDecoration = textDecoration,
        color = color
    )
}

@Composable
@Preview
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
        "Heading" to listOf(
            "Primary" to WooPosTypography.HeadingPrimary,
            "Secondary" to WooPosTypography.HeadingSecondary,
            "Tertiary" to WooPosTypography.HeadingTertiary
        ),
        "Body X Large" to listOf(
            "Primary" to WooPosTypography.BodyXLargePrimary,
            "Secondary" to WooPosTypography.BodyXLargeSecondary,
            "Tertiary" to WooPosTypography.BodyXLargeTertiary
        ),
        "Body Large Bold" to listOf(
            "Primary" to WooPosTypography.BodyLargeBoldPrimary,
            "Secondary" to WooPosTypography.BodyLargeBoldSecondary,
            "Tertiary" to WooPosTypography.BodyLargeBoldTertiary
        ),
        "Body Large Regular" to listOf(
            "Primary" to WooPosTypography.BodyLargeRegularPrimary,
            "Secondary" to WooPosTypography.BodyLargeRegularSecondary,
            "Tertiary" to WooPosTypography.BodyLargeRegularTertiary
        ),
        "Body Large Underline" to listOf(
            "Primary" to WooPosTypography.BodyLargeUnderlinePrimary,
            "Secondary" to WooPosTypography.BodyLargeUnderlineSecondary,
            "Tertiary" to WooPosTypography.BodyLargeUnderlineTertiary
        ),
        "Body Medium Bold" to listOf(
            "Primary" to WooPosTypography.BodyMediumBoldPrimary,
            "Secondary" to WooPosTypography.BodyMediumBoldSecondary,
            "Tertiary" to WooPosTypography.BodyMediumBoldTertiary
        ),
        "Body Medium Regular" to listOf(
            "Primary" to WooPosTypography.BodyMediumRegularPrimary,
            "Secondary" to WooPosTypography.BodyMediumRegularSecondary,
            "Tertiary" to WooPosTypography.BodyMediumRegularTertiary
        ),
        "Body Medium Underline" to listOf(
            "Primary" to WooPosTypography.BodyMediumUnderlinePrimary,
            "Secondary" to WooPosTypography.BodyMediumUnderlineSecondary,
            "Tertiary" to WooPosTypography.BodyMediumUnderlineTertiary
        ),
        "Body Small Bold" to listOf(
            "Primary" to WooPosTypography.BodySmallBoldPrimary,
            "Secondary" to WooPosTypography.BodySmallBoldSecondary,
            "Tertiary" to WooPosTypography.BodySmallBoldTertiary
        ),
        "Body Small Regular" to listOf(
            "Primary" to WooPosTypography.BodySmallRegularPrimary,
            "Secondary" to WooPosTypography.BodySmallRegularSecondary,
            "Tertiary" to WooPosTypography.BodySmallRegularTertiary
        ),
        "Body Small Underline" to listOf(
            "Primary" to WooPosTypography.BodySmallUnderlinePrimary,
            "Secondary" to WooPosTypography.BodySmallUnderlineSecondary,
            "Tertiary" to WooPosTypography.BodySmallUnderlineTertiary
        ),
        "Caption Bold" to listOf(
            "Primary" to WooPosTypography.CaptionBoldPrimary,
            "Secondary" to WooPosTypography.CaptionBoldSecondary,
            "Tertiary" to WooPosTypography.CaptionBoldTertiary
        ),
        "Caption Regular" to listOf(
            "Primary" to WooPosTypography.CaptionRegularPrimary,
            "Secondary" to WooPosTypography.CaptionRegularSecondary,
            "Tertiary" to WooPosTypography.CaptionRegularTertiary
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Text Styles Preview",
            style = WooPosTypography.HeadingPrimary,
            modifier = Modifier.padding(16.dp)
        )

        tableData.forEach { (category, styles) ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(text = category, style = WooPosTypography.BodyXLargePrimary)

                    styles.forEach { (label, textStyle) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                style = WooPosTypography.BodyMediumBoldPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "Sample Text",
                                style = textStyle,
                                modifier = Modifier.weight(2f)
                            )
                        }
                    }
                }
            }
        }
    }
}
