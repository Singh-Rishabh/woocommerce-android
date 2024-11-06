package com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.SelectionCheck
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType

@Composable
fun WooShippingSavedPackageScreen(viewModel: WooShippingLabelPackageCreationViewModel) {
    val viewState = viewModel.viewState.observeAsState()
    WooShippingSavedPackageScreen(
        savedPackages = viewState.value?.savedPackages.orEmpty()
    )
}

@Composable
fun WooShippingSavedPackageScreen(
    modifier: Modifier = Modifier,
    savedPackages: List<PackageData>
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        savedPackages.forEach { packageData ->
            WooShippingSavedPackageItem(modifier, packageData)
        }
    }
}

@Composable
fun WooShippingSavedPackageItem(
    modifier: Modifier,
    packageData: PackageData
) {
    Column (
        modifier = modifier.padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row (
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SelectionCheck(
                isSelected = true,
                onSelectionChange = {}
            )
            Column (verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = packageData.description,
                    style = MaterialTheme.typography.caption,
                    color = colorResource(id = R.color.color_on_surface_disabled)
                )
                Text(
                    text = packageData.name,
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = packageData.dimensionsForDisplay,
                    style = MaterialTheme.typography.body2
                )
            }
        }
        Divider()
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
                    height = "10"
                ),
                PackageData(
                    type = PackageType.BOX,
                    name = "Small Flat Rate Box",
                    description = "Custom package",
                    length = "20",
                    width = "20",
                    height = "20"
                ),
                PackageData(
                    type = PackageType.BOX,
                    name = "Small Flat Rate Box",
                    description = "DHL Express",
                    length = "30",
                    width = "30",
                    height = "30"
                )
            )
        )
    }
}
