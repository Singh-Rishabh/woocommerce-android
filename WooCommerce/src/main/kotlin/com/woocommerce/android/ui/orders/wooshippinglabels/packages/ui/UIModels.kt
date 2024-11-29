package com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui

import android.os.Parcelable
import com.woocommerce.android.R
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import kotlinx.parcelize.Parcelize

@Parcelize
data class PackageData(
    val name: String,
    val dimensions: String,
    val isSelected: Boolean,
    val isLetter: Boolean
) : Parcelable {
    val descriptionResId: Int
        get() = when (isLetter) {
            true -> R.string.woo_shipping_labels_package_creation_envelope_type
            false -> R.string.woo_shipping_labels_package_creation_box_type
        }
}

@Parcelize
data class PredefinedPackage(
    val boxWeight: Double,
    val isFlatRate: Boolean,
    val id: String,
    val name: String,
    val dimensions: String,
    val maxWeight: Double,
    val isLetter: Boolean,
    val groupId: String,
    val canShipInternational: Boolean
) : Parcelable

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

    fun toPackageData(dimensionUnit: String = "cm") = PackageData(
        name = "",
        dimensions = "$length x $width x $height $dimensionUnit",
        isSelected = true,
        isLetter = type == PackageType.ENVELOPE
    )

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

@Parcelize
data class CarrierPackageGroup(
    val groupName: String,
    val packages: List<PackageData>
) : Parcelable

@Parcelize
sealed class Carrier(
    val id: String,
    val name: String,
    val logoRes: Int? = null,
) : Parcelable {
    data object USPS : Carrier(
        id = "usps",
        name = "USPS",
        logoRes = R.drawable.usps_logo
    )

    data object DHL : Carrier(
        id = "dhl",
        name = "DHL",
        logoRes = R.drawable.dhl_logo
    )
}

@Parcelize
data class StorePredefinedPackages(
    val carrierPackageSelection: CarrierPackageSelection,
    val savedPackageSelection: SavedPackageSelection
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
