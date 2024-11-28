package com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.components.WooSavedPackageListItem

@Composable
fun WooShippingSavedPackageScreen(viewModel: WooShippingLabelPackageCreationViewModel) {
    val viewState = viewModel.viewState.observeAsState()
    WooShippingSavedPackageScreen(
        savedPackages = viewState.value?.savedPackageSelection?.packages.orEmpty(),
        isAddPackageEnabled = viewState.value?.savedPackageSelection?.hasSelection ?: false,
        onAddPackageClick = viewModel::onAddSavedPackageClick,
        onSavedPackageSelected = viewModel::onSavedPackageSelected

    )
}

@Composable
fun WooShippingSavedPackageScreen(
    modifier: Modifier = Modifier,
    savedPackages: List<PackageData>,
    isAddPackageEnabled: Boolean,
    onAddPackageClick: () -> Unit,
    onSavedPackageSelected: (PackageData, Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            savedPackages.forEach { packageData ->
                WooSavedPackageListItem(
                    modifier,
                    packageData,
                    onSavedPackageSelected
                )
            }
        }
        Column {
            Divider()
            Button(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                enabled = isAddPackageEnabled,
                onClick = onAddPackageClick
            ) {
                Text(stringResource(id = R.string.woo_shipping_labels_package_creation_add_package))
            }
        }
    }
}

@Preview
@Composable
fun WooShippingSavedPackageScreenPreview() {
    WooThemeWithBackground {
        WooShippingSavedPackageScreen(
            savedPackages = listOf(
                PackageData(
                    type = PackageType.ENVELOPE,
                    name = "Small Flat Rate Box",
                    description = "USPS Priority Mail Flat Rate Boxes",
                    length = "10",
                    width = "10",
                    height = "10",
                    isSelected = true
                ),
                PackageData(
                    type = PackageType.BOX,
                    name = "Small Flat Rate Box",
                    description = "Custom package",
                    length = "20",
                    width = "20",
                    height = "20",
                    isSelected = false
                ),
                PackageData(
                    type = PackageType.BOX,
                    name = "Small Flat Rate Box",
                    description = "DHL Express",
                    length = "30",
                    width = "30",
                    height = "30",
                    isSelected = false
                )
            ),
            isAddPackageEnabled = true,
            onAddPackageClick = {},
            onSavedPackageSelected = { _, _ -> }
        )
    }
}
