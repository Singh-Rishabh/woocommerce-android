package com.woocommerce.android.ui.orders.wooshippinglabels.customs.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground

@Composable
fun WooShippingCustomsProductListItem(
    itemData: WooShippingCustomsProductUIModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .background(
            color = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_large))
        )
        .border(
            width = dimensionResource(R.dimen.minor_10),
            color = colorResource(R.color.divider_color),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_large))
        )
        .padding(16.dp)
    ) {
        Row {
            Text(
                text = itemData.name,
                style = MaterialTheme.typography.body1,
                modifier = modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null
            )
        }
        Row {
            Text(
                text = itemData.description,
                style = MaterialTheme.typography.body2,
                modifier = modifier.weight(1f)
            )
            Text(
                text = itemData.tariffNumber,
                style = MaterialTheme.typography.body2
            )
        }
        Row {
            Text(
                text = itemData.originCountry,
                style = MaterialTheme.typography.body2,
                modifier = modifier.weight(1f)
            )
            Text(
                text = itemData.valuePerUnit,
                style = MaterialTheme.typography.body2,
            )
            Text(
                text = itemData.weightPerUnit,
                style = MaterialTheme.typography.body2
            )
        }
    }
}

@Preview
@Composable
fun WooShippingCustomsProductListItemPreview() {
    WooThemeWithBackground {
        WooShippingCustomsProductListItem(
            itemData = WooShippingCustomsProductUIModel(
                name = "Little Nap Brazil 250g",
                description = "Coffee Beans",
                tariffNumber = "HS 14-1",
                valuePerUnit = "$20.00",
                weightPerUnit = "0.3kg",
                originCountry = "Japan",
                isExpanded = false
            )
        )
    }
}
