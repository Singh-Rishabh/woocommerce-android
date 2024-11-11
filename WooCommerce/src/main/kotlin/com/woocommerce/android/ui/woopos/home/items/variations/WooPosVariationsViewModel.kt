package com.woocommerce.android.ui.woopos.home.items.variations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.ui.products.variations.selector.VariationListHandler
import com.woocommerce.android.ui.woopos.common.data.WooPosGetProductById
import com.woocommerce.android.ui.woopos.home.items.WooPosItem
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsUIEvent
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsViewState
import com.woocommerce.android.ui.woopos.home.items.WooPosVariationsViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WooPosVariationsViewModel @Inject constructor(
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

    private var fetchJob: Job? = null
    private var loadMoreJob: Job? = null

    fun init(productId: Long) {
        fetchJob?.cancel()

        fetchJob = viewModelScope.launch {
            _viewState.value = WooPosVariationsViewState.Loading(withCart = true)
            val product = getProductById(productId)

            variationsDataSource.fetchVariations(productId, forceRefresh = true)

            variationsDataSource.getVariationsFlow(productId).collect { variationList ->
                _viewState.value = WooPosVariationsViewState.Content(
                    items = variationList.map {
                    WooPosItem.Variation(
                        id = it.remoteVariationId,
                        name = it.getName(product),
                        price = it.priceWithCurrency ?: "",
                        imageUrl = it.image?.source
                    )
                },
                    loadingMore = false,
                    reloadingProductsWithPullToRefresh = false,
                )
            }
        }
    }

    fun loadMore(productId: Long) {
        loadMoreJob?.cancel()
        loadMoreJob = viewModelScope.launch {
            val result = variationsDataSource.loadMore(productId)
            if (result.isSuccess) {
                Result.success(Unit)
            } else {
                WooPosVariationsViewState.Error()
            }
        }
    }

    fun onUIEvent(event: WooPosVariationsUIEvents) {
        when (event) {
            is WooPosVariationsUIEvents.EndOfItemsListReached -> {
                onEndOfVariationsListReached(event.productId)
            }
        }
    }

    private fun onEndOfVariationsListReached(productId: Long) {
        loadMore(productId)
    }
}
