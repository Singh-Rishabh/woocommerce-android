package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.CarrierType.DHL
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.CarrierType.USPS
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.CarrierPackageGroupDTO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.CarrierPredefinedPackagesDTO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.CustomPackageDTO
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.PackageResponse
import javax.inject.Inject

class WooShippingLabelPackageMapper @Inject constructor() {
    operator fun invoke(response: PackageResponse): StorePackagesDAO {
        val savedPackagesResponse = response.packages?.saved?.custom ?: emptyList()

        return StorePackagesDAO(
            savedPackages = mapSavedPackages(savedPackagesResponse),
            carrierPackages = mapCarrierPackages(response.packages?.predefined)
        )
    }

    private fun mapSavedPackages(savedResponse: List<CustomPackageDTO>): List<PackageDAO> {
        return savedResponse.map {
            PackageDAO(
                id = it.id.orEmpty(),
                name = it.name.orEmpty(),
                dimensions = it.dimensions.orEmpty(),
                isLetter = it.isLetter ?: false
            )
        }
    }

    private fun mapCarrierPackages(
        carrierPackagesResponse: CarrierPredefinedPackagesDTO?
    ): Map<CarrierType, CarrierDAO> {
        val uspsPackages = mutableListOf<CarrierPackageGroupDAO>().apply {
            carrierPackagesResponse?.usps?.let { usps ->
                usps.flatBoxes?.toCarrierGroup()?.let { add(it) }
                usps.boxes?.toCarrierGroup()?.let { add(it) }
                usps.expressBoxes?.toCarrierGroup()?.let { add(it) }
                usps.envelopes?.toCarrierGroup()?.let { add(it) }
            }
        }.let { CarrierDAO(it) }

        val dhlPackages = mutableListOf<CarrierPackageGroupDAO>().apply {
            carrierPackagesResponse?.dhlExpress?.let { dhl ->
                dhl.domesticAndInternationalPackages?.toCarrierGroup()?.let { add(it) }
            }
        }.let { CarrierDAO(it) }

        return mapOf(
            USPS to uspsPackages,
            DHL to dhlPackages
        )
    }

    private fun CarrierPackageGroupDTO.toCarrierGroup() = CarrierPackageGroupDAO(
        description = title.orEmpty(),
        packages = definitions?.map {
            PackageDAO(
                id = it.id.orEmpty(),
                name = it.name.orEmpty(),
                dimensions = it.dimensions.orEmpty(),
                isLetter = it.isLetter ?: false
            )
        } ?: emptyList()
    )
}
