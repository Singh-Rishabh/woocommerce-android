package com.woocommerce.android.ui.orders.wooshippinglabels.customs.products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WooShippingCustomsProductListItem(
    itemData: WooShippingCustomsProductUIModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
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
