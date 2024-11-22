package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.WooShippingLabelPackageRepository.StorePackages
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.PackageResponse
import javax.inject.Inject

class WooShippingLabelPackageMapper @Inject constructor() {
    operator fun invoke(response: PackageResponse): StorePackages {
        return StorePackages(
            savedPackages = emptyList(),
            carrierPackages = emptyList()
        )
    }
}
