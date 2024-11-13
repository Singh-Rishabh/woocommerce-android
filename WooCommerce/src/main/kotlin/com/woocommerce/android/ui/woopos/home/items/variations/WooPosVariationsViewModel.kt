package com.woocommerce.android.ui.woopos.home.items.variations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.ui.woopos.common.data.WooPosGetProductById
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.items.WooPosItem
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsViewModel
import com.woocommerce.android.ui.woopos.home.items.WooPosVariationsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooPosVariationsViewModel @Inject constructor(
    private val fromChildToParentEventSender: WooPosChildrenToParentEventSender,
    private val getProductById: WooPosGetProductById,
    private val variationsDataSource: WooPosVariationsDataSource,
) : ViewModel() {

    private val _viewState =
        MutableStateFlow<WooPosVariationsViewState>(WooPosVariationsViewState.Loading(withCart = true))
    val viewState: StateFlow<WooPosVariationsViewState> = _viewState
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = _viewState.value,
        )

    private val _events: MutableSharedFlow<WooPosVariationEvents> = MutableSharedFlow(
        extraBufferCapacity = 1
    )
    val events = _events.asSharedFlow()

    private var fetchJob: Job? = null
    private var loadMoreJob: Job? = null

    fun init(productId: Long) {
        fetchVariations(productId = productId, withPullToRefresh = false, withCart = true)
    }

    private fun fetchVariations(productId: Long, withPullToRefresh: Boolean, withCart: Boolean) {
        _viewState.value = if (withPullToRefresh) {
            buildProductsReloadingState()
        } else {
            WooPosVariationsViewState.Loading(withCart = withCart)
        }
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            val product = getProductById(productId)

            val result = variationsDataSource.fetchVariations(productId, forceRefresh = true)
            if (result.isSuccess) {
                variationsDataSource.getVariationsFlow(productId).collect { variationList ->
                    _viewState.value = WooPosVariationsViewState.Content(
                        items = variationList.map {
                            WooPosItem.Variation(
                                id = it.remoteVariationId,
                                name = it.getName(product),
                                productId = it.remoteProductId,
                                price = it.priceWithCurrency ?: "",
                                imageUrl = it.image?.source
                            )
                        },
                        loadingMore = false,
                        reloadingProductsWithPullToRefresh = false,
                    )
                }
            } else {
                _viewState.value = WooPosVariationsViewState.Error()
            }
        }
    }

    private fun buildProductsReloadingState() =
        when (val state = viewState.value) {
            is WooPosVariationsViewState.Content -> state.copy(reloadingProductsWithPullToRefresh = true)
            is WooPosVariationsViewState.Loading -> state.copy(reloadingProductsWithPullToRefresh = true)
            is WooPosVariationsViewState.Error -> state.copy(reloadingProductsWithPullToRefresh = true)
            is WooPosVariationsViewState.Empty -> state.copy(reloadingProductsWithPullToRefresh = true)
        }

    fun loadMore(productId: Long) {
        val currentState = _viewState.value
        if (currentState !is WooPosVariationsViewState.Content) {
            return
        }
        if (!variationsDataSource.canLoadMore()) {
            return
        }
        _viewState.value = currentState.copy(loadingMore = true)
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            val result = variationsDataSource.loadMore(productId)
            if (result.isSuccess) {
                Result.success(Unit)
                if (!variationsDataSource.canLoadMore()) {
                    _viewState.value = currentState.copy(loadingMore = false)
                }
            } else {
                _events.tryEmit(WooPosVariationEvents.PaginationError)
                _viewState.value = currentState.copy(loadingMore = false)
            }
        }
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
                fetchVariations(event.productId, withPullToRefresh = false, withCart = false)
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

    sealed class WooPosVariationEvents {
        data object PaginationError : WooPosVariationEvents()
    }
}
