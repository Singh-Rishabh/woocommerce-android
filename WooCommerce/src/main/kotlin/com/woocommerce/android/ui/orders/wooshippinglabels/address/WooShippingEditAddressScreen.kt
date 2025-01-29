package com.woocommerce.android.ui.orders.wooshippinglabels.address

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.extensions.isNotNullOrEmpty
import com.woocommerce.android.ui.compose.component.Toolbar
import com.woocommerce.android.ui.orders.wooshippinglabels.RoundedCornerBoxWithBorder
import com.woocommerce.android.ui.orders.wooshippinglabels.address.origin.EditableAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.address.origin.WooShippingEditOriginViewModel

@Composable
fun WooShippingEditAddressScreen(
    viewModel: WooShippingEditOriginViewModel,
    modifier: Modifier = Modifier
) {
    when (val viewState = viewModel.viewState.collectAsState().value) {
        is WooShippingEditOriginViewModel.EditAddressViewState.DataState -> {
            WooShippingEditAddressScreen(
                editableAddress = viewState.editableAddress,
                isCompanyExpanded = viewState.isCompanyExpanded,
                onExpandCompany = viewModel::onExpandCompany,
                onNameChange = viewModel::onNameChange,
                onCompanyChange = viewModel::onCompanyChange,
                onAddressChange = viewModel::onAddressChange,
                onCityChange = viewModel::onCityChange,
                onPostalCodeChange = viewModel::onPostalCodeChange,
                onEmailChange = viewModel::onEmailChange,
                onPhoneChange = viewModel::onPhoneChange,
                modifier = modifier
            )
        }
    }
}

@Composable
fun WooShippingEditAddressScreen(
    editableAddress: EditableAddress,
    isCompanyExpanded: Boolean,
    onExpandCompany: () -> Unit,
    onNameChange: (String) -> Unit,
    onCompanyChange: (String) -> Unit,
    onAddressChange: (String) -> Unit,
    onCityChange: (String) -> Unit,
    onPostalCodeChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            Toolbar(
                title = stringResource(id = R.string.woo_shipping_edit_origin_address_title),
                onNavigationButtonClick = {},
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack
            )
        },
        backgroundColor = MaterialTheme.colors.surface
    ) { padding ->
        Column(
            modifier
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            val companyFocusRequester = remember { FocusRequester() }
            val addressFocusRequester = remember { FocusRequester() }
            val cityFocusRequester = remember { FocusRequester() }
            val postalCodeFocusRequester = remember { FocusRequester() }
            val emailFocusRequester = remember { FocusRequester() }
            val phoneFocusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            RoundedBorderTextFieldWithLabel(
                label = "${stringResource(id = R.string.woo_shipping_label_name)} *",
                text = editableAddress.name.value,
                error = editableAddress.name.error,
                onTextChange = onNameChange,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        if (isCompanyExpanded) {
                            companyFocusRequester.requestFocus()
                        } else {
                            addressFocusRequester.requestFocus()
                        }
                    }
                ),
            )

            CollapsedField(
                isExpanded = isCompanyExpanded,
                label = stringResource(id = R.string.woo_shipping_label_company),
                onExpand = onExpandCompany,
                modifier = Modifier.fillMaxWidth()
            ) {
                RoundedBorderTextFieldWithLabel(
                    label = stringResource(id = R.string.woo_shipping_label_company),
                    text = editableAddress.company.value,
                    onTextChange = onCompanyChange,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            addressFocusRequester.requestFocus()
                        }
                    ),
                    modifier = Modifier.padding(top = 8.dp),
                    focusRequester = companyFocusRequester
                )
            }

            RoundedBorderDropDownWithLabel(
                label = "${stringResource(id = R.string.woo_shipping_label_country)} *",
                text = editableAddress.country,
                modifier = Modifier.padding(top = 24.dp),
                onClick = {}
            )
            RoundedBorderTextFieldWithLabel(
                label = "${stringResource(id = R.string.woo_shipping_label_address)} *",
                text = editableAddress.address.value,
                error = editableAddress.address.error,
                onTextChange = onAddressChange,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        cityFocusRequester.requestFocus()
                    }
                ),
                focusRequester = addressFocusRequester,
                modifier = Modifier.padding(top = 8.dp)
            )
            RoundedBorderTextFieldWithLabel(
                label = "${stringResource(id = R.string.woo_shipping_label_city)} *",
                text = editableAddress.city.value,
                error = editableAddress.city.error,
                onTextChange = onCityChange,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = {
                        postalCodeFocusRequester.requestFocus()
                    }
                ),
                focusRequester = cityFocusRequester,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row {
                RoundedBorderDropDownWithLabel(
                    label = "${stringResource(id = R.string.woo_shipping_label_state)} *",
                    text = editableAddress.state,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .weight(1f),
                    onClick = {}
                )
                Spacer(modifier = Modifier.size(8.dp))
                RoundedBorderTextFieldWithLabel(
                    label = "${stringResource(id = R.string.woo_shipping_label_post_code)} *",
                    text = editableAddress.postalCode.value,
                    error = editableAddress.postalCode.error,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = {
                            emailFocusRequester.requestFocus()
                        }
                    ),
                    focusRequester = postalCodeFocusRequester,
                    onTextChange = onPostalCodeChange,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .weight(1f)
                )
            }

            RoundedBorderTextFieldWithLabel(
                label = "${stringResource(id = R.string.woo_shipping_label_email)} *",
                text = editableAddress.email.value,
                error = editableAddress.email.error,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                onTextChange = onEmailChange,
                keyboardActions = KeyboardActions(
                    onNext = {
                        phoneFocusRequester.requestFocus()
                    }
                ),
                focusRequester = emailFocusRequester,
                modifier = Modifier.padding(top = 32.dp)
            )
            RoundedBorderTextFieldWithLabel(
                label = "${stringResource(id = R.string.woo_shipping_label_phone)} *",
                text = editableAddress.phone.value,
                error = editableAddress.phone.error,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        phoneFocusRequester.freeFocus()
                        keyboardController?.hide()
                    }
                ),
                focusRequester = phoneFocusRequester,
                onTextChange = onPhoneChange,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
internal fun RoundedBorderTextFieldWithLabel(
    label: String,
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
    error: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    focusRequester: FocusRequester? = null,
    endSection: @Composable () -> Unit = {}
) {
    val hasError = error.isNotNullOrEmpty()
    val color = if (hasError) {
        MaterialTheme.colors.error
    } else {
        colorResource(R.color.divider_color)
    }

    val textFieldModifier = if (focusRequester != null) {
        Modifier
            .focusable()
            .focusRequester(focusRequester)
    } else {
        Modifier
    }

    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        RoundedCornerBoxWithBorder(
            borderColor = color
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BasicTextField(
                        modifier = textFieldModifier.fillMaxWidth(),
                        value = text,
                        onValueChange = onTextChange,
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        textStyle = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface),
                    )
                    if (text.isEmpty()) {
                        Text(
                            text = hint,
                            style = MaterialTheme.typography.body2,
                            color = colorResource(id = R.color.color_on_surface_disabled)
                        )
                    }
                }
                endSection()
            }
        }
        error?.let {
            Text(
                text = error,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Preview
@Composable
private fun RoundedBorderTextFieldWithLabelPreview() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        RoundedBorderTextFieldWithLabel(
            label = "Label",
            text = "123",
            onTextChange = {},
            hint = "Hint"
        )
        Spacer(modifier = Modifier.padding(8.dp))
        RoundedBorderTextFieldWithLabel(
            label = "Empty text field",
            text = "",
            onTextChange = {},
            hint = "Hint"
        )
        Spacer(modifier = Modifier.padding(8.dp))
        RoundedBorderTextFieldWithLabel(
            label = "Required field",
            text = "",
            onTextChange = {},
            hint = "Hint",
            error = "This field is required"
        )
    }
}

@Composable
private fun RoundedBorderDropDownWithLabel(
    label: String,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        RoundedCornerBoxWithBorder(innerModifier = Modifier.clickable { onClick() }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}

@Preview
@Composable
private fun RoundedBorderDropDownWithLabelPreview() {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colors.background)
            .padding(16.dp)
    ) {
        RoundedBorderDropDownWithLabel(
            label = "Label",
            text = "Text",
            onClick = {}
        )
    }
}

@Composable
private fun CollapsedField(
    label: String,
    isExpanded: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedContent(targetState = isExpanded, label = "expand_field_animation") { value ->
        if (value) {
            content()
        } else {
            Row(
                modifier = modifier
                    .clickable { onExpand() }
                    .padding(vertical = 16.dp, horizontal = 8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary
                )
                Text(
                    text = stringResource(id = R.string.add, label.lowercase()),
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(start = 8.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
