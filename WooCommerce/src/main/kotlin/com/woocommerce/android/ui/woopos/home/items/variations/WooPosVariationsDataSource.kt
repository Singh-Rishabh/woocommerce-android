package com.woocommerce.android.ui.woopos.home.items.variations

import com.woocommerce.android.model.ProductVariation
import com.woocommerce.android.ui.products.variations.selector.VariationListHandler
import com.woocommerce.android.util.WooLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WooPosVariationsDataSource @Inject constructor(
    private val handler: VariationListHandler
) {
    private val variationCache = VariationsLRUCache<Long, List<ProductVariation>>(maxSize = 50)
    private val cacheMutex = kotlinx.coroutines.sync.Mutex()

    private suspend fun getCachedVariations(productId: Long): List<ProductVariation> {
        return cacheMutex.withLock { variationCache.get(productId) ?: emptyList() }
    }

    private suspend fun updateCache(productId: Long, variations: List<ProductVariation>) {
        cacheMutex.withLock {
            variationCache.put(productId, variations)
        }
    }

    fun canLoadMore(): Boolean {
        return handler.canLoadMore()
    }

    fun fetchFirstPage(
        productId: Long,
        forceRefresh: Boolean = true
    ): Flow<FetchResult<List<ProductVariation>>> = flow {
        if (forceRefresh) {
            updateCache(productId, emptyList())
        }

        val cachedVariations = getCachedVariations(productId)
        if (cachedVariations.isNotEmpty()) {
            emit(FetchResult.Cached(cachedVariations))
        }

        val result = handler.fetchVariations(productId, forceRefresh = true)
        if (result.isSuccess) {
            val remoteVariations = handler.getVariationsFlow(productId).firstOrNull() ?: emptyList()
            updateCache(productId, remoteVariations)
            emit(FetchResult.Remote(Result.success(remoteVariations)))
        } else {
            emit(
                FetchResult.Remote(
                    Result.failure(
                        result.exceptionOrNull() ?: Exception("Unknown error while fetching variations")
                    )
                )
            )
        }
    }.flowOn(Dispatchers.IO)

    suspend fun loadMore(productId: Long): Result<List<ProductVariation>> = withContext(Dispatchers.IO) {
        val result = handler.loadMore(productId)
        if (result.isSuccess) {
            val fetchedVariations = handler.getVariationsFlow(productId).first()
            Result.success(fetchedVariations)
        } else {
            result.logFailure()
            Result.failure(
                result.exceptionOrNull() ?: Exception("Unknown error while loading more variations")
            )
        }
    }

    fun resetLoadMoreState() {
        handler.resetLoadMoreState()
    }
}

private fun Result<Unit>.logFailure() {
    val error = exceptionOrNull()
    val errorMessage = error?.message ?: "Unknown error"
    WooLog.e(WooLog.T.POS, "Loading variations failed - $errorMessage", error)
}

sealed class FetchResult<out T> {
    data class Cached<out T>(val data: T) : FetchResult<T>()
    data class Remote<out T>(val result: Result<T>) : FetchResult<T>()
}
