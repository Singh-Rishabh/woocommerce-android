package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.networking.PackageResponse
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.networking.WooShippingLabelPackageRestClient
import javax.inject.Inject
import javax.inject.Singleton
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult
import org.wordpress.android.fluxc.store.WCShippingLabelStore

@Singleton
class WooShippingLabelPackageRepository @Inject constructor(
    private val selectedSite: SelectedSite,
    private val shippingLabelStore: WCShippingLabelStore,
    private val packageRestClient: WooShippingLabelPackageRestClient
) {
    suspend fun fetchAllStorePackages(
        site: SiteModel = selectedSite.get()
    ): WooResult<PackageResponse> {
        val response = packageRestClient.fetchShippingLabelPackages(site)

        return response.result
            .takeIf { response.isError.not() }
            ?.let { WooResult(it) }
            ?: WooResult(response.error)
    }
}
