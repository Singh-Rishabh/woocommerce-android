package com.woocommerce.android.ui.orders.wooshippinglabels.hazmat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.woocommerce.android.R
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelHazmatCategory

typealias OnClick = () -> Unit

@Composable
internal fun HazmatCard(
    modifier: Modifier = Modifier,
    selectedCategory: ShippingLabelHazmatCategory? = null,
    onClick: OnClick? = null
) {
    var hazmatCardModifier = modifier
    if (onClick != null) {
        hazmatCardModifier = modifier.then(modifier.clickable { onClick() })
    }

    Row(modifier = hazmatCardModifier) {
        Text(
            text = stringResource(R.string.shipping_label_hazmat_title),
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(dimensionResource(id = R.dimen.major_100))
                .align(Alignment.CenterVertically)
        )

        Text(
            text = if (selectedCategory == null) stringResource(R.string.no) else stringResource(R.string.yes),
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.color_on_surface_medium),
            modifier = Modifier
                .align(Alignment.CenterVertically)
        )

        if (onClick != null) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_right),
                tint = colorResource(id = R.color.color_on_surface_medium),
                contentDescription = stringResource(
                    id = R.string.shipping_label_package_details_items_expand_content_description
                ),
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = dimensionResource(R.dimen.minor_50))
            )
        }
    }

    if (selectedCategory != null) {
        HazmatSelectionCard(
            selectedCategory = selectedCategory,
            modifier = Modifier.padding(dimensionResource(id = R.dimen.major_100))
        )
    }
}
