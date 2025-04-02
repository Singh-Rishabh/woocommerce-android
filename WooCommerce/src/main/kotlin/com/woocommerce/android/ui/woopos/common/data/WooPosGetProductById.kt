package com.woocommerce.android.ui.woopos.common.data

import com.woocommerce.android.model.Product
import com.woocommerce.android.model.toAppModel
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.woopos.featureflags.WooPosIsProductsSearchEnabled
import com.woocommerce.android.ui.woopos.home.items.search.WooPosSearchProductsDataSource
import com.woocommerce.android.util.WooLog
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WCProductStore
import org.wordpress.android.fluxc.store.WCProductStore.FetchSingleProductPayload
import javax.inject.Inject

class WooPosGetProductById @Inject constructor(
    private val store: WCProductStore,
    private val site: SelectedSite,
    private val isWooPosSearchEnabled: WooPosIsProductsSearchEnabled,
    private val searchProductsDataSource: WooPosSearchProductsDataSource,
) {
    @Suppress("ForbiddenComment")
    suspend operator fun invoke(productId: Long): Product? = withContext(IO) {
        var result = store.getProductByRemoteId(site.get(), productId)?.toAppModel()
        // TODO: the call to local DB should be replaced by querying POS-specific Product table, once it's implemented.

        if (result == null && isWooPosSearchEnabled()) {
            result = searchProductsDataSource.getProductById(productId)
        }

        if (result == null) {
            val fetchResult = store.fetchSingleProduct(FetchSingleProductPayload(site.get(), productId))
            if (!fetchResult.isError) {
                result = store.getProductByRemoteId(site.get(), productId)?.toAppModel()
            } else {
                WooLog.w(WooLog.T.POS, "Error fetching product $productId: ${fetchResult.error.message}")
            }
        }
        result
    }
}
