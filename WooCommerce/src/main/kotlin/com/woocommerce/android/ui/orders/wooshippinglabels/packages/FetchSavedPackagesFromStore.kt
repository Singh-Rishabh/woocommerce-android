package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageData
import javax.inject.Inject

class FetchSavedPackagesFromStore @Inject constructor() {
    operator fun invoke(): List<PackageData> {
        return emptyList()
    }
}
