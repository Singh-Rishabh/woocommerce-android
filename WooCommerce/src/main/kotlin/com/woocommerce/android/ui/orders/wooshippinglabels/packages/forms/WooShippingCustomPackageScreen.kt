package com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.WCOutlinedTextField
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel

@Composable
fun WooShippingCustomPackageCreationScreen(viewModel: WooShippingLabelPackageCreationViewModel) {
    val viewState by viewModel.viewState.observeAsState()
    WooShippingCustomPackageCreationScreen(
        isAddPackageEnabled = viewState?.customPackageCreationData?.isValid ?: false,
        onAddPackageClick = viewModel::onAddPackageClick
    )
}

@Composable
fun WooShippingCustomPackageCreationScreen(
    modifier: Modifier = Modifier,
    isAddPackageEnabled: Boolean,
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
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = modifier.fillMaxWidth()
            ) {
                Text("Length")
                Text("Width")
                Text("Height")
            }
            Row {
                Text("Package Saving options")
            }
        }
        Button(
            modifier = modifier.fillMaxWidth(),
            enabled = isAddPackageEnabled,
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
            isAddPackageEnabled = true,
            onAddPackageClick = {}
        )
    }
}
