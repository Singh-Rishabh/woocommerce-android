package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.networking

import javax.inject.Inject
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooNetwork
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooPayload
import org.wordpress.android.fluxc.utils.toWooPayload

class WooShippingLabelPackageRestClient @Inject constructor(
    private val wooNetwork: WooNetwork
) {
    suspend fun fetchShippingLabelPackages(
        site: SiteModel
    ) : WooPayload<PackageResponse> {
        val url = "/wcshipping/v1/packages"

        return wooNetwork.executeGetGsonRequest(
            site = site,
            path = url,
            clazz = PackageResponse::class.java,
        ).toWooPayload()
    }
}
