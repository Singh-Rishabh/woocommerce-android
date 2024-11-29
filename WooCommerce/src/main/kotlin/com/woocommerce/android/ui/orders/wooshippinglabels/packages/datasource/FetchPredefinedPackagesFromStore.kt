package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageSelection
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.SavedPackageSelection
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.StorePredefinedPackages
import javax.inject.Inject

class FetchPredefinedPackagesFromStore @Inject constructor(
    private val selectedSite: SelectedSite,
    private val packageRepository: WooShippingLabelPackageRepository
) {
    suspend operator fun invoke(): StorePredefinedPackages? {
        val storePackages = selectedSite.getOrNull()
            ?.let { packageRepository.fetchAllStorePackages(it) }
            ?.takeIf { it.isError.not() }
            ?.model
            ?: return null

        return StorePredefinedPackages(
            savedPackageSelection = storePackages
                .filterSavedData()
                .let { SavedPackageSelection(it) },
            carrierPackageSelection = storePackages
                .filterCarrierData()
                .let { CarrierPackageSelection(it) }
        )
    }

    private fun StorePackagesDAO.filterSavedData() =
        savedPackages.map { packageDAO ->
            PackageData(
                name = packageDAO.name,
                description = "",
                dimensions = packageDAO.dimensions,
                isSelected = false,
                isLetter = packageDAO.isLetter
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
                        name = packageItem.name,
                        description = "",
                        dimensions = packageItem.dimensions,
                        isSelected = false,
                        isLetter = packageItem.isLetter
                    )
                }
            )
        }
    } ?: emptyList()
}
