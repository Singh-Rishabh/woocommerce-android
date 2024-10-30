package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground

@Composable
internal fun ShippingRatesCard(
    modifier: Modifier = Modifier
) {
    var selectedSortOption by remember { mutableStateOf(ShippingSortOption.CHEAPEST) }
    Column(modifier = modifier) {
        ShippingRatesHeader(
            selectedSortOption = selectedSortOption,
            onSortOptionSelected = { selectedSortOption = it }
        )
    }
}

@Composable
private fun ShippingRatesHeader(
    selectedSortOption: ShippingSortOption,
    onSortOptionSelected: (ShippingSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.shipping_label_shipping_service_title),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        SortingDropdownMenu(
            selectedSortOption = selectedSortOption,
            onSortOptionSelected = onSortOptionSelected,
            modifier = Modifier.padding(dimensionResource(R.dimen.major_100))
        )
    }
}

@Preview
@Composable
private fun ShippingRatesHeaderPreview() {
    WooThemeWithBackground {
        ShippingRatesHeader(
            selectedSortOption = ShippingSortOption.CHEAPEST,
            onSortOptionSelected = {},
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun SortingDropdownMenu(
    selectedSortOption: ShippingSortOption,
    onSortOptionSelected: (ShippingSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = ShippingSortOption.entries

    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(selectedSortOption.stringResource),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.primary
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(
                    R.string.sorted_by,
                    stringResource(selectedSortOption.stringResource)
                ),
                tint = MaterialTheme.colors.primary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.sizeIn(minWidth = 150.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onSortOptionSelected(option)
                    expanded = false
                }) {
                    Text(
                        text = stringResource(option.stringResource),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
        }
    }
}

enum class ShippingSortOption(@StringRes val stringResource: Int) {
    CHEAPEST(R.string.shipping_label_shipping_rates_sort_option_cheapest),
    FASTEST(R.string.shipping_label_shipping_rates_sort_option_fastest)
}
