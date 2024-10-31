package com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel

@Composable
fun WooShippingCustomPackageCreationScreen(viewModel: WooShippingLabelPackageCreationViewModel) {
    WooShippingCustomPackageCreationScreen(
        onAddPackageClick = viewModel::onAddPackageClick
    )
}

@Composable
fun WooShippingCustomPackageCreationScreen(
    modifier: Modifier = Modifier,
    onAddPackageClick: () -> Unit
) {
    Column {
        Column(modifier = modifier) {
            Text("Package type")
        }
        Row(modifier = modifier) {
            Text("Package measurements")
        }
        Row {
            Text("Package Saving options")
        }
    }
}
