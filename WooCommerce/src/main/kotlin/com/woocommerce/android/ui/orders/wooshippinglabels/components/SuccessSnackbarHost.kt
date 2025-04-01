package com.woocommerce.android.ui.orders.wooshippinglabels.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R

@Composable
fun SuccessSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            val actionLabel = snackbarData.visuals.actionLabel

            SuccessSnackbar(
                content = snackbarData.visuals.message,
                actionLabel = actionLabel,
                action =  { snackbarData.performAction() }
            )
        }
    )
}

@Composable
private fun SuccessSnackbar(
    content: String,
    actionLabel: String?,
    action: () -> Unit
) {
    Surface(
        color = SnackbarDefaults.color,
        shape = RoundedCornerShape(4.dp),
        modifier = Modifier.padding(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = colorResource(R.color.woo_green_20),
                modifier = Modifier.padding(end = 16.dp)
            )

            Text(
                text = content,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = SnackbarDefaults.contentColor
            )

            if (actionLabel != null) {
                TextButton(
                    onClick = action,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = SnackbarDefaults.actionContentColor
                    )
                ) {
                    Text(text = actionLabel)
                }
            }
        }
    }
}

@Preview
@Composable
fun SuccessSnackbarHostPreview() {
    SuccessSnackbar(
        content = "Shipping label created successfully",
        actionLabel = "View",
        action = {}
    )
}
