package com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking

class CarrierPredefinedPackagesDTO {
    val usps: USPSPackageDTO? = null
    val dhlExpress: DHLPackageDTO? = null
}

class USPSPackageDTO {
    val flatBoxes: CarrierPackageGroupDTO? = null
    val boxes: CarrierPackageGroupDTO? = null
    val expressBoxes: CarrierPackageGroupDTO? = null
    val envelopes: CarrierPackageGroupDTO? = null
    val expressEnvelopes: CarrierPackageGroupDTO? = null
}

class DHLPackageDTO {
    val domesticAndInternationalPackages: CarrierPackageGroupDTO? = null
}

class CarrierPackageGroupDTO {
    val title: String? = null
    val definitions: List<PredefinedPackageDTO>? = null
}

class PredefinedPackageDTO {
    val innerDimensions: String? = null
    val outerDimensions: String? = null
    val boxWeight: Double? = null
    val isFlatRate: Boolean? = null
    val id: String? = null
    val name: String? = null
    val dimensions: String? = null
    val maxWeight: Double? = null
    val isLetter: Boolean? = null
    val groupId: String? = null
    val canShipInternational: Boolean? = null
}
