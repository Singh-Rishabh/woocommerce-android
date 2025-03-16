package com.cataloghub.android.ui.products

import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.util.CoroutineDispatchers
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WCProductStore
import javax.inject.Inject

class GetBundledProductsCount @Inject constructor(
    private val selectedSite: SelectedSite,
    private val productStore: WCProductStore,
    private val dispatchers: CoroutineDispatchers
) {
    suspend operator fun invoke(productId: Long): Int {
        val siteModel = selectedSite.get()
        return withContext(dispatchers.io) {
            productStore.getBundledProductsCount(siteModel, productId)
        }
    }
}
