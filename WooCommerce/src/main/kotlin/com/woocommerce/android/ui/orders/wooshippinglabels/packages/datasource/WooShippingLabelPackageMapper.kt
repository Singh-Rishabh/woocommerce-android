package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.CarrierGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.Package
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.StorePackages
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.CarrierPackageGroupDTO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.CarrierPredefinedPackagesDTO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.PackageResponse
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.PredefinedPackageDTO
import javax.inject.Inject

class WooShippingLabelPackageMapper @Inject constructor() {
    operator fun invoke(response: PackageResponse): StorePackages {
        val savedPackagesResponse = response.packages?.saved?.predefined ?: emptyList()

        return StorePackages(
            savedPackages = mapSavedPackages(savedPackagesResponse),
            carrierPackages = mapCarrierPackages(response.packages?.predefined)
        )
    }

    private fun mapSavedPackages(savedResponse: List<PredefinedPackageDTO>): List<Package> {
        return savedResponse.map {
            Package(
                id = it.id ?: "",
                name = it.name ?: "",
                dimensions = it.dimensions ?: ""
            )
        }
    }

    private fun mapCarrierPackages(carrierPackagesResponse: CarrierPredefinedPackagesDTO?): List<CarrierGroup> {
        return mutableListOf<CarrierGroup>().apply {
            carrierPackagesResponse?.usps?.let { usps ->
                usps.flatBoxes?.toCarrierGroup()?.let { add(it) }
                usps.boxes?.toCarrierGroup()?.let { add(it) }
                usps.expressBoxes?.toCarrierGroup()?.let { add(it) }
                usps.envelopes?.toCarrierGroup()?.let { add(it) }
            }
            carrierPackagesResponse?.dhlExpress?.let { dhl ->
                dhl.domesticAndInternationalPackages?.toCarrierGroup()?.let { add(it) }
            }
        }
    }

    private fun CarrierPackageGroupDTO.toCarrierGroup() = CarrierGroup(
        description = title.orEmpty(),
        packages = definitions?.map {
            Package(
                id = it.id.orEmpty(),
                name = it.name.orEmpty(),
                dimensions = it.dimensions.orEmpty()
            )
        } ?: emptyList()
    )
}
