package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.R
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import javax.inject.Inject

class FetchCarrierPackagesFromStore @Inject constructor() {
    operator fun invoke() : Map<Carrier, List<CarrierPackageGroup>> {
        // This is a mocked response.
        // When fully implemented, this will be sorted from the Shipping plugin API.
        return mapOf(
            Carrier(
                id = "dhl",
                name = "DHL Express",
                logoRes = R.drawable.dhl_logo
            ) to listOf(
                CarrierPackageGroup(
                    groupName = "Group 1",
                    packages = listOf(
                        PackageData(
                            type = PackageType.BOX,
                            name = "Package 1 - Carrier 1",
                            description = "Description 1",
                            length = "10",
                            width = "10",
                            height = "10",
                            isSelected = false
                        ),
                        PackageData(
                            type = PackageType.BOX,
                            name = "Package 2 - Carrier 1",
                            description = "Description 2",
                            length = "20",
                            width = "20",
                            height = "20",
                            isSelected = false
                        )
                    )
                ),
                CarrierPackageGroup(
                    groupName = "Group 2",
                    packages = listOf(
                        PackageData(
                            type = PackageType.BOX,
                            name = "Package 3 - Carrier 1",
                            description = "Description 3",
                            length = "30",
                            width = "30",
                            height = "30",
                            isSelected = false
                        ),
                        PackageData(
                            type = PackageType.BOX,
                            name = "Package 4 - Carrier 1",
                            description = "Description 4",
                            length = "40",
                            width = "40",
                            height = "40",
                            isSelected = false
                        )
                    )
                )
            ),
            Carrier(
                id = "usps",
                name = "USPS",
                logoRes = R.drawable.usps_logo
            ) to listOf(
                CarrierPackageGroup(
                    groupName = "Group 2",
                    packages = listOf(
                        PackageData(
                            type = PackageType.BOX,
                            name = "Package 1 - Carrier 2",
                            description = "Description 1",
                            length = "10",
                            width = "10",
                            height = "10",
                            isSelected = false
                        ),
                        PackageData(
                            type = PackageType.BOX,
                            name = "Package 2 Carrier - 2",
                            description = "Description 2",
                            length = "20",
                            width = "20",
                            height = "20",
                            isSelected = false
                        )
                    )
                )
            )
        )
    }
}
