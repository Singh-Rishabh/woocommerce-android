package com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.WCOutlinedSpinner
import com.woocommerce.android.ui.compose.component.WCOutlinedTextField
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel

@Composable
fun WooShippingCustomPackageCreationScreen(viewModel: WooShippingLabelPackageCreationViewModel) {
    val viewState by viewModel.viewState.observeAsState()
    val packageType = viewState?.customPackageCreationData?.type?.resourceId
        ?.let { stringResource(id = it) }.orEmpty()

    WooShippingCustomPackageCreationScreen(
        packageType = packageType,
        packageHeight = viewState?.customPackageCreationData?.height.orEmpty(),
        packageLength = viewState?.customPackageCreationData?.length.orEmpty(),
        packageWidth = viewState?.customPackageCreationData?.width.orEmpty(),
        isAddPackageEnabled = viewState?.customPackageCreationData?.isValid ?: false,
        isPackageNameFieldEnabled = viewState?.customPackageCreationData?.saveAsTemplate ?: false,
        onAddPackageClick = viewModel::onAddCustomPackageClick,
        onPackageTypeClick = viewModel::onPackageTypeSpinnerClick,
        onLengthChange = viewModel::onLengthChange,
        onWidthChange = viewModel::onWidthChange,
        onHeightChange = viewModel::onHeightChange,
        onSavePackageChanged = viewModel::onSavePackageChanged
    )
}

@Composable
fun WooShippingCustomPackageCreationScreen(
    modifier: Modifier = Modifier,
    packageType: String,
    packageLength: String,
    packageWidth: String,
    packageHeight: String,
    isAddPackageEnabled: Boolean,
    isPackageNameFieldEnabled: Boolean,
    onAddPackageClick: () -> Unit,
    onPackageTypeClick: () -> Unit,
    onLengthChange: (String) -> Unit,
    onWidthChange: (String) -> Unit,
    onHeightChange: (String) -> Unit,
    onSavePackageChanged: (Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = modifier) {
                WCOutlinedSpinner(
                    onClick = onPackageTypeClick,
                    value = packageType,
                    label = stringResource(id = R.string.woo_shipping_labels_package_creation_package_type),
                    modifier = modifier.fillMaxWidth()
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = modifier.fillMaxWidth()
            ) {
                WCOutlinedTextField(
                    value = packageLength,
                    onValueChange = onLengthChange,
                    label = stringResource(id = R.string.woo_shipping_labels_package_creation_length),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = modifier.weight(1f)
                )

                WCOutlinedTextField(
                    value = packageWidth,
                    onValueChange = onWidthChange,
                    label = stringResource(id = R.string.woo_shipping_labels_package_creation_width),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = modifier.weight(1f)
                )

                WCOutlinedTextField(
                    value = packageHeight,
                    onValueChange = onHeightChange,
                    label = stringResource(id = R.string.woo_shipping_labels_package_creation_height),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = modifier.weight(1f)
                )
            }
            Row(
                horizontalArrangement = Arrangement.Absolute.SpaceBetween,
                modifier = modifier.fillMaxWidth()
            ) {
                Text(
                    text = stringResource(id = R.string.woo_shipping_labels_package_creation_save_package_option),
                    modifier = modifier.align(Alignment.CenterVertically)
                )
                Checkbox(checked = false, onCheckedChange = onSavePackageChanged)
            }
        }
        Button(
            modifier = modifier.fillMaxWidth(),
            enabled = isAddPackageEnabled,
            onClick = onAddPackageClick
        ) {
            Text(stringResource(id = R.string.woo_shipping_labels_package_creation_add_package))
        }
    }
}

@Preview
@Composable
fun PreviewWooShippingCustomPackageCreationScreen() {
    WooThemeWithBackground {
        WooShippingCustomPackageCreationScreen(
            packageType = "Box",
            packageLength = "10",
            packageWidth = "10",
            packageHeight = "10",
            isAddPackageEnabled = true,
            isPackageNameFieldEnabled = true,
            onAddPackageClick = {},
            onPackageTypeClick = {},
            onLengthChange = {},
            onWidthChange = {},
            onHeightChange = {},
            onSavePackageChanged = {}
        )
    }
}
