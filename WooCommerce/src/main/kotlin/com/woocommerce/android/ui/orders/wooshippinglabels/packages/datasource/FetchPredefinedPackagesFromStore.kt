package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PredefinedPackagesState
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import javax.inject.Inject

class FetchPredefinedPackagesFromStore @Inject constructor(
    private val selectedSite: SelectedSite,
    private val packageRepository: WooShippingLabelPackageRepository
) {
    suspend operator fun invoke(): PredefinedPackagesState {
        val storePackages = selectedSite.getOrNull()
            ?.let { packageRepository.fetchAllStorePackages(it) }
            ?.takeIf { it.isError.not() }
            ?.model
            ?: return PredefinedPackagesState.Error

        return PredefinedPackagesState.Data(
            savedPackages = storePackages.filterSavedData(),
            carrierPackages = storePackages.filterCarrierData()
        )
    }

    private fun StorePackagesDAO.filterSavedData() =
        savedPackages.map { packageDAO ->
            PackageData(
                name = packageDAO.name,
                dimensions = packageDAO.dimensions,
                weight = packageDAO.weight,
                isSelected = false,
                isLetter = packageDAO.isLetter,
                dimensionUnit = packageDAO.dimensionUnit,
                weightUnit = packageDAO.weightUnit,
                groupName = packageDAO.groupName
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
                        dimensions = packageItem.dimensions,
                        weight = packageItem.weight,
                        isSelected = false,
                        isLetter = packageItem.isLetter,
                        dimensionUnit = packageItem.dimensionUnit,
                        weightUnit = packageItem.weightUnit,
                        groupName = packageItem.groupName
                    )
                }
            )
        }
    } ?: emptyList()
}
