package com.woocommerce.android.ui.orders.creation.coupon

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground

@Composable
fun CouponLineFormSection(
    couponLineDetails: List<CouponLineDetails>,
    isEnabled: Boolean,
    onAdd: () -> Unit,
    onRemove: (code: String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(couponLineDetails.isNotEmpty()) {
        Card(shape = RectangleShape, modifier = modifier) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.padding(bottom = 16.dp)) {
                    Text(
                        text = stringResource(id = R.string.coupons),
                        style = MaterialTheme.typography.h6,
                        modifier = modifier
                            .weight(2f, true)
                            .align(Alignment.CenterVertically),
                    )
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(id = R.string.order_creation_add_coupon),
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .alpha(if (isEnabled) 1f else 0.4f)
                            .clickable(enabled = isEnabled) { onAdd() },
                        tint = MaterialTheme.colors.primary
                    )
                }

                couponLineDetails.forEachIndexed { i, couponDetails ->
                    val itemModifier = if (i == 0) Modifier else Modifier.padding(top = 8.dp)
                    CouponLineEditCard(
                        couponLine = couponDetails,
                        modifier = itemModifier
                            .alpha(if (isEnabled) 1f else 0.4f)
                            .clickable(enabled = isEnabled) { onRemove(couponDetails.code) }
                    )
                }
            }
        }
    }
}

@Composable
fun CouponLineEditCard(
    couponLine: CouponLineDetails,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .border(
                brush = SolidColor(MaterialTheme.colors.onSurface.copy(alpha = 0.12f)),
                width = 1.dp,
                shape = RoundedCornerShape(dimensionResource(id = R.dimen.corner_radius_large))
            )
            .padding(dimensionResource(id = R.dimen.major_100))
    ) {
        Column(
            modifier = Modifier
                .weight(2f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = couponLine.code,
                style = TextStyle(
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp
                ),
                color = colorResource(id = R.color.color_on_surface),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 16.dp)
        )
    }
}

@Preview
@Preview(name = "Dark mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CouponLineDetailsPreview() {
    WooThemeWithBackground {
        CouponLineDetails(
            code = "abcdefg",
        )
    }
}

@Preview
@Composable
fun CouponLineFormSectionPreview() {
    val couponLine = List(3) { i ->
        CouponLineDetails(
            code = "abcdefg_$i",
        )
    }
    WooThemeWithBackground {
        CouponLineFormSection(
            couponLineDetails = couponLine,
            isEnabled = true,
            onAdd = { },
            onRemove = { }
        )
    }
}
