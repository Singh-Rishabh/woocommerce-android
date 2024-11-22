package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.CarrierGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.Package
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.StorePackages
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
        return emptyList()
    }
}
