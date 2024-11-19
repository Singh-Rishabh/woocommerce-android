package com.woocommerce.android.ui.woopos.home.items.variations

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import com.woocommerce.android.R
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
                top = 30.dp.toAdaptivePadding(),
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
    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val (backButton, productName, variationsCount) = createRefs()

        IconButton(
            onClick = onBackClicked,
            modifier = Modifier.constrainAs(backButton) {
                start.linkTo(parent.start)
                top.linkTo(productName.top)
                bottom.linkTo(productName.bottom)
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(id = R.string.woopos_variations_back_content_description),
                tint = MaterialTheme.colors.onSurface
            )
        }

        Text(
            text = variableProductData.name,
            style = MaterialTheme.typography.h4.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.constrainAs(productName) {
                start.linkTo(backButton.end, margin = 8.dp)
                top.linkTo(parent.top, margin = 8.dp)
            }
        )

        Text(
            text = "${variableProductData.numOfVariations} variations",
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.constrainAs(variationsCount) {
                start.linkTo(productName.start)
                top.linkTo(productName.bottom, margin = 4.dp)
            }
        )
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
