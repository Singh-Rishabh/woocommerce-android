package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import android.os.Parcelable
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import kotlinx.parcelize.Parcelize

@Parcelize
data class PackageData(
    val type: PackageType,
    val name: String,
    val description: String,
    val length: String,
    val width: String,
    val height: String,
    val isSelected: Boolean,
    val dimensionUnit: String = "cm"
) : Parcelable {
    val dimensionsForDisplay: String
        get() = "$length x $width x $height $dimensionUnit"
}

@Parcelize
data class CarrierPackageGroup(
    val groupName: String,
    val packages: List<PackageData>
) : Parcelable

@Parcelize
data class Carrier(
    val id: String,
    val name: String,
    val logoRes: Int? = null,
) : Parcelable

@Parcelize
data class CarrierPackageSelection(
    val carrierPackages: Map<Carrier, List<CarrierPackageGroup>>
) : Parcelable {
    val hasSelection: Boolean
        get() = carrierPackages.values.flatten().find { group ->
            group.packages.find { it.isSelected } != null
        } != null
}

@Parcelize
data class SavedPackageSelection(
    val packages: List<PackageData>
) : Parcelable {
    val hasSelection: Boolean
        get() = packages.find { it.isSelected } != null
}

@Parcelize
data class CustomPackageCreationData(
    val type: PackageType,
    val length: String,
    val width: String,
    val height: String,
    val saveAsTemplate: Boolean
) : Parcelable {
    val isValid: Boolean
        get() = height.isNotEmpty() && length.isNotEmpty() && width.isNotEmpty()

    companion object {
        val EMPTY = CustomPackageCreationData(
            type = PackageType.BOX,
            length = "",
            width = "",
            height = "",
            saveAsTemplate = false
        )
    }
}
