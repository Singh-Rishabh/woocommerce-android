package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

data class StorePackagesDAO(
    val savedPackages: List<PackageDAO>,
    val carrierPackages: Map<CarrierType, CarrierDAO>
)

data class PackageDAO(
    val id: String,
    val name: String,
    val dimensions: String,
    val isLetter: Boolean
)

data class CarrierDAO(
    val packageGroup: List<CarrierPackageGroupDAO>
)

data class CarrierPackageGroupDAO(
    val description: String,
    val packages: List<PackageDAO>
)

enum class CarrierType {
    USPS,
    DHL
}
