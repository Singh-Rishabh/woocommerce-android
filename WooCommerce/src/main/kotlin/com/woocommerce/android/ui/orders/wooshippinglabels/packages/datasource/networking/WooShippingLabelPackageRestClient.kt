package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.networking

import javax.inject.Inject
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooNetwork

class WooShippingLabelPackageRestClient @Inject constructor(
    private val wooNetwork: WooNetwork
) {
    private val url = "/wcshipping/v1/packages"

    fun fetchShippingLabelPackages(): StorePackagesDTO? {
        return null
    }

    class StorePackagesDTO {
        val storeOptions: PackageStoreOptionsDTO? = null
    }

    class PackageStoreOptionsDTO {
        val currencySymbol: String? = null
        val dimensionUnit: String? = null
        val weightUnit: String? = null
        val originCountry: String? = null
    }
}
