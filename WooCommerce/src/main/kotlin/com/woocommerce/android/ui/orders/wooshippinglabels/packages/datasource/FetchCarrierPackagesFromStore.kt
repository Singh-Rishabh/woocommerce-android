package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.R
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import javax.inject.Inject

class FetchCarrierPackagesFromStore @Inject constructor(
    private val packageRepository: WooShippingLabelPackageRepository
) {
    @Suppress("LongMethod")
    suspend operator fun invoke(): Map<Carrier, List<CarrierPackageGroup>> {
        return packageRepository.fetchAllStorePackages()
            .takeIf { it.isError.not() }
            ?.model
            ?.let { response ->
                val uspsPackages = response.carrierPackages
                    .parseCarrierData(CarrierType.USPS)
                    .let {
                        Pair(
                            Carrier(
                                id = "usps",
                                name = "USPS",
                                logoRes = R.drawable.usps_logo
                            ), it
                        )
                    }

                val dhlPackages = response.carrierPackages
                    .parseCarrierData(CarrierType.DHL)
                    .let {
                        Pair(
                            Carrier(
                                id = "dhl",
                                name = "DHL Express",
                                logoRes = R.drawable.dhl_logo
                            ), it
                        )
                }

                mapOf(uspsPackages, dhlPackages)
            } ?: emptyMap()
    }

    private fun Map<CarrierType, CarrierDAO>.parseCarrierData(
        carrierType: CarrierType
    ) : List<CarrierPackageGroup> {
        return this[carrierType]?.let {
            return it.packageGroup.map { group ->
                CarrierPackageGroup(
                    groupName = group.description,
                    packages = group.packages.map { packageItem ->
                        PackageData(
                            type = PackageType.BOX,
                            name = packageItem.name,
                            description = "",
                            length = "",
                            width = "",
                            height = "",
                            isSelected = false
                        )
                    }
                )
            }
        } ?: emptyList()
    }
}
