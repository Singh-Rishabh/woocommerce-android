package com.cataloghub.android.ui.live

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cataloghub.android.R
import com.cataloghub.android.model.Product

@Composable
fun GoLiveScreen(
    viewModel: GoLiveViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isYouTubeConnected by viewModel.isYouTubeConnected.collectAsState()
    val listState = rememberLazyListState()
    val dragDropState = rememberDragDropState(listState) { from, to -> viewModel.onProductMoved(from, to) }

    LaunchedEffect(Unit) {
        viewModel.fetchProducts()
        viewModel.checkYouTubeConnectionStatus()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.go_live)) },
                actions = {
                    Button(onClick = { viewModel.createLiveSession() }) {
                        Text(text = stringResource(id = R.string.go_live))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Error handling
            uiState.error?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    backgroundColor = MaterialTheme.colors.error,
                    elevation = 4.dp
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colors.onError,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            var streamDetailsExpanded by remember { mutableStateOf(true) }
            var productSelectionExpanded by remember { mutableStateOf(true) }

            CollapsibleSection(
                title = stringResource(id = R.string.live_stream_title),
                expanded = streamDetailsExpanded,
                onToggle = { streamDetailsExpanded = !streamDetailsExpanded }
            ) {
                Column {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = { viewModel.onTitleChanged(it) },
                        label = { Text(text = stringResource(id = R.string.live_stream_title)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.onDescriptionChanged(it) },
                        label = { Text(text = stringResource(id = R.string.live_stream_description)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            CollapsibleSection(
                title = stringResource(id = R.string.select_products_to_showcase),
                expanded = productSelectionExpanded,
                onToggle = { productSelectionExpanded = !productSelectionExpanded }
            ) {
                ProductSelectionSection(
                    availableProducts = uiState.products,
                    selectedProducts = uiState.selectedProducts,
                    onProductSelected = { viewModel.onProductSelected(it) },
                    onProductMoved = { from, to -> viewModel.onProductMoved(from, to) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            CollapsibleSection(
                title = stringResource(id = R.string.select_platforms),
                expanded = true,
                onToggle = { }
            ) {
                PlatformSelectionSection(
                    isYouTubeConnected = isYouTubeConnected,
                    selectedPlatforms = uiState.platforms,
                    onPlatformSelected = { viewModel.onPlatformSelected(it) }
                )
            }
        }
    }
}

@Composable
fun CollapsibleSection(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onToggle)
        ) {
            Text(text = title, style = MaterialTheme.typography.h6)
            Icon(
                imageVector = if (expanded) Icons.Default.ArrowDropDown else Icons.Default.ArrowDropUp,
                contentDescription = null
            )
        }
        if (expanded) {
            content()
        }
    }
}

@Composable
fun ProductListItem(
    product: Product,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelected)
            .padding(8.dp)
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onSelected() }
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = product.name)
    }
}

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropState {
    return remember { DragDropState(lazyListState, onMove) }
}

class DragDropState(
    private val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    var isDragging by mutableStateOf(false)
        private set

    var draggedItem by mutableStateOf<LazyListItemInfo?>(null)
        private set

    var draggedDistance by mutableStateOf(0f)
        private set

    fun onDragStart(offset: androidx.compose.ui.geometry.Offset, item: Any) {
        lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { item == it.key }
            ?.also {
                draggedItem = it
                isDragging = true
            }
    }

    fun onDrag(dragAmount: androidx.compose.ui.geometry.Offset) {
        draggedDistance += dragAmount.y
        val targetItem = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull {
            draggedDistance in it.offset.toFloat()..it.offsetEnd.toFloat()
        }
        if (targetItem != null && targetItem != draggedItem) {
            onMove(draggedItem!!.index, targetItem.index)
            draggedItem = targetItem
        }
    }

    fun onDragEnd() {
        isDragging = false
        draggedDistance = 0f
    }
}


@Composable
fun ProductSelectionSection(
    availableProducts: List<Product>,
    selectedProducts: List<Product>,
    onProductSelected: (Product) -> Unit,
    onProductMoved: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (availableProducts.isNotEmpty()) {
            Text(
                text = "Available Products",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                items(availableProducts) { product ->
                    ProductListItem(
                        product = product,
                        isSelected = selectedProducts.contains(product),
                        onSelected = { onProductSelected(product) }
                    )
                }
            }
        }

        if (selectedProducts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Selected Products (${selectedProducts.size})",
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                items(selectedProducts, key = { it.remoteId }) { product ->
                    SelectedProductItem(
                        product = product,
                        onRemove = { onProductSelected(product) }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectedProductItem(
    product: Product,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = product.name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.body1
            )
            
            Text(
                text = product.priceWithCurrency ?: "",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove product",
                    tint = MaterialTheme.colors.error
                )
            }
        }
    }
}

@Composable
fun PlatformSelectionSection(
    isYouTubeConnected: Boolean,
    selectedPlatforms: List<String>,
    onPlatformSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (isYouTubeConnected) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = selectedPlatforms.contains("youtube"),
                    onCheckedChange = { onPlatformSelected("youtube") }
                )
                Text(text = "YouTube")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Connected",
                    tint = Color.Green
                )
            }
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = false,
                    onCheckedChange = { },
                    enabled = false
                )
                Text(
                    text = stringResource(id = R.string.youtube_not_connected),
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = { /* TODO: Navigate to YouTube connection */ }) {
                    Text(text = stringResource(id = R.string.connect_youtube))
                }
            }
        }
        
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = false,
                onCheckedChange = { },
                enabled = false
            )
            Text(
                text = "Facebook Live",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(id = R.string.coming_soon),
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

@Preview
@Composable
fun GoLiveScreenPreview() {
    GoLiveScreen()
}