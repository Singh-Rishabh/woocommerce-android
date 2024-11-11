package com.woocommerce.android.ui.woopos.home.items.variations

import com.woocommerce.android.model.ProductVariation
import com.woocommerce.android.ui.products.variations.selector.VariationListHandler
import com.woocommerce.android.util.WooLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WooPosVariationsDataSource @Inject constructor(
    private val handler: VariationListHandler
) {
    fun getVariationsFlow(productId: Long): Flow<List<ProductVariation>> {
        return handler.getVariationsFlow(productId)
    }

    suspend fun fetchVariations(productId: Long, forceRefresh: Boolean = true) {
        val result = handler.fetchVariations(productId, forceRefresh = forceRefresh)
        if (result.isSuccess) {
            Result.success(Unit)
        } else {
            result.logFailure()
            Result.failure(
                result.exceptionOrNull() ?: Exception("Unknown error while loading more variations")
            )
        }
    }

    suspend fun loadMore(productId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        val result = handler.loadMore(productId)
        if (result.isSuccess) {
            Result.success(Unit)
        } else {
            result.logFailure()
            Result.failure(
                result.exceptionOrNull() ?: Exception("Unknown error while loading more variations")
            )
        }
    }
}

private fun Result<Unit>.logFailure() {
    val error = exceptionOrNull()
    val errorMessage = error?.message ?: "Unknown error"
    WooLog.e(WooLog.T.POS, "Loading variations failed - $errorMessage", error)
}
