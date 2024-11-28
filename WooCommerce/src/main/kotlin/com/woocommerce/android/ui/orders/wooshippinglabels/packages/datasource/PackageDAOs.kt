package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

data class StorePackagesDAO(
    val savedPackages: List<PackageDAO>,
    val carrierPackages: List<CarrierDAO>
)

data class PackageDAO(
    val id: String,
    val name: String,
    val dimensions: String
)

data class CarrierDAO(
    val type: CarrierType,
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
