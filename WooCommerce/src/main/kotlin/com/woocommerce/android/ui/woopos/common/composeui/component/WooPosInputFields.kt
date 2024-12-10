package com.woocommerce.android.ui.woopos.common.composeui.component

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.compose.component.BigDecimalTextFieldValueMapper
import com.woocommerce.android.ui.compose.component.TextFieldValueMapper
import com.woocommerce.android.ui.compose.component.WCOutlinedTypedTextField
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import java.math.BigDecimal

@Composable
fun <T> WooPosTypedInputField(
    value: T,
    onValueChange: (T) -> Unit,
    label: String,
    valueMapper: TextFieldValueMapper<T>,
    modifier: Modifier = Modifier,
    helperText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    errorMessage: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = TextFieldDefaults.outlinedTextFieldColors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        errorBorderColor = Color.Transparent,
    ),
    placeholderText: String? = null
) {
    Column(modifier = modifier) {
        WCOutlinedTypedTextField(
            value = value,
            onValueChange = onValueChange,
            label = if (value == null || value.toString().isEmpty()) "" else label,
            valueMapper = valueMapper,
            helperText = helperText,
            enabled = enabled,
            readOnly = readOnly,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = errorMessage != null,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            interactionSource = interactionSource,
            colors = colors,
            placeholderText = placeholderText,
        )

        Spacer(modifier = Modifier.size(8.dp))

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(start = 16.dp)
            )
        }
    }
}

@WooPosPreview
@Composable
fun WooPosInputFieldPreview() {
    WooPosTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            WooPosTypedInputField(
                value = BigDecimal.TEN,
                onValueChange = {},
                valueMapper = BigDecimalTextFieldValueMapper.create(true),
                label = "Label",
            )

            Spacer(modifier = Modifier.size(8.dp))

            WooPosTypedInputField(
                value = BigDecimal.TEN,
                onValueChange = {},
                valueMapper = BigDecimalTextFieldValueMapper.create(true),
                label = "",
            )

            Spacer(modifier = Modifier.size(8.dp))

            WooPosTypedInputField(
                value = BigDecimal.TEN,
                valueMapper = BigDecimalTextFieldValueMapper.create(true),
                onValueChange = {},
                label = "",
                errorMessage = "Please enter a valid amount",
            )
        }
    }
}
