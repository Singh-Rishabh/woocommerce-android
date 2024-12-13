package com.woocommerce.android.ui.woopos.common.composeui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextFieldColors
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.compose.component.BigDecimalTextFieldValueMapper
import com.woocommerce.android.ui.compose.component.TextFieldValueMapper
import com.woocommerce.android.ui.compose.component.WCOutlinedTypedTextField
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding
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
        focusedLabelColor = MaterialTheme.colors.onBackground.copy(alpha = 0.8f),
    ),
    placeholderText: String? = null
) {
    Column(modifier = modifier) {
        WCOutlinedTypedTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
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

        val errorTextStyle = MaterialTheme.typography.subtitle2
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                fontWeight = FontWeight.Normal,
                style = errorTextStyle,
                textAlign = TextAlign.Start,
                modifier = Modifier
                    .padding(start = 16.dp.toAdaptivePadding())
            )
        }
    }
}

@Composable
fun WooPosInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    textStyle: TextStyle = MaterialTheme.typography.h6,
    textColor: Color = MaterialTheme.colors.onBackground,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    contentAlignment: Alignment = Alignment.CenterStart,
    modifier: Modifier = Modifier,
) {
    var labelWidth by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .background(Color.Transparent),
        contentAlignment = contentAlignment,
    ) {
        if (value.isEmpty()) {
            Text(
                text = label,
                style = textStyle.copy(color = textColor.copy(alpha = 0.2f)),
                maxLines = 1,
                softWrap = false,
                modifier = Modifier
                    .onGloballyPositioned { coordinates ->
                        labelWidth = coordinates.size.width
                    }
            )
        }

        val density = LocalDensity.current

        // that's workaround to keep cursor to the left from the label
        val textFieldModifier = if (value.isEmpty()) {
            Modifier.width(with(density) { labelWidth.toDp() + 4.dp })
        } else {
            Modifier.width(IntrinsicSize.Min)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            textStyle = textStyle.copy(color = textColor),
            singleLine = true,
            keyboardActions = keyboardActions,
            keyboardOptions = keyboardOptions,
            modifier = textFieldModifier,
            cursorBrush = SolidColor(MaterialTheme.colors.onBackground),
        )
    }
}

@WooPosPreview
@Composable
fun WooPosTypedInputFieldPreview() {
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

@WooPosPreview
@Composable
fun WooPosInputFieldPreview() {
    WooPosTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WooPosInputField(
                value = "longemail@gmail.com",
                onValueChange = {},
                textStyle = MaterialTheme.typography.h3,
                contentAlignment = Alignment.Center
            )

            Spacer(modifier = Modifier.size(8.dp))

            WooPosInputField(
                value = "",
                onValueChange = {},
                textStyle = MaterialTheme.typography.h3,
                label = "Label Label",
            )
        }
    }
}
