package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageSelection
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.SavedPackageSelection
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.StorePredefinedPackages
import javax.inject.Inject

class FetchPredefinedPackagesFromStore @Inject constructor(
    private val packageRepository: WooShippingLabelPackageRepository
) {
    suspend operator fun invoke(): StorePredefinedPackages {
        val storePackages = packageRepository.fetchAllStorePackages()

        val carrierPackages = storePackages
            .takeIf { it.isError.not() }
            ?.model
            ?.filterCarrierData()
            ?: emptyMap()

        val savedPackages = storePackages
            .takeIf { it.isError.not() }
            ?.model
            ?.savedPackages
            ?.map { packageDAO ->
                PackageData(
                    type = PackageType.BOX,
                    name = packageDAO.name,
                    description = "",
                    length = "",
                    width = "",
                    height = "",
                    isSelected = false
                )
            } ?: emptyList()

        return StorePredefinedPackages(
            carrierPackageSelection = CarrierPackageSelection(carrierPackages),
            savedPackageSelection = SavedPackageSelection(savedPackages)
        )
    }

    private fun StorePackagesDAO.filterCarrierData() = mapOf(
        carrierPackages
            .parseCarrierData(CarrierType.USPS)
            .let { Carrier.USPS to it },

        carrierPackages
            .parseCarrierData(CarrierType.DHL)
            .let { Carrier.DHL to it }
    )

    private fun Map<CarrierType, CarrierDAO>.parseCarrierData(
        carrierType: CarrierType
    ) = get(carrierType)?.let {
        it.packageGroup.map { group ->
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
