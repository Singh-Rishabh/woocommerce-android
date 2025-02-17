package com.woocommerce.android.ui.woopos.common.composeui.designsystem

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object WooPosTypography {
    val Heading = TextStyle(
        fontSize = 36.sp,
        lineHeight = 40.sp
    )
    val BodyXLarge = TextStyle(
        fontSize = 30.sp,
        lineHeight = 32.sp
    )
    val BodyLarge = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
    val BodyMedium = TextStyle(
        fontSize = 20.sp,
        lineHeight = 32.sp
    )
    val BodySmall = TextStyle(
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val Caption = TextStyle(
        fontSize = 14.sp,
        lineHeight = 20.sp
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
        "Heading" to WooPosTypography.Heading,
        "Body X Large" to WooPosTypography.BodyXLarge,
        "Body Large" to WooPosTypography.BodyLarge,
        "Body Medium" to WooPosTypography.BodyMedium,
        "Body Small" to WooPosTypography.BodySmall,
        "Caption" to WooPosTypography.Caption
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WooPosSpacing.Medium.value)
            .background(
                MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            )
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Text Styles Preview",
            style = WooPosTypography.Heading,
            modifier = Modifier.padding(WooPosSpacing.Medium.value)
        )

        tableData.forEach { (name, style) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = WooPosSpacing.XSmall.value),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = name,
                    style = WooPosTypography.BodyMedium,
                    modifier = Modifier.weight(1f)
                )
                @Suppress("WooPosTypographyUsageRule")
                Text(
                    text = "Sample Text",
                    style = style,
                    modifier = Modifier.weight(2f)
                )
            }
        }
    }
}
