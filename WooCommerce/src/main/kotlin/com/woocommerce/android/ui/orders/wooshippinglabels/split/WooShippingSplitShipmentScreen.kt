package com.woocommerce.android.ui.orders.wooshippinglabels.split

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.WCTextButton
import com.woocommerce.android.ui.orders.wooshippinglabels.ExpandableSelectableShippingProduct
import com.woocommerce.android.ui.orders.wooshippinglabels.ProductsSummary
import com.woocommerce.android.ui.orders.wooshippinglabels.SelectableShippingProduct
import com.woocommerce.android.ui.orders.wooshippinglabels.split.WooShippingSplitShipmentViewModel.SplitShipmentViewState

@Composable
fun WooShippingSplitShipmentScreen(
    viewModel: WooShippingSplitShipmentViewModel,
    modifier: Modifier = Modifier
) {
    viewModel.viewState.observeAsState().value?.let {
        WooShippingSplitShipmentScreen(
            viewState = it,
            onBack = viewModel::onNavigateBack,
            modifier = modifier
        )
    }
}

@Composable
fun WooShippingSplitShipmentScreen(
    viewState: SplitShipmentViewState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.woo_shipping_split_shipment)) },
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = R.string.close)
                        )
                    }
                },
                backgroundColor = colorResource(id = R.color.color_toolbar),
                actions = {
                    WCTextButton(
                        onClick = onBack,
                        text = stringResource(id = R.string.done)
                    )
                }
            )
        }
    ) { padding ->
        val shipment = viewState.selectableItems.values.first()
        Surface(
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
            ) {
                ProductsSummary(
                    totalItems = shipment.shippableItems.size,
                    totalWeight = shipment.formattedTotalWeight,
                    totalPrice = shipment.formattedTotalPrice,
                    modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
                )

                LazyColumn {
                    items(
                        items = shipment.shippableItems,
                    ) { shippableItem ->
                        var selection by remember { mutableStateOf(false) }
                        when (shippableItem) {
                            is SelectableShippableItemUI.SingleSelectableShippableItemUI -> {
                                SelectableShippingProduct(
                                    title = shippableItem.shippableItem.title,
                                    description = shippableItem.shippableItem.formattedSize,
                                    weight = shippableItem.shippableItem.formattedWeight,
                                    price = shippableItem.shippableItem.formattedPrice,
                                    quantity = shippableItem.shippableItem.quantity,
                                    imageUrl = shippableItem.shippableItem.imageUrl,
                                    isSelected = selection,
                                    onSelectionChange = { selection = !selection },
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            is SelectableShippableItemUI.ExpandableSelectableShippableItemUI -> {
                                var expanded by remember { mutableStateOf(false) }
                                ExpandableSelectableShippingProduct(
                                    title = shippableItem.shippableItem.title,
                                    description = shippableItem.shippableItem.formattedSize,
                                    weight = shippableItem.shippableItem.formattedWeight,
                                    price = shippableItem.shippableItem.formattedPrice,
                                    quantity = shippableItem.shippableItem.quantity,
                                    imageUrl = shippableItem.shippableItem.imageUrl,
                                    isSelected = selection,
                                    onSelectionChange = {
                                        selection = !selection
                                    },
                                    isExpanded = expanded,
                                    onExpand = { expanded = !expanded },
                                    singleWeight = shippableItem.innerShippableItem.formattedWeight,
                                    singlePrice = shippableItem.innerShippableItem.formattedPrice,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
