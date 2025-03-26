package com.woocommerce.android.ui.woopos.home.items.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.model.Product
import com.woocommerce.android.ui.products.ProductType
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.ParentToChildrenEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.WooPosParentToChildrenEventReceiver
import com.woocommerce.android.ui.woopos.home.items.PaginationState
import com.woocommerce.android.ui.woopos.home.items.WooPosItemSelectionViewState
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooPosItemsSearchViewModel @Inject constructor(
    val emptyStateProvider: WooPosItemsSearchEmptyStateProvider,
    private val priceFormat: WooPosFormatPrice,
    private val dataSource: WooPosSearchProductsMockedDataSource,
    private val childToParentEventSender: WooPosChildrenToParentEventSender,
    private val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver,
) : ViewModel() {
    private val _viewState =
        MutableStateFlow<WooPosItemsSearchViewState>(WooPosItemsSearchViewState.Empty)
    val viewState: StateFlow<WooPosItemsSearchViewState> = _viewState
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = _viewState.value,
        )

    private var searchJob: Job? = null

    init {
        viewModelScope.launch { setEmptySearchQueryState() }

        listenEventsFromParent()
    }

    private fun listenEventsFromParent() {
        viewModelScope.launch {
            parentToChildrenEventReceiver.events.collect { event ->
                when (event) {
                    is ParentToChildrenEvent.SearchEvent.ChangedQuery -> handleChangedSearchQuery(event)

                    ParentToChildrenEvent.SearchEvent.Started -> Unit
                    ParentToChildrenEvent.SearchEvent.Finished -> Unit
                    is ParentToChildrenEvent.BackFromCheckoutToCartClicked -> Unit
                    is ParentToChildrenEvent.ItemClickedInProductSelector -> Unit
                    is ParentToChildrenEvent.OrderSuccessfullyPaid -> Unit
                    is ParentToChildrenEvent.CheckoutClicked -> Unit
                }
            }
        }
    }

    private suspend fun CoroutineScope.handleChangedSearchQuery(
        event: ParentToChildrenEvent.SearchEvent.ChangedQuery
    ) {
        searchJob?.cancel()

        if (event.query.isEmpty()) {
            setEmptySearchQueryState()
        } else {
            searchJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCING_TIME)

                childToParentEventSender.sendToParent(ChildToParentEvent.SearchEvent.Started)

                dataSource.searchProducts(event.query).collect { result ->
                    when (result) {
                        is WooPosSearchProductsMockedDataSource.ProductsResult.Cached -> {
                            if (result.products.isEmpty()) {
                                _viewState.value = WooPosItemsSearchViewState.Empty
                            } else {
                                _viewState.value = result.products.toContentState()
                            }
                        }

                        is WooPosSearchProductsMockedDataSource.ProductsResult.Remote -> {
                            if (result.productsResult.isSuccess) {
                                val products = result.productsResult.getOrThrow()
                                if (products.isEmpty()) {
                                    _viewState.value = WooPosItemsSearchViewState.Empty
                                } else {
                                    _viewState.value = products.toContentState()
                                }
                            } else {
                                _viewState.value = WooPosItemsSearchViewState.Error
                            }
                            childToParentEventSender.sendToParent(ChildToParentEvent.SearchEvent.Finished)
                        }
                    }
                }
            }
        }
    }

    private suspend fun List<Product>.toContentState(
        paginationState: PaginationState = PaginationState.None
    ) = WooPosItemsSearchViewState.Content(
        items = map { product ->
            if (product.productType == ProductType.VARIABLE) {
                WooPosItemSelectionViewState.Product.Variable(
                    id = product.remoteId,
                    name = product.name,
                    price = priceFormat(product.price),
                    imageUrl = product.firstImageUrl,
                    numOfVariations = product.numVariations,
                    variationIds = product.variationIds
                )
            } else {
                WooPosItemSelectionViewState.Product.Simple(
                    id = product.remoteId,
                    name = product.name,
                    price = priceFormat(product.price),
                    imageUrl = product.firstImageUrl,
                )
            }
        },
        paginationState = paginationState,
        reloadingProductsWithPullToRefresh = false
    )

    private suspend fun CoroutineScope.setEmptySearchQueryState() {
        val lastSearchesDeferred = async { emptyStateProvider.getLastSearches() }
        val popularItemsDeferred = async { emptyStateProvider.getPopularItems() }

        _viewState.value = WooPosItemsSearchViewState.EmptySearchQuery(
            popularItems = popularItemsDeferred.await().let { it.take(minOf(MAX_ITEMS_COUNT, it.size)) },
            recentSearches = lastSearchesDeferred.await().let { it.take(minOf(MAX_ITEMS_COUNT, it.size)) },
        )
    }

    private companion object {
        const val MAX_ITEMS_COUNT = 3
        const val SEARCH_DEBOUNCING_TIME = 500L
    }
}
