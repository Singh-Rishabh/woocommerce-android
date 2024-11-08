package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import javax.inject.Inject

class FetchSavedPackagesFromStore @Inject constructor() {
    operator fun invoke(): List<PackageData> {
        // This is a mocked response.
        // When fully implemented, this will be sorted from the Shipping plugin API.
        return listOf(
            PackageData(
                type = PackageType.ENVELOPE,
                name = "Small Flat Rate Box",
                description = "USPS Priority Mail Flat Rate Boxes",
                length = "10",
                width = "10",
                height = "10",
                isSelected = true
            ),
            PackageData(
                type = PackageType.BOX,
                name = "Small Flat Rate Box",
                description = "Custom package",
                length = "20",
                width = "20",
                height = "20",
                isSelected = false
            ),
            PackageData(
                type = PackageType.BOX,
                name = "Small Flat Rate Box",
                description = "DHL Express",
                length = "30",
                width = "30",
                height = "30",
                isSelected = false
            )
        )
    }
}
