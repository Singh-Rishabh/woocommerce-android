package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R

@Composable
fun WooShippingCustomsFormScreen(viewModel: WooShippingCustomsFormViewModel) {

}

@Composable
fun WooShippingCustomsFormScreen(
    modifier: Modifier = Modifier,
    isAddCustomsButtonEnabled: Boolean,
    onAddCustomsDataClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {

        }
        Button(
            modifier = modifier.fillMaxWidth(),
            enabled = isAddCustomsButtonEnabled,
            onClick = onAddCustomsDataClick
        ) {
            Text(stringResource(id = R.string.woo_shipping_labels_customs_add_missing_information))
        }
    }
}
