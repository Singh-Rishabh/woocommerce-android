package com.woocommerce.android.ui.orders.wooshippinglabels

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.WCColoredButton
import com.woocommerce.android.ui.compose.modifiers.dashedBorder
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground

@Composable
fun WooShippingLabelCreationScreen(viewModel: WooShippingLabelCreationViewModel) {
    WooShippingLabelCreationScreen(
        onSelectPackageClick = viewModel::onSelectPackageClicked,
        onPurchaseShippingLabel = viewModel::onPurchaseShippingLabel
    )
}

@Composable
fun WooShippingLabelCreationScreen(
    modifier: Modifier = Modifier,
    onSelectPackageClick: () -> Unit,
    onPurchaseShippingLabel: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LabelCreationScreenWithBottomSheet(
            modifier = modifier,
            onSelectPackageClick = onSelectPackageClick,
            onPurchaseShippingLabel = onPurchaseShippingLabel
        )
        Box(modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)) {
            Surface(elevation = 4.dp) {
                PurchaseButton(total = "$34.89", onPurchaseShippingLabel = { })
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun LabelCreationScreenWithBottomSheet(
    modifier: Modifier = Modifier,
    onSelectPackageClick: () -> Unit,
    onPurchaseShippingLabel: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    BottomSheetScaffold(
        sheetContent = {
            val markOrderComplete = remember { mutableStateOf(false) }
            ShipmentDetails(
                modifier = Modifier.padding(bottom = 74.dp),
                scaffoldState = scaffoldState,
                markOrderComplete = markOrderComplete.value,
                onMarkOrderCompleteChange = { markOrderComplete.value = it },
                onPurchaseShippingLabel = onPurchaseShippingLabel
            )
        },
        sheetPeekHeight = 132.dp,
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.shipping_label_create_title)) },
                navigationIcon = {
                    IconButton({}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                backgroundColor = colorResource(id = R.color.color_toolbar),
                elevation = 0.dp,
            )
        },
    ) { innerPadding ->
        Surface(
            modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier.verticalScroll(rememberScrollState())) {
                val isExpanded = remember { mutableStateOf(false) }
                ShippingProductsCard(
                    shippableItems = ShippableItems(
                        shippableItems = generateItems(6),
                        totalWeight = "8.5kg",
                        totalPrice = "$92.78"
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    isExpanded = isExpanded.value,
                    onExpand = { isExpanded.value = it }
                )
                HazmatCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 4.dp, end = 8.dp)
                )
                PackageCard(
                    modifier = Modifier.padding(16.dp),
                    onSelectPackageClick = onSelectPackageClick
                )
                val selected = remember { mutableStateOf<ShippingRate?>(null) }
                val signatureRequired = remember { mutableStateOf<SignatureRequired?>(null) }
                ShippingRatesCard(
                    selected = selected.value,
                    onSelectedChange = {
                        selected.value = it
                        signatureRequired.value = null
                    },
                    shippingRates = generateShippingRates(),
                    signatureRequired = signatureRequired.value,
                    onSelectedSignatureChange = { signatureRequired.value = it },
                    signatureRequiredOptions = listOf(
                        SignatureRequired("Signature Required", "$10.00"),
                        SignatureRequired("Adult Signature Required", "$15.00")
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "dark", uiMode = Configuration.UI_MODE_NIGHT_YES, device = Devices.PIXEL)
@Preview(name = "light", uiMode = Configuration.UI_MODE_NIGHT_NO, device = Devices.PIXEL)
@Composable
private fun WooShippingLabelCreationScreenPreview() {
    WooThemeWithBackground {
        WooShippingLabelCreationScreen(
            modifier = Modifier.fillMaxSize(),
            onSelectPackageClick = {},
            onPurchaseShippingLabel = {}
        )
    }
}

@Composable
internal fun HazmatCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Row(modifier = modifier.clickable { onClick() }) {
        Text(
            text = stringResource(R.string.shipping_label_hazmat_title),
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(dimensionResource(id = R.dimen.major_100))
                .align(Alignment.CenterVertically)
        )

        Text(
            text = stringResource(R.string.no),
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.color_on_surface_medium),
            modifier = Modifier
                .align(Alignment.CenterVertically)
        )

        Icon(
            painter = painterResource(R.drawable.ic_arrow_right),
            tint = colorResource(id = R.color.color_on_surface_medium),
            contentDescription =
            stringResource(id = R.string.shipping_label_package_details_items_expand_content_description),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(end = dimensionResource(R.dimen.minor_50))
        )
    }
}

@Preview
@Composable
private fun HazmatCardPreview() {
    WooThemeWithBackground {
        HazmatCard(modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun PackageCard(
    modifier: Modifier = Modifier,
    onSelectPackageClick: () -> Unit
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colors.surface,
                shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_large))
            )
            .dashedBorder(
                color = colorResource(R.color.divider_color),
                strokeWidth = 2.dp,
                dashLength = 8.dp,
                gapLength = 8.dp,
                shape = RoundedCornerShape(dimensionResource(R.dimen.corner_radius_large))
            )
            .padding(dimensionResource(id = R.dimen.major_200))
    ) {
        WCColoredButton(
            onClick = onSelectPackageClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.shipping_label_select_package_button))
        }
        Text(
            text = stringResource(R.string.shipping_label_select_package_title),
            style = MaterialTheme.typography.subtitle1,
            color = MaterialTheme.colors.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.major_200))
                .align(Alignment.CenterHorizontally)
        )
        Text(
            text = stringResource(R.string.shipping_label_select_package_description),
            style = MaterialTheme.typography.subtitle1,
            color = colorResource(id = R.color.color_on_surface_medium),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = dimensionResource(id = R.dimen.minor_100))
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Preview
@Composable
private fun PackageCardPreview() {
    WooThemeWithBackground {
        PackageCard(
            modifier = Modifier.padding(16.dp),
            onSelectPackageClick = {}
        )
    }
}

data class ShippableItem(
    val productId: Long,
    val title: String,
    val description: String,
    val weight: String,
    val price: String,
    val quantity: Int,
    val imageUrl: String? = null
)

data class ShippableItems(
    val shippableItems: List<ShippableItem>,
    val totalWeight: String,
    val totalPrice: String
)
