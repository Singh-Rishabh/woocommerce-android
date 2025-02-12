package com.woocommerce.android.ui.woopos.common.composeui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.woopos.common.composeui.WooPosCornerRadius
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosSpacing
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme

@Composable
fun WooPosButton(
    modifier: Modifier = Modifier,
    text: String,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ),
    state: WooPosButtonState = WooPosButtonState.ENABLED,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(WooPosCornerRadius.Medium.value),
        enabled = state == WooPosButtonState.ENABLED,
        colors = colors,
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = WooPosSpacing.None.value,
            pressedElevation = WooPosSpacing.None.value,
            disabledElevation = WooPosSpacing.None.value,
            hoveredElevation = WooPosSpacing.None.value,
            focusedElevation = WooPosSpacing.None.value
        )
    ) {
        when (state) {
            WooPosButtonState.ENABLED,
            WooPosButtonState.DISABLED -> {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
            WooPosButtonState.LOADING -> {
                WooPosCircularLoadingIndicator(
                    modifier = Modifier.size(32.dp),
                )
            }
        }
    }
}

@Composable
fun WooPosButtonLarge(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(WooPosCornerRadius.Large.value),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = WooPosSpacing.None.value,
            pressedElevation = WooPosSpacing.None.value,
            focusedElevation = WooPosSpacing.None.value,
            hoveredElevation = WooPosSpacing.None.value,
            disabledElevation = WooPosSpacing.None.value
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun WooPosOutlinedButtonSmall(
    modifier: Modifier = Modifier,
    text: String,
    shape: RoundedCornerShape = RoundedCornerShape(WooPosCornerRadius.Small.value),
    onClick: () -> Unit,
) = WooPosOutlinedButton(
    modifier = modifier,
    shape = shape,
    content = {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    },
    onClick = onClick,
)

@Composable
fun WooPosOutlinedButton(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(WooPosCornerRadius.Small.value),
    content: @Composable RowScope.() -> Unit,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        onClick = onClick,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        shape = shape,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = WooPosSpacing.None.value,
            pressedElevation = WooPosSpacing.None.value,
            disabledElevation = WooPosSpacing.None.value,
            hoveredElevation = WooPosSpacing.None.value,
            focusedElevation = WooPosSpacing.None.value
        ),
        content = content
    )
}

@Composable
fun WooPosOutlinedButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        onClick = onClick,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.onBackground),
        shape = RoundedCornerShape(WooPosCornerRadius.Medium.value),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = WooPosSpacing.None.value,
            pressedElevation = WooPosSpacing.None.value,
            disabledElevation = WooPosSpacing.None.value,
            hoveredElevation = WooPosSpacing.None.value,
            focusedElevation = WooPosSpacing.None.value
        )
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
@WooPosPreview
fun WooPosButtonsPreview() {
    WooPosTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(WooPosSpacing.XLarge.value),
        ) {
            WooPosButtonLarge(
                text = "Button Large",
                onClick = {},
            )

            Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value))

            WooPosOutlinedButton(
                text = "Button Outlined Small",
                onClick = {},
            )

            Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value))

            WooPosButton(
                text = "Button",
                onClick = {},
            )

            Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value))

            WooPosButton(
                text = "Button Disabled",
                onClick = {},
                state = WooPosButtonState.DISABLED,
            )

            Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value))

            WooPosButton(
                text = "Button Loading",
                onClick = {},
                state = WooPosButtonState.LOADING,
            )

            Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value))

            WooPosButton(
                text = "Button Black And White",
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onBackground
                ),
                onClick = {},
            )

            Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value))

            WooPosOutlinedButton(
                text = "Button Outlined",
                onClick = {},
            )
        }
    }
}

enum class WooPosButtonState {
    ENABLED, DISABLED, LOADING
}
