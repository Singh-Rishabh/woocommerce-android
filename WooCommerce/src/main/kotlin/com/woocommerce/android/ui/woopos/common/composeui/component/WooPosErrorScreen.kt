package com.woocommerce.android.ui.woopos.common.composeui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosCornerRadius
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosSpacing
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosTypography
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.toAdaptivePadding

@Composable
fun WooPosErrorScreen(
    modifier: Modifier = Modifier,
    icon: Painter = painterResource(id = R.drawable.ic_woo_pos_error),
    message: String,
    reason: String,
    primaryButton: Button? = null,
    secondaryButton: Button? = null,
    adaptToScreenHeight: Boolean = false,
) {
    Column(
        modifier = modifier.fillMaxSize()
            .clip(RoundedCornerShape(WooPosCornerRadius.Medium.value))
            .padding(WooPosSpacing.XLarge.value.toAdaptivePadding()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            modifier = Modifier.let { if (adaptToScreenHeight) it.weight(1f) else it },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                modifier = Modifier.size(80.dp),
                painter = icon,
                contentDescription = stringResource(id = R.string.woopos_error_icon_content_description),
                tint = WooPosTheme.colors.unspecified,
            )

            Spacer(modifier = Modifier.height(WooPosSpacing.XLarge.value.toAdaptivePadding()))

            Text(
                text = message,
                style = WooPosTypography.Heading,
                fontWeight = FontWeight.Bold,
            )

            Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value.toAdaptivePadding()))

            Text(
                text = reason,
                style = WooPosTypography.BodyLarge,
            )
            Spacer(modifier = Modifier.height(WooPosSpacing.XLarge.value.toAdaptivePadding()))
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = WooPosSpacing.Medium.value,
                    end = WooPosSpacing.Medium.value,
                    start = WooPosSpacing.Medium.value
                )
        ) {
            primaryButton?.let {
                WooPosButton(
                    text = it.text,
                    onClick = it.click,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
            secondaryButton?.let {
                Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value.toAdaptivePadding()))
                WooPosButton(
                    text = it.text,
                    onClick = it.click,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
            Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value.toAdaptivePadding()))
        }
    }
}

data class Button(
    val text: String,
    val click: () -> Unit
)

@Composable
@WooPosPreview
fun WooPosErrorStatePreview() {
    WooPosTheme {
        WooPosErrorScreen(
            message = stringResource(R.string.woopos_totals_main_error_label),
            reason = "Reason",
            primaryButton = Button(
                text = stringResource(R.string.retry),
                click = { }
            ),
            secondaryButton = Button(
                text = stringResource(R.string.cancel),
                click = { }
            )
        )
    }
}

@Composable
@WooPosPreview
fun WooPosErrorStateSingleButtonPreview() {
    WooPosTheme {
        WooPosErrorScreen(
            message = stringResource(R.string.woopos_totals_main_error_label),
            reason = "Reason",
            primaryButton = Button(
                text = stringResource(R.string.retry),
                click = { }
            ),
        )
    }
}

@Composable
@WooPosPreview
fun WooPosErrorStateSingleButtonAdaptToScreenHeightPreview() {
    WooPosTheme {
        WooPosErrorScreen(
            message = stringResource(R.string.woopos_totals_main_error_label),
            reason = "Reason",
            primaryButton = Button(
                text = stringResource(R.string.retry),
                click = { }
            ),
            adaptToScreenHeight = true,
        )
    }
}

@Composable
@WooPosPreview
fun WooPosErrorStateAdaptToScreenHeightPreview() {
    WooPosTheme {
        WooPosErrorScreen(
            message = stringResource(R.string.woopos_totals_main_error_label),
            reason = "Reason",
            primaryButton = Button(
                text = stringResource(R.string.retry),
                click = { }
            ),
            secondaryButton = Button(
                text = stringResource(R.string.cancel),
                click = { }
            ),
            adaptToScreenHeight = true,
        )
    }
}
