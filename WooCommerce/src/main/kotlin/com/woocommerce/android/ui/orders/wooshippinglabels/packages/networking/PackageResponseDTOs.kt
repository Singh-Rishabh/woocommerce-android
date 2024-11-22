package com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.WooShippingLabelPackageRestClient.CarrierPredefinedPackagesDTO

class PackageResponse {
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
    val predefined: CarrierPredefinedPackagesDTO? = null
}
