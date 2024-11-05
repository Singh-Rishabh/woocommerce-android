package com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageData

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

    }
}
