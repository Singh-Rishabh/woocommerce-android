package com.woocommerce.android.ui.woopos.home.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.ShadowType
import com.woocommerce.android.ui.woopos.common.composeui.WooPosCard
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.component.Button
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosErrorScreen
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosLazyColumn
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosShimmerBox
import com.woocommerce.android.ui.woopos.common.composeui.toAdaptivePadding
import com.woocommerce.android.ui.woopos.home.items.WooPosItem.SimpleProduct
import com.woocommerce.android.ui.woopos.home.items.WooPosItem.VariableProduct
import com.woocommerce.android.ui.woopos.home.items.WooPosItemNavigationData.VariableProductData
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsUIEvent.EndOfItemsListReached
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsUIEvent.ItemClicked
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsUIEvent.ProductsLoadingErrorRetryButtonClicked
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsUIEvent.PullToRefreshTriggered
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WooPosItemsScreen(modifier: Modifier = Modifier) {
    val productsViewModel: WooPosItemsViewModel = hiltViewModel()
    WooPosItemsScreen(
        modifier = modifier,
        itemsStateFlow = productsViewModel.viewState,
        navigationState = productsViewModel.navigationState,
        onItemClicked = {
            when (it) {
                is SimpleProduct -> productsViewModel.onUIEvent(ItemClicked(it))
                is VariableProduct -> productsViewModel.onUIEvent(
                    WooPosItemsUIEvent.NavigateToVariationsScreen(
                        VariableProductData(
                            id = it.id,
                            name = it.name,
                            numOfVariations = it.numOfVariations,
                            variationIds = it.variationIds
                        )
                    )
                )
            }
        },
        onEndOfItemListReached = { productsViewModel.onUIEvent(EndOfItemsListReached) },
        onPullToRefresh = { productsViewModel.onUIEvent(PullToRefreshTriggered) },
        onRetryClicked = { productsViewModel.onUIEvent(ProductsLoadingErrorRetryButtonClicked) },
        onSimpleProductsBannerClosed = {
            productsViewModel.onUIEvent(WooPosItemsUIEvent.SimpleProductsBannerClosed)
        },
        onSimpleProductsBannerLearnMoreClicked = {
            productsViewModel.onUIEvent(WooPosItemsUIEvent.SimpleProductsBannerLearnMoreClicked)
        },
        onToolbarInfoIconClicked = {
            productsViewModel.onUIEvent(WooPosItemsUIEvent.SimpleProductsDialogInfoIconClicked)
        },
        onNavigateBackClicked = {
            productsViewModel.navigateBackToItemListScreen()
        }
    )
}

@ExperimentalMaterialApi
@Composable
private fun WooPosItemsScreen(
    modifier: Modifier = Modifier,
    itemsStateFlow: StateFlow<WooPosItemsViewState>,
    navigationState: StateFlow<WooPosItemsNavigator.NavigationState>,
    onItemClicked: (item: WooPosItem) -> Unit,
    onEndOfItemListReached: () -> Unit,
    onPullToRefresh: () -> Unit,
    onRetryClicked: () -> Unit,
    onSimpleProductsBannerClosed: () -> Unit,
    onSimpleProductsBannerLearnMoreClicked: () -> Unit,
    onToolbarInfoIconClicked: () -> Unit,
    onNavigateBackClicked: () -> Unit,
) {
    val state = itemsStateFlow.collectAsState()
    val currentNavigationState = navigationState.collectAsState()
    val pullToRefreshState = rememberPullRefreshState(state.value.reloadingProductsWithPullToRefresh, onPullToRefresh)

    Box(modifier = modifier.fillMaxSize()) {
        Crossfade(targetState = currentNavigationState.value, label = "") { navigationState ->
            when (navigationState) {
                is WooPosItemsNavigator.NavigationState.ItemListScreen -> {
                    MainItemsList(
                        modifier = modifier,
                        pullToRefreshState = pullToRefreshState,
                        state = state,
                        onToolbarInfoIconClicked = onToolbarInfoIconClicked,
                        onSimpleProductsBannerLearnMoreClicked = onSimpleProductsBannerLearnMoreClicked,
                        onSimpleProductsBannerClosed = onSimpleProductsBannerClosed,
                        onItemClicked = onItemClicked,
                        onEndOfItemListReached = onEndOfItemListReached,
                        onRetryClicked = onRetryClicked
                    )
                }

                is WooPosItemsNavigator.NavigationState.VariationsScreen -> {
                    NavigateToVariationsScreen(
                        variableProductData = navigationState.product,
                        modifier = modifier,
                        onBackClicked = { onNavigateBackClicked() }
                    )
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun MainItemsList(
    modifier: Modifier,
    pullToRefreshState: PullRefreshState,
    state: State<WooPosItemsViewState>,
    onToolbarInfoIconClicked: () -> Unit,
    onSimpleProductsBannerLearnMoreClicked: () -> Unit,
    onSimpleProductsBannerClosed: () -> Unit,
    onItemClicked: (item: WooPosItem) -> Unit,
    onEndOfItemListReached: () -> Unit,
    onRetryClicked: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .pullRefresh(pullToRefreshState)
            .padding(
                start = 16.dp.toAdaptivePadding(),
                end = 16.dp.toAdaptivePadding(),
                top = 40.dp.toAdaptivePadding(),
                bottom = 0.dp.toAdaptivePadding(),
            )
    ) {
        Column(
            modifier.fillMaxHeight()
        ) {
            val titleColor = when (state.value) {
                is WooPosItemsViewState.Loading,
                is WooPosItemsViewState.Empty,
                is WooPosItemsViewState.Error -> MaterialTheme.colors.onSurface.copy(alpha = 0.6f)

                is WooPosItemsViewState.Content -> MaterialTheme.colors.onSurface
            }
            ItemsToolbar(state.value, titleColor, onToolbarInfoIconClicked)

            Spacer(modifier = Modifier.height(24.dp))

            when (val itemsState = state.value) {
                is WooPosItemsViewState.Content -> {
                    Column {
                        SimpleProductsBanner(
                            itemsState.bannerState,
                            onSimpleProductsBannerLearnMoreClicked,
                            onSimpleProductsBannerClosed
                        )
                        ItemsList(
                            itemsState,
                            onItemClicked,
                            onEndOfItemListReached,
                        )
                    }
                }

                is WooPosItemsViewState.Loading -> ItemsLoadingIndicator()

                is WooPosItemsViewState.Empty -> ProductsEmptyList()

                is WooPosItemsViewState.Error -> ProductsError { onRetryClicked() }
            }
        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = state.value.reloadingProductsWithPullToRefresh,
            state = pullToRefreshState
        )
    }
}

@Composable
private fun NavigateToVariationsScreen(
    variableProductData: VariableProductData,
    modifier: Modifier,
    onBackClicked: () -> Unit,
) {
    WooPosVariationsScreen(
        modifier,
        variableProductData,
        onBackClicked = onBackClicked
    )
}

@Composable
private fun ItemsToolbar(
    productViewState: WooPosItemsViewState,
    titleColor: Color,
    onToolbarInfoIconClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = stringResource(id = R.string.woopos_products_screen_title),
            style = MaterialTheme.typography.h4,
            fontWeight = FontWeight.Bold,
            color = titleColor,
        )
        when (productViewState) {
            is WooPosItemsViewState.Content -> {
                if (productViewState.bannerState.isBannerHiddenByUser) {
                    IconButton(
                        modifier = Modifier.size(40.dp),
                        onClick = {
                            onToolbarInfoIconClicked()
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.info),
                            contentDescription = stringResource(
                                id = R.string.woopos_banner_simple_products_info_content_description
                            ),
                            tint = MaterialTheme.colors.onSurface.copy(ContentAlpha.high),
                        )
                    }
                }
            }

            else -> {
                // no op
            }
        }
    }
}

@Composable
private fun SimpleProductsBanner(
    bannerState: WooPosItemsViewState.Content.BannerState,
    onSimpleProductsBannerLearnMoreClicked: () -> Unit,
    onSimpleProductsBannerClosed: () -> Unit
) {
    AnimatedVisibility(
        visible = !bannerState.isBannerHiddenByUser,
        exit = shrinkVertically(),
    ) {
        WooPosBanner(
            title = stringResource(id = bannerState.title),
            message = stringResource(id = bannerState.message),
            bannerIcon = R.drawable.info,
            onClose = {
                onSimpleProductsBannerClosed()
            },
            onLearnMore = {
                onSimpleProductsBannerLearnMoreClicked()
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemsList(
    state: WooPosItemsViewState.Content,
    onItemClicked: (item: WooPosItem) -> Unit,
    onEndOfProductsListReached: () -> Unit,
) {
    val listState = rememberLazyListState()
    WooPosLazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(2.dp),
        state = listState,
    ) {
        items(
            state.items,
            key = { product -> product.id }
        ) { product ->
            when (product) {
                is SimpleProduct -> {
                    ProductItem(
                        modifier = Modifier.animateItemPlacement(),
                        item = product,
                        onItemClicked = onItemClicked
                    )
                }

                is VariableProduct -> {
                    VariableProductItem(
                        modifier = Modifier.animateItemPlacement(),
                        item = product,
                        onItemClicked = onItemClicked
                    )
                }
            }
        }

        if (state.loadingMore) {
            item {
                ItemsLoadingItem()
            }
        }
        item {
            Spacer(modifier = Modifier.height(104.dp))
        }
    }
    InfiniteListHandler(listState, state) {
        onEndOfProductsListReached()
    }
}

@Composable
fun ItemsLoadingIndicator() {
    WooPosLazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(2.dp),
    ) {
        items(10) {
            ItemsLoadingItem()
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ItemsLoadingItem() {
    WooPosCard(
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 6.dp,
        shadowType = ShadowType.Soft,
    ) {
        Row(
            modifier = Modifier
                .height(112.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(WooPosTheme.colors.loadingSkeleton)
            )

            Spacer(modifier = Modifier.width(32.dp))

            WooPosShimmerBox(
                modifier = Modifier
                    .weight(1f)
                    .height(30.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(184.dp))

            WooPosShimmerBox(
                modifier = Modifier
                    .height(30.dp)
                    .width(76.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Spacer(modifier = Modifier.width(24.dp))
        }
    }
}

@Composable
private fun ProductItem(
    modifier: Modifier = Modifier,
    item: SimpleProduct,
    onItemClicked: (item: WooPosItem) -> Unit
) {
    val itemContentDescription = stringResource(
        id = R.string.woopos_cart_item_content_description,
        item.name,
        item.price
    )
    ItemCard(modifier, itemContentDescription, onItemClicked, item)
}

@Composable
private fun VariableProductItem(
    modifier: Modifier = Modifier,
    item: VariableProduct,
    onItemClicked: (item: WooPosItem) -> Unit
) {
    val itemContentDescription = stringResource(
        id = R.string.woopos_cart_item_content_description,
        item.name,
        item.price
    )
    ItemCard(modifier, itemContentDescription, onItemClicked, item)
}

@Composable
private fun ItemCard(
    modifier: Modifier,
    itemContentDescription: String,
    onItemClicked: (item: WooPosItem) -> Unit,
    item: WooPosItem
) {
    WooPosCard(
        modifier = modifier
            .semantics { contentDescription = itemContentDescription },
        shape = RoundedCornerShape(8.dp),
        backgroundColor = MaterialTheme.colors.surface,
        elevation = 6.dp,
        shadowType = ShadowType.Soft,
    ) {
        Row(
            modifier = Modifier
                .clickable { onItemClicked(item) }
                .height(112.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(item)

            Spacer(modifier = Modifier.width(32.dp))

            ProductInfo(item)
        }
    }
}

@Composable
private fun ProductInfo(item: WooPosItem) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = item.name,
            style = MaterialTheme.typography.h5,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(8.dp))
        when (item) {
            is SimpleProduct -> SimpleProductDetails(item = item)
            is VariableProduct -> VariableProductDetails(item = item)
        }
    }
}

@Composable
private fun ProductImage(item: WooPosItem) {
    val imageUrl = when (item) {
        is SimpleProduct -> item.imageUrl
        is VariableProduct -> item.imageUrl
    }

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        fallback = ColorPainter(WooPosTheme.colors.loadingSkeleton),
        error = ColorPainter(WooPosTheme.colors.loadingSkeleton),
        placeholder = ColorPainter(WooPosTheme.colors.loadingSkeleton),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.size(112.dp)
    )
}

@Composable
private fun SimpleProductDetails(item: SimpleProduct) {
    Text(
        text = item.price,
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Normal
    )
}

@Composable
private fun VariableProductDetails(item: VariableProduct) {
    Text(
        text = "${item.numOfVariations} Variations",
        style = MaterialTheme.typography.h6,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun ProductsEmptyList() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier.size(104.dp),
                imageVector = ImageVector.vectorResource(id = R.drawable.woo_pos_ic_empty_products),
                contentDescription = stringResource(id = R.string.woopos_products_empty_list_image_description),
            )

            Spacer(modifier = Modifier.height(40.dp.toAdaptivePadding()))

            Text(
                text = stringResource(id = R.string.woopos_products_empty_list_title),
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
            )

            Spacer(modifier = Modifier.height(16.dp.toAdaptivePadding()))

            Text(
                text = stringResource(id = R.string.woopos_products_empty_list_message),
                style = MaterialTheme.typography.h5,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp.toAdaptivePadding()))
        }
    }
}

@Composable
fun ProductsError(onRetryClicked: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        WooPosErrorScreen(
            modifier = Modifier.width(640.dp),
            message = stringResource(id = R.string.woopos_products_loading_error_title),
            reason = stringResource(id = R.string.woopos_products_loading_error_message),
            primaryButton = Button(
                text = stringResource(id = R.string.woopos_products_loading_error_retry_button),
                click = onRetryClicked
            )
        )
    }
}

@Composable
private fun InfiniteListHandler(
    listState: LazyListState,
    state: WooPosItemsViewState.Content,
    onEndOfProductsListReached: () -> Unit
) {
    val buffer = 5
    val loadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItemsNumber = layoutInfo.totalItemsCount
            val lastVisibleItemIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1

            lastVisibleItemIndex > (totalItemsNumber - buffer)
        }
    }

    LaunchedEffect(state.reloadingProductsWithPullToRefresh) {
        snapshotFlow { loadMore.value }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                onEndOfProductsListReached()
            }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@WooPosPreview
fun WooPosItemsScreenPreview(modifier: Modifier = Modifier) {
    val productState = MutableStateFlow(
        WooPosItemsViewState.Content(
            items = listOf(
                SimpleProduct(
                    1,
                    name = "Product 1, Product 1, Product 1, " +
                        "Product 1, Product 1, Product 1, Product 1, Product 1" +
                        "Product 1, Product 1, Product 1, Product 1, Product 1",
                    price = "10.0$",
                    imageUrl = null,
                ),
                SimpleProduct(
                    2,
                    name = "Product 2",
                    price = "2000.00$",
                    imageUrl = null,
                ),
                VariableProduct(
                    3,
                    name = "Product 3",
                    price = "2000.00$",
                    imageUrl = null,
                    numOfVariations = 20,
                    variationIds = listOf()
                ),
                SimpleProduct(
                    4,
                    name = "Product 4",
                    price = "1.0$",
                    imageUrl = null,
                ),
            ),
            loadingMore = true,
            reloadingProductsWithPullToRefresh = true,
            bannerState = WooPosItemsViewState.Content.BannerState(
                isBannerHiddenByUser = true,
                title = R.string.woopos_banner_simple_products_only_title,
                message = R.string.woopos_banner_simple_products_only_message,
                icon = R.drawable.info,
            ),
        )
    )
    WooPosTheme {
        WooPosItemsScreen(
            modifier = modifier,
            itemsStateFlow = productState,
            navigationState = MutableStateFlow(WooPosItemsNavigator.NavigationState.ItemListScreen),
            onItemClicked = {},
            onEndOfItemListReached = {},
            onPullToRefresh = {},
            onRetryClicked = {},
            onSimpleProductsBannerClosed = {},
            onSimpleProductsBannerLearnMoreClicked = {},
            onToolbarInfoIconClicked = {},
            onNavigateBackClicked = {},
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@WooPosPreview
fun WooPosItemsScreenLoadingPreview() {
    val productState = MutableStateFlow(
        WooPosItemsViewState.Loading(
            reloadingProductsWithPullToRefresh = true,
            withCart = false
        )
    )
    WooPosTheme {
        WooPosItemsScreen(
            itemsStateFlow = productState,
            navigationState = MutableStateFlow(WooPosItemsNavigator.NavigationState.ItemListScreen),
            onItemClicked = {},
            onEndOfItemListReached = {},
            onPullToRefresh = {},
            onRetryClicked = {},
            onSimpleProductsBannerClosed = {},
            onSimpleProductsBannerLearnMoreClicked = {},
            onToolbarInfoIconClicked = {},
            onNavigateBackClicked = {},
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@WooPosPreview
fun WooPosProductsScreenEmptyListPreview() {
    val productState = MutableStateFlow(WooPosItemsViewState.Empty(true))
    WooPosTheme {
        WooPosItemsScreen(
            itemsStateFlow = productState,
            navigationState = MutableStateFlow(WooPosItemsNavigator.NavigationState.ItemListScreen),
            onItemClicked = {},
            onEndOfItemListReached = {},
            onPullToRefresh = {},
            onRetryClicked = {},
            onSimpleProductsBannerClosed = {},
            onSimpleProductsBannerLearnMoreClicked = {},
            onToolbarInfoIconClicked = {},
            onNavigateBackClicked = {},
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@WooPosPreview
fun WooPosProductsScreenErrorPreview() {
    val productState = MutableStateFlow(WooPosItemsViewState.Error())
    WooPosTheme {
        WooPosItemsScreen(
            itemsStateFlow = productState,
            navigationState = MutableStateFlow(WooPosItemsNavigator.NavigationState.ItemListScreen),
            onItemClicked = {},
            onEndOfItemListReached = {},
            onPullToRefresh = {},
            onRetryClicked = {},
            onSimpleProductsBannerClosed = {},
            onSimpleProductsBannerLearnMoreClicked = {},
            onToolbarInfoIconClicked = {},
            onNavigateBackClicked = {},
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@WooPosPreview
fun WooPosHomeScreenItemsWithSimpleProductsOnlyBannerPreview() {
    val productState = MutableStateFlow(
        WooPosItemsViewState.Content(
            items = listOf(
                SimpleProduct(
                    1,
                    name = "Product 1, Product 1, Product 1, " +
                        "Product 1, Product 1, Product 1, Product 1, Product 1" +
                        "Product 1, Product 1, Product 1, Product 1, Product 1",
                    price = "10.0$",
                    imageUrl = null,
                ),
                SimpleProduct(
                    2,
                    name = "Product 2",
                    price = "2000.00$",
                    imageUrl = null,
                ),
                SimpleProduct(
                    3,
                    name = "Product 3",
                    price = "1.0$",
                    imageUrl = null,
                ),
            ),
            loadingMore = false,
            reloadingProductsWithPullToRefresh = true,
            bannerState = WooPosItemsViewState.Content.BannerState(
                isBannerHiddenByUser = false,
                title = R.string.woopos_banner_simple_products_only_title,
                message = R.string.woopos_banner_simple_products_only_message,
                icon = R.drawable.info,
            )
        )
    )
    WooPosTheme {
        WooPosItemsScreen(
            itemsStateFlow = productState,
            navigationState = MutableStateFlow(WooPosItemsNavigator.NavigationState.ItemListScreen),
            onItemClicked = {},
            onEndOfItemListReached = {},
            onPullToRefresh = {},
            onRetryClicked = {},
            onSimpleProductsBannerClosed = {},
            onSimpleProductsBannerLearnMoreClicked = {},
            onToolbarInfoIconClicked = {},
            onNavigateBackClicked = {},
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
@WooPosPreview
fun WooPosHomeScreenItemsWithInfoIconInToolbarPreview() {
    val productState = MutableStateFlow(
        WooPosItemsViewState.Content(
            items = listOf(
                SimpleProduct(
                    1,
                    name = "Product 1, Product 1, Product 1, " +
                        "Product 1, Product 1, Product 1, Product 1, Product 1" +
                        "Product 1, Product 1, Product 1, Product 1, Product 1",
                    price = "10.0$",
                    imageUrl = null,
                ),
                SimpleProduct(
                    2,
                    name = "Product 2",
                    price = "2000.00$",
                    imageUrl = null,
                ),
                SimpleProduct(
                    3,
                    name = "Product 3",
                    price = "1.0$",
                    imageUrl = null,
                ),
            ),
            loadingMore = false,
            reloadingProductsWithPullToRefresh = false,
            bannerState = WooPosItemsViewState.Content.BannerState(
                isBannerHiddenByUser = true,
                title = R.string.woopos_banner_simple_products_only_title,
                message = R.string.woopos_banner_simple_products_only_message,
                icon = R.drawable.info,
            )
        )
    )
    WooPosTheme {
        WooPosItemsScreen(
            itemsStateFlow = productState,
            navigationState = MutableStateFlow(WooPosItemsNavigator.NavigationState.ItemListScreen),
            onItemClicked = {},
            onEndOfItemListReached = {},
            onPullToRefresh = {},
            onRetryClicked = {},
            onSimpleProductsBannerClosed = {},
            onSimpleProductsBannerLearnMoreClicked = {},
            onToolbarInfoIconClicked = {},
            onNavigateBackClicked = {},
        )
    }
}
