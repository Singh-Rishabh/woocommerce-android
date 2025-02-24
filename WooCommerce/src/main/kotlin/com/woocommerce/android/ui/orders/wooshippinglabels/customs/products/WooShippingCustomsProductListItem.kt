package com.woocommerce.android.ui.orders.wooshippinglabels.customs.products

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.WCOutlinedTextField
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.components.RoundedBorderDropDownWithLabel
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.InputValue

@Composable
fun WooShippingCustomsProductListItem(
    itemData: WooShippingCustomsProductUIModel,
    modifier: Modifier = Modifier
) {
    val rotationAnimation = itemData.isExpanded
        .let { if (it) 180f else 0f }
        .let { animateFloatAsState(targetValue = it, label = "rotationAnimation") }

    if (itemData.isExpanded) {
        WooShippingCustomsProductExpandedListItem(
            itemData = itemData,
            onDescriptionChanged = { },
            onTariffChanged = { },
            onValuePerUnitChanged = { },
            onWeightPerUnitChanged = { },
            modifier = modifier
        )
    } else {
        WooShippingCustomsProductCollapsedListItem(
            itemData = itemData,
            modifier = modifier
        )
    }
}

@Composable
fun WooShippingCustomsProductCollapsedListItem(
    modifier: Modifier,
    itemData: WooShippingCustomsProductUIModel
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
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = null
            )
        }

        Spacer(modifier.padding(vertical = 4.dp))

        Row {
            Text(
                text = itemData.description.currentInput,
                style = MaterialTheme.typography.caption,
                modifier = modifier.weight(1f)
            )
            Text(
                text = itemData.tariffNumber.currentInput,
                style = MaterialTheme.typography.caption
            )
        }
        Row {
            Text(
                text = itemData.originCountry,
                style = MaterialTheme.typography.caption,
                modifier = modifier.weight(1f)
            )
            Text(
                text = itemData.valueAndWeightForDisplay,
                style = MaterialTheme.typography.caption,
            )
        }
    }
}

@Composable
fun WooShippingCustomsProductExpandedListItem(
    modifier: Modifier,
    itemData: WooShippingCustomsProductUIModel,
    onDescriptionChanged: (String) -> Unit,
    onTariffChanged: (String) -> Unit,
    onValuePerUnitChanged: (String) -> Unit,
    onWeightPerUnitChanged: (String) -> Unit
) {
    Column(modifier = modifier
        .fillMaxWidth()
        .background(
            color = MaterialTheme.colors.surface,
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_large))
        )
        .border(
            width = dimensionResource(R.dimen.minor_10),
            color = colorResource(R.color.woo_black),
            shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_large))
        )
        .padding(16.dp)
    ) {
        Row {
            Text(
                text = itemData.name,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_arrow_up),
                contentDescription = null
            )
        }
        Divider(modifier.padding(vertical = 8.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            WCOutlinedTextField(
                value = itemData.description.currentInput,
                onValueChange = onDescriptionChanged,
                label = stringResource(id = R.string.woo_shipping_labels_customs_product_details_description),
                singleLine = true,
                isError = itemData.description is InputValue.Error,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = modifier.fillMaxWidth(),
                helperText = itemData.description.errorMessageOrNull
                    ?.let { stringResource(it) }
            )

            WCOutlinedTextField(
                value = itemData.tariffNumber.currentInput,
                onValueChange = onTariffChanged,
                label = stringResource(id = R.string.woo_shipping_labels_customs_product_details_tariff),
                singleLine = true,
                isError = itemData.tariffNumber is InputValue.Error,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = modifier.fillMaxWidth(),
                helperText = itemData.tariffNumber.errorMessageOrNull
                    ?.let { stringResource(it) }
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WCOutlinedTextField(
                    value = itemData.valuePerUnit.currentInput,
                    onValueChange = onValuePerUnitChanged,
                    label = stringResource(id = R.string.woo_shipping_labels_customs_product_details_value_per_unit),
                    singleLine = true,
                    isError = itemData.valuePerUnit is InputValue.Error,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = modifier.weight(1f),
                    helperText = itemData.valuePerUnit.errorMessageOrNull
                        ?.let { stringResource(it) }
                )

                WCOutlinedTextField(
                    value = itemData.weightPerUnit.currentInput,
                    onValueChange = onWeightPerUnitChanged,
                    label = stringResource(id = R.string.woo_shipping_labels_customs_product_details_weight_per_unit),
                    singleLine = true,
                    isError = itemData.weightPerUnit is InputValue.Error,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = modifier.weight(1f),
                    helperText = itemData.weightPerUnit.errorMessageOrNull
                        ?.let { stringResource(it) }
                )
            }

            RoundedBorderDropDownWithLabel(
                label = stringResource(id = R.string.woo_shipping_labels_customs_product_details_origin_country),
                text = itemData.originCountry,
                onClick = { },
                modifier = modifier.fillMaxWidth()
            )
        }
    }
}

@Preview
@Composable
fun WooShippingCustomsProductListCollapsedItemPreview() {
    WooThemeWithBackground {
        Box(Modifier.padding(16.dp)) {
            WooShippingCustomsProductListItem(
                itemData = WooShippingCustomsProductUIModel(
                    name = "Little Nap Brazil 250g",
                    description = InputValue.Data("Coffee Beans"),
                    tariffNumber = InputValue.Data("HS 14-1"),
                    valuePerUnit = InputValue.Data("$20.00"),
                    weightPerUnit = InputValue.Data("0.3kg"),
                    originCountry = "Japan",
                    isExpanded = false
                )
            )
        }
    }
}

@Preview
@Composable
fun WooShippingCustomsProductListExpandedItemPreview() {
    WooThemeWithBackground {
        Box(Modifier.padding(16.dp)) {
            WooShippingCustomsProductListItem(
                itemData = WooShippingCustomsProductUIModel(
                    name = "Little Nap Brazil 250g",
                    description = InputValue.Data("Coffee Beans"),
                    tariffNumber = InputValue.Data("HS 14-1"),
                    valuePerUnit = InputValue.Data("$20.00"),
                    weightPerUnit = InputValue.Data("0.3kg"),
                    originCountry = "Japan",
                    isExpanded = true
                )
            )
        }
    }
}
