package com.woocommerce.android.ui.woopos.home.items.variations

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding
import com.woocommerce.android.ui.woopos.home.items.WooPosItemNavigationData.VariableProductData

@Composable
fun WooPosVariationsScreen(
    modifier: Modifier,
    variableProductData: VariableProductData,
    onBackClicked: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = 16.dp.toAdaptivePadding(),
                end = 16.dp.toAdaptivePadding(),
                top = 40.dp.toAdaptivePadding(),
                bottom = 0.dp.toAdaptivePadding(),
            )
    ) {
        BackHandler(onBack = onBackClicked)
        Column(
            modifier = modifier.fillMaxHeight()
        ) {
            VariationsToolbar(
                variableProductData = variableProductData,
                onBackClicked = onBackClicked
            )
        }

    }
}

@Composable
private fun VariationsToolbar(
    variableProductData: VariableProductData,
    onBackClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClicked) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colors.onSurface
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = variableProductData.name,
                style = MaterialTheme.typography.h4.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colors.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${variableProductData.numOfVariations} variations",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
@WooPosPreview
fun WooPosVariationsScreenPreview() {
    WooPosTheme {
        WooPosVariationsScreen(
            modifier = Modifier,
            variableProductData = VariableProductData(
                id = 0,
                name = "Variable Product",
                numOfVariations = 20,
                variationIds = emptyList()
            )
        ) {

        }
    }
}
