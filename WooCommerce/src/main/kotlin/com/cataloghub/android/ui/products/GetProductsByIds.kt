package com.cataloghub.android.ui.products

import com.cataloghub.android.model.Product
import com.cataloghub.android.model.toAppModel
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.util.CoroutineDispatchers
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WCProductStore
import javax.inject.Inject

class GetProductsByIds @Inject constructor(
    private val selectedSite: SelectedSite,
    private val productStore: WCProductStore,
    private val dispatchers: CoroutineDispatchers
) {
    suspend operator fun invoke(productRemoteIds: List<Long>): List<Product> {
        return withContext(dispatchers.io) {
            val siteModel = selectedSite.get()
            productStore.fetchProductListSynced(siteModel, productRemoteIds)
                ?.map { it.toAppModel() }
                ?: emptyList()
        }
    }
}
