package com.cataloghub.android.ui.products

import com.cataloghub.android.model.ProductCategory
import com.cataloghub.android.model.toProductCategory
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.util.CoroutineDispatchers
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WCProductStore
import javax.inject.Inject

class GetCategoriesByIds @Inject constructor(
    private val selectedSite: SelectedSite,
    private val productStore: WCProductStore,
    private val dispatchers: CoroutineDispatchers
) {
    suspend operator fun invoke(productRemoteIds: List<Long>): List<ProductCategory> {
        return withContext(dispatchers.io) {
            val siteModel = selectedSite.get()
            productStore.fetchProductCategoryListSynced(siteModel, productRemoteIds)
                ?.map { it.toProductCategory() }
                ?: emptyList()
        }
    }
}
