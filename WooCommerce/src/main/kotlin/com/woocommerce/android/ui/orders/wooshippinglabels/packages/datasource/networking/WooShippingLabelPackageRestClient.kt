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
    ) : WooPayload<StorePackagesDTO> {
        val url = "/wcshipping/v1/packages"

        return wooNetwork.executeGetGsonRequest(
            site = site,
            path = url,
            clazz = StorePackagesDTO::class.java,
        ).toWooPayload()
    }

    class StorePackagesDTO {
        val storeOptions: PackageStoreOptionsDTO? = null
        val packages: PackagesInfoDTO? = null
    }

    class PackageStoreOptionsDTO {
        val currencySymbol: String? = null
        val dimensionUnit: String? = null
        val weightUnit: String? = null
        val originCountry: String? = null
    }

    class PackagesInfoDTO {
        val saved: SavedPackageInfoDTO? = null
        val predefined: String? = null
    }

    class SavedPackageInfoDTO {
        val custom: List<CustomPackageDTO>? = null
    }

    class CustomPackageDTO {
        val id: String? = null
        val name: String? = null
        val dimensions: String? = null
        val length: Double? = null
        val width: Double? = null
        val height: Double? = null
        val boxWeight: Double? = null
        val isLetter: Boolean? = null
        val isUserDefined: Boolean? = null
        val type: String? = null
    }
}
