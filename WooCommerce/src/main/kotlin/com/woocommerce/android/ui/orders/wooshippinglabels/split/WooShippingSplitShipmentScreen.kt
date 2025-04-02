package com.woocommerce.android.ui.orders.wooshippinglabels.split

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.annotatedStringRes
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
            onDismissInstructions = viewModel::onDismissInstructions,
            onUpdateSelection = viewModel::onUpdateSelection,
            modifier = modifier
        )
    }
}

@Composable
fun WooShippingSplitShipmentScreen(
    viewState: SplitShipmentViewState,
    onBack: () -> Unit,
    onDismissInstructions: () -> Unit,
    onUpdateSelection: (shipmentKey: Int, index: Int, selectedIndexes: Set<Int>?) -> Unit,
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
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Box(
                modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, top = 16.dp, end = 16.dp)
            ) {
                Column {
                    ProductsSummary(
                        totalItems = shipment.shippableItems.size,
                        totalWeight = shipment.formattedTotalWeight,
                        totalPrice = shipment.formattedTotalPrice,
                        modifier = modifier.padding(top = 8.dp, bottom = 8.dp)
                    )

                    LazyColumn {
                        itemsIndexed(
                            items = shipment.shippableItems,
                        ) { index, shippableItem ->
                            when (shippableItem) {
                                is SelectableShippableItemUI.SingleSelectableShippableItemUI -> {
                                    SelectableShippingProduct(
                                        title = shippableItem.shippableItem.title,
                                        description = shippableItem.shippableItem.formattedSize,
                                        weight = shippableItem.shippableItem.formattedWeight,
                                        price = shippableItem.shippableItem.formattedPrice,
                                        quantity = shippableItem.shippableItem.quantity,
                                        imageUrl = shippableItem.shippableItem.imageUrl,
                                        isSelected = shippableItem.isSelected,
                                        onSelectionChange = {
                                            onUpdateSelection(
                                                viewState.shipmentSelected,
                                                index,
                                                null
                                            )
                                        },
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
                                        isSelected = shippableItem.isSelected,
                                        onSelectionChange = {
                                            onUpdateSelection(
                                                viewState.shipmentSelected,
                                                index,
                                                null
                                            )
                                        },
                                        isExpanded = expanded,
                                        onExpand = { expanded = !expanded },
                                        singleWeight = shippableItem.innerShippableItem.formattedWeight,
                                        singlePrice = shippableItem.innerShippableItem.formattedPrice,
                                        selectedIndexes = shippableItem.selectedIndexes,
                                        onInnerSelectionChange = { isSelected, innerIndex ->
                                            val indexes = shippableItem.selectedIndexes.toMutableSet()
                                            if (isSelected) indexes.remove(innerIndex) else indexes.add(innerIndex)

                                            onUpdateSelection(
                                                viewState.shipmentSelected,
                                                index,
                                                indexes
                                            )
                                        },
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                        if (viewState.splitMessage != null || viewState.splitMovements.isNotEmpty()) {
                            item {
                                when {
                                    viewState.splitMessage != null && viewState.splitMovements.isNotEmpty()
                                    -> Spacer(modifier = Modifier.padding(bottom = 240.dp))
                                    else -> Spacer(modifier = Modifier.padding(bottom = 120.dp))
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 16.dp)) {
                    AnimatedVisibility(
                        visible = viewState.splitMessage != null,
                        label = "message_transition",
                        enter = slideInVertically(initialOffsetY = { it })
                    ) {
                        when (viewState.splitMessage) {
                            is SplitShipmentMessage.Instructions -> {
                                InstructionsMessage(
                                    message = annotatedStringRes(R.string.woo_shipping_split_shipment_instructions),
                                    onClose = onDismissInstructions
                                )
                            }

                            is SplitShipmentMessage.Success -> TODO()
                            null -> {}
                        }
                    }
                    AnimatedVisibility(
                        visible = viewState.splitMovements.isNotEmpty(),
                        label = "movements_transition",
                        enter = slideInVertically(initialOffsetY = { it })
                    ) {
                        SplitMovements(
                            movements = viewState.splitMovements,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InstructionsMessage(
    message: AnnotatedString,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        backgroundColor = colorResource(id = R.color.woo_message_surface),
        modifier = modifier,
        shape = RoundedCornerShape(corner = CornerSize(8.dp))
    ) {
        Row {
            Text(
                text = message,
                color = MaterialTheme.colors.onPrimary,
                modifier = Modifier
                    .weight(1f, true)
                    .padding(start = 16.dp, top = 16.dp, bottom = 16.dp)
            )
            IconButton(onClick = { onClose() }) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    tint = MaterialTheme.colors.onPrimary.copy(alpha = .60f),
                    contentDescription = stringResource(id = R.string.close),
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun SplitMovements(
    movements: List<SplitMovements>,
    modifier: Modifier = Modifier
) {
    if (movements.isNotEmpty()) {
        Card(
            backgroundColor = MaterialTheme.colors.surface,
            modifier = modifier,
            shape = RoundedCornerShape(corner = CornerSize(8.dp)),
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.selection_count, movements.first().totalItemsToMove),
                    color = MaterialTheme.colors.onSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                WCTextButton(
                    text = stringResource(R.string.woo_shipping_split_move_to_new),
                    onClick = { }
                )
            }
        }
    }
}
