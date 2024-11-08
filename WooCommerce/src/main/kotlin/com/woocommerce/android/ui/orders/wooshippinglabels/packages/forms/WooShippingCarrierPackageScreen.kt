package com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel

@Composable
fun WooShippingCarrierPackageScreen(viewModel: WooShippingLabelPackageCreationViewModel) {
    val viewState by viewModel.viewState.observeAsState()
    viewState.apply {  }
    WooShippingCarrierPackageScreen(
        carrierPackages = emptyMap()
    )
}

@Composable
fun WooShippingCarrierPackageScreen(
    carrierPackages: Map<Carrier, List<PackageData>>
) {
    Column {
        CarrierTabRow()
        PackageListPager()
    }
}

@Composable
private fun CarrierTabRow() {

}

@Composable
private fun PackageListPager() {

}
