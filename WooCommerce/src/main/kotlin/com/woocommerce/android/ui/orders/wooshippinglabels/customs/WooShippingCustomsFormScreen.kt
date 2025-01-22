package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.WCOutlinedSpinner
import com.woocommerce.android.ui.compose.component.WCOutlinedTextField

@Composable
fun WooShippingCustomsFormScreen(viewModel: WooShippingCustomsFormViewModel) {

}

@Composable
fun WooShippingCustomsFormScreen(
    modifier: Modifier = Modifier,
    contentType: String,
    restrictionType: String,
    itnValue: String,
    isAddCustomsButtonEnabled: Boolean,
    onContentTypeClick: () -> Unit,
    onRestrictionTypeClick: () -> Unit,
    onItnChanged: (String) -> Unit,
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            WCOutlinedSpinner(
                onClick = onContentTypeClick,
                value = contentType,
                label = stringResource(id = R.string.woo_shipping_labels_customs_content_type_label),
                modifier = modifier.fillMaxWidth()
            )
            WCOutlinedSpinner(
                onClick = onRestrictionTypeClick,
                value = restrictionType,
                label = stringResource(id = R.string.woo_shipping_labels_customs_restriction_type_label),
                modifier = modifier.fillMaxWidth()
            )
            WCOutlinedTextField(
                value = itnValue,
                onValueChange = onItnChanged,
                label = stringResource(id = R.string.woo_shipping_labels_customs_itn_label),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = modifier.fillMaxWidth()
            )
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
