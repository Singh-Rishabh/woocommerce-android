package com.woocommerce.android.ui.woopos.home.items.variations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.ui.woopos.common.data.WooPosGetProductById
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.items.PaginationState
import com.woocommerce.android.ui.woopos.home.items.WooPosItem
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsViewModel
import com.woocommerce.android.ui.woopos.home.items.WooPosVariationsViewState
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooPosVariationsViewModel @Inject constructor(
    private val fromChildToParentEventSender: WooPosChildrenToParentEventSender,
    private val getProductById: WooPosGetProductById,
    private val variationsDataSource: WooPosVariationsDataSource,
    private val priceFormat: WooPosFormatPrice,
) : ViewModel() {

    private val _viewState =
        MutableStateFlow<WooPosVariationsViewState>(WooPosVariationsViewState.Loading(withCart = true))
    val viewState: StateFlow<WooPosVariationsViewState> = _viewState
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = _viewState.value,
        )

    private var flowJob: Job? = null
    private var fetchJob: Job? = null
    private var loadMoreJob: Job? = null

    fun init(productId: Long, withPullToRefresh: Boolean = false, withCart: Boolean = true) {
        resetState()
        startCollectingVariationsFlow(productId)
        fetchVariations(productId = productId, withPullToRefresh = withPullToRefresh, withCart = withCart)
    }

    private fun resetState() {
        flowJob?.cancel()
        variationsDataSource.resetLoadMoreState()
        _viewState.value = WooPosVariationsViewState.Loading(withCart = true)
    }

    private fun startCollectingVariationsFlow(productId: Long) {
        flowJob?.cancel()
        flowJob = viewModelScope.launch {
            variationsDataSource.getVariationsFlow(productId)
                .map { variationList ->
                    if (variationList.isEmpty() && fetchJob?.isActive == true) {
                        WooPosVariationsViewState.Loading(withCart = true)
                    } else if (variationList.isEmpty()) {
                        WooPosVariationsViewState.Error()
                    } else {
                        WooPosVariationsViewState.Content(
                            items = variationList.filter { it.price != null }.map {
                                WooPosItem.Variation(
                                    id = it.remoteVariationId,
                                    name = it.getName(getProductById(productId)),
                                    productId = it.remoteProductId,
                                    price = priceFormat(it.price),
                                    imageUrl = it.image?.source
                                )
                            }
                        )
                    }
                }
                .collect { state ->
                    _viewState.value = state
                }
        }
    }

    private fun fetchVariations(productId: Long, withPullToRefresh: Boolean, withCart: Boolean) {
        _viewState.value = if (withPullToRefresh) {
            buildProductsReloadingState()
        } else {
            WooPosVariationsViewState.Loading(withCart = withCart)
        }
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            variationsDataSource.fetchVariations(productId, forceRefresh = true)
        }
    }

    private fun buildProductsReloadingState() =
        when (val state = viewState.value) {
            is WooPosVariationsViewState.Content -> state.copy(reloadingProductsWithPullToRefresh = true)
            is WooPosVariationsViewState.Loading -> state.copy(reloadingProductsWithPullToRefresh = true)
            is WooPosVariationsViewState.Error -> state.copy(reloadingProductsWithPullToRefresh = true)
            is WooPosVariationsViewState.Empty -> state.copy(reloadingProductsWithPullToRefresh = true)
        }

    private fun loadMore(productId: Long) {
        val currentState = _viewState.value
        if (currentState !is WooPosVariationsViewState.Content) {
            return
        }

        if (!variationsDataSource.canLoadMore()) {
            return
        }

        _viewState.value = currentState.copy(paginationState = PaginationState.Loading)

        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            val result = variationsDataSource.loadMore(productId)
            _viewState.update { current ->
                if (current is WooPosVariationsViewState.Content) {
                    current.copy(paginationState = determinePaginationState(result))
                } else {
                    current
                }
            }
        }
    }

    private fun determinePaginationState(result: Result<*>): PaginationState {
        return if (result.isSuccess) PaginationState.None else PaginationState.Error
    }

    fun onUIEvent(event: WooPosVariationsUIEvents) {
        when (event) {
            is WooPosVariationsUIEvents.EndOfItemsListReached -> {
                onEndOfVariationsListReached(event.productId)
            }

            is WooPosVariationsUIEvents.PullToRefreshTriggered -> {
                fetchVariations(event.productId, withPullToRefresh = true, withCart = false)
            }

            is WooPosVariationsUIEvents.VariationsLoadingErrorRetryButtonClicked -> {
                init(event.productId, withPullToRefresh = false, withCart = false)
            }

            is WooPosVariationsUIEvents.OnItemClicked -> {
                onVariationClicked(event.productId, event.variationId)
            }
        }
    }

    private fun onVariationClicked(productId: Long, variationId: Long) {
        sendEventToParent(
            ChildToParentEvent.ItemClickedInProductSelector(
                WooPosItemsViewModel.ItemClickedData.Variation(productId, variationId)
            )
        )
    }

    private fun sendEventToParent(event: ChildToParentEvent) {
        viewModelScope.launch { fromChildToParentEventSender.sendToParent(event) }
    }

    private fun onEndOfVariationsListReached(productId: Long) {
        loadMore(productId)
    }
}
