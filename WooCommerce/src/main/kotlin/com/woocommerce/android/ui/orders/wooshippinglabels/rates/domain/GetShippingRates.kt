package com.woocommerce.android.ui.orders.wooshippinglabels.rates.domain

import com.woocommerce.android.model.Address
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRatesRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.CarrierUI
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.ShippingRateUI
import javax.inject.Inject

class GetShippingRates @Inject constructor(
    private val repository: WooShippingRatesRepository,
    private val shippingMapper: WooShippingRatesDomainMapper
) {

    suspend operator fun invoke(
        orderId: Long,
        selectedPackage: PackageData,
        shipTo: Address,
        shipFrom: OriginShippingAddress,
        weight: Float
    ): Result<Map<CarrierUI, List<ShippingRateUI>>> {
        val result = repository.getShippingRates(
            orderId = orderId,
            selectedPackage = selectedPackage,
            shipTo = shipTo,
            shipFrom = shipFrom,
            weight = weight
        )
        return if (result.isSuccess) {
            val sortedRates = shippingMapper(result.getOrThrow())
            Result.success(sortedRates)
        } else {
            Result.failure(result.exceptionOrNull() ?: Exception("Failed to get shipping rates"))
        }
    }
}
