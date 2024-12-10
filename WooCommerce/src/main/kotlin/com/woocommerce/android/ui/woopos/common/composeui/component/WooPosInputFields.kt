package com.woocommerce.android.ui.woopos.common.composeui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme

@Composable
fun WooPosInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "",
    errorMessage: String? = null,
    textStyle: TextStyle = MaterialTheme.typography.h6,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                if (value.isEmpty()) {
                    Text(
                        text = label,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.4f),
                        fontStyle = textStyle.fontStyle,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            textStyle = textStyle,
            isError = errorMessage != null,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent
            ),
            modifier = Modifier
                .fillMaxWidth(),
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colors.error,
                style = MaterialTheme.typography.subtitle2,
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
            WooPosInputField(
                value = "",
                onValueChange = {},
                label = "Label",
            )

            Spacer(modifier = Modifier.size(8.dp))

            WooPosInputField(
                value = "Text",
                onValueChange = {},
                label = "",
            )

            Spacer(modifier = Modifier.size(8.dp))

            WooPosInputField(
                value = "Text",
                onValueChange = {},
                label = "",
                errorMessage = "Please enter a valid amount",
            )
        }
    }
}
