package com.cataloghub.android.ui.products.images

import com.cataloghub.android.model.Product
import com.cataloghub.android.model.toAppModel
import com.cataloghub.android.tools.SelectedSite
import org.wordpress.android.fluxc.store.WCProductStore
import javax.inject.Inject

class ProductImagesRepository @Inject constructor(
    private val productStore: WCProductStore,
    private val selectedSite: SelectedSite
) {
    fun getProduct(remoteProductId: Long): Product? =
        productStore.getProductByRemoteId(selectedSite.get(), remoteProductId)?.toAppModel()
}
