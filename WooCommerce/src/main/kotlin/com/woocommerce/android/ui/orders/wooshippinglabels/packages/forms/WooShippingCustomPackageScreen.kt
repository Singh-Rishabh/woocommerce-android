package com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = modifier.weight(1f)) {
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
        Button(
            modifier = modifier.fillMaxWidth(),
            onClick = onAddPackageClick
        ) {
            Text("Add package")
        }
    }
}

@Preview
@Composable
fun PreviewWooShippingCustomPackageCreationScreen() {
    WooThemeWithBackground {
        WooShippingCustomPackageCreationScreen(
            onAddPackageClick = {}
        )
    }
}
