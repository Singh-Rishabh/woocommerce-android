package com.woocommerce.android.ui.woopos.common.composeui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.ShadowType
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
    WooPosPaginationErrorIndicatorContent(
        modifier = modifier,
        icon = icon,
        message = message,
        primaryButton = primaryButton
    )
}

@Composable
private fun WooPosPaginationErrorIndicatorContent(
    modifier: Modifier,
    icon: Painter,
    message: String,
    primaryButton: Button
) {
    val itemContentDescription = stringResource(id = R.string.woopos_items_pagination_error_content_description)
    WooPosCard(
        modifier = modifier
            .semantics { contentDescription = itemContentDescription },
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 6.dp,
        shadowType = ShadowType.Soft,
    ) {
        Row(
            modifier = Modifier
                .height(112.dp)
                .fillMaxWidth()
                .padding(16.dp),
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
                Text(
                    text = message,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colors.error,
                    modifier = Modifier.padding(start = 24.dp.toAdaptivePadding())
                )
            }

            WooPosButton(
                text = primaryButton.text,
                onClick = primaryButton.click,
                modifier = Modifier
                    .weight(0.5f)
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
