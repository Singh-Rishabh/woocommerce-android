package com.cataloghub.android.ui.woopos.common.data

import com.cataloghub.android.model.Product
import com.cataloghub.android.model.toAppModel
import com.cataloghub.android.tools.SelectedSite
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WCProductStore
import javax.inject.Inject

class WooPosGetProductById @Inject constructor(
    private val store: WCProductStore,
    private val site: SelectedSite,
) {
    suspend operator fun invoke(productId: Long): Product? = withContext(IO) {
        store.getProductByRemoteId(site.getOrNull()!!, productId)?.toAppModel()
    }
}
