package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.CarrierDAO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.CarrierPackageGroupDAO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.PackageDAO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.StorePackagesDAO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.CarrierPackageGroupDTO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.CarrierPredefinedPackagesDTO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.PackageResponse
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.PredefinedPackageDTO
import javax.inject.Inject

class WooShippingLabelPackageMapper @Inject constructor() {
    operator fun invoke(response: PackageResponse): StorePackagesDAO {
        val savedPackagesResponse = response.packages?.saved?.predefined ?: emptyList()

        return StorePackagesDAO(
            savedPackages = mapSavedPackages(savedPackagesResponse),
            carrierPackages = mapCarrierPackages(response.packages?.predefined)
        )
    }

    private fun mapSavedPackages(savedResponse: List<PredefinedPackageDTO>): List<PackageDAO> {
        return savedResponse.map {
            PackageDAO(
                id = it.id.orEmpty(),
                name = it.name.orEmpty(),
                dimensions = it.dimensions.orEmpty()
            )
        }
    }

    private fun mapCarrierPackages(carrierPackagesResponse: CarrierPredefinedPackagesDTO?): List<CarrierDAO> {
        val usps = mutableListOf<CarrierPackageGroupDAO>().apply {
            carrierPackagesResponse?.usps?.let { usps ->
                usps.flatBoxes?.toCarrierGroup()?.let { add(it) }
                usps.boxes?.toCarrierGroup()?.let { add(it) }
                usps.expressBoxes?.toCarrierGroup()?.let { add(it) }
                usps.envelopes?.toCarrierGroup()?.let { add(it) }
            }
        }.let { CarrierDAO(it) }

        val dhl = mutableListOf<CarrierPackageGroupDAO>().apply {
            carrierPackagesResponse?.dhlExpress?.let { dhl ->
                dhl.domesticAndInternationalPackages?.toCarrierGroup()?.let { add(it) }
            }
        }.let { CarrierDAO(it) }

        return listOf(usps, dhl)
    }

    private fun CarrierPackageGroupDTO.toCarrierGroup() = CarrierPackageGroupDAO(
        description = title.orEmpty(),
        packages = definitions?.map {
            PackageDAO(
                id = it.id.orEmpty(),
                name = it.name.orEmpty(),
                dimensions = it.dimensions.orEmpty()
            )
        } ?: emptyList()
    )
}
