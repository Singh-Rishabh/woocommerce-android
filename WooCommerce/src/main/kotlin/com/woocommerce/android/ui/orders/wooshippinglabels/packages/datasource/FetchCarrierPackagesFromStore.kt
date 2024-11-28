package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import javax.inject.Inject

class FetchCarrierPackagesFromStore @Inject constructor(
    private val packageRepository: WooShippingLabelPackageRepository
) {
    suspend operator fun invoke(): Map<Carrier, List<CarrierPackageGroup>> {
        return packageRepository.fetchAllStorePackages()
            .takeIf { it.isError.not() }
            ?.model
            ?.filterCarrierData()
            ?: emptyMap()
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
