package com.woocommerce.android.ui.woopos.common.composeui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import com.woocommerce.android.ui.woopos.common.composeui.WooPosCard
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding

@Composable
fun WooPosPaginationErrorIndicator(
    modifier: Modifier = Modifier,
    icon: Painter = painterResource(id = R.drawable.woo_pos_ic_error),
    message: String,
    primaryButton: Button,
) {
    WooPosCard {
        WooPosPaginationErrorIndicatorContent(
            modifier = modifier,
            icon = icon,
            message = message,
            primaryButton = primaryButton
        )
    }
}

@Composable
private fun WooPosPaginationErrorIndicatorContent(
    modifier: Modifier,
    icon: Painter,
    message: String,
    primaryButton: Button
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(112.dp)
            .clip(RoundedCornerShape(16.dp))
            .padding(16.dp.toAdaptivePadding()),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = icon,
                contentDescription = stringResource(R.string.woopos_error_icon_content_description),
                tint = Color.Unspecified,
            )

            Spacer(modifier = Modifier.width(24.dp.toAdaptivePadding()))

            Text(
                text = message,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colors.error
            )
        }

        Row(
            modifier = Modifier.weight(0.5f)
        ) {
            WooPosButton(
                text = primaryButton.text,
                onClick = primaryButton.click,
                modifier = Modifier
                    .height(50.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
@WooPosPreview
fun WooPosPaginationErrorScreenPreview() {
    WooPosTheme {
        WooPosPaginationErrorIndicator(
            message = "Error loading products",
            primaryButton = Button(
                text = "Load more",
                click = {}
            )
        )
    }
}
