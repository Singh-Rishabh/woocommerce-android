package com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
            WooShippingSavedPackageItem(packageData)
        }
    }
}

@Composable
fun WooShippingSavedPackageItem(packageData: PackageData) {
    Column {
        Text(text = packageData.name)
        Text(text = "Type: ${packageData.type}")
        Text(text = "Length: ${packageData.length}")
        Text(text = "Width: ${packageData.width}")
        Text(text = "Height: ${packageData.height}")
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
                    name = "Package 1",
                    length = "10",
                    width = "10",
                    height = "10"
                ),
                PackageData(
                    type = PackageType.BOX,
                    name = "Package 2",
                    length = "20",
                    width = "20",
                    height = "20"
                ),
                PackageData(
                    type = PackageType.BOX,
                    name = "Package 3",
                    length = "30",
                    width = "30",
                    height = "30"
                )
            )
        )
    }
}
