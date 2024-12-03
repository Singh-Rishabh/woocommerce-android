package com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking

class SavedPackageInfoDTO {
    val custom: List<CustomPackageDTO>? = null
    val predefined: List<PredefinedPackageDTO>? = null
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
