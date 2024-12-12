package com.woocommerce.android.ui.orders.wooshippinglabels.rates.domain

import com.woocommerce.android.model.Address
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.PackageDAO
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRateOptionsModel
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRatesRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.CarrierUI
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.ShippingRateUI
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.ShippingSortOption
import javax.inject.Inject

class GetShippingRates @Inject constructor(
    private val repository: WooShippingRatesRepository,
    private val shippingMapper: WooShippingRatesDomainMapper
) {
    private val cheapestComparator = Comparator<WooShippingRateOptionsModel> { r1, r2 ->
        r1.defaultRate.price.compareTo(r2.defaultRate.price)
    }

    private val fastestComparator = Comparator<WooShippingRateOptionsModel> { r1, r2 ->
        r1.defaultRate.deliveryDays.compareTo(r2.defaultRate.deliveryDays)
    }

    suspend operator fun invoke(
        selectedPackage: PackageDAO,
        shipTo: Address,
        shipFrom: OriginShippingAddress,
        sortOrder: ShippingSortOption = ShippingSortOption.FASTEST
    ): Result<Map<CarrierUI, List<ShippingRateUI>>> {
        val result = repository.getShippingRates(
            selectedPackage = selectedPackage,
            shipTo = shipTo,
            shipFrom = shipFrom
        )
        val comparator = when (sortOrder) {
            ShippingSortOption.CHEAPEST -> {
                cheapestComparator
            }

            ShippingSortOption.FASTEST -> {
                fastestComparator
            }
        }

        return if (result.isSuccess) {
            val rates = shippingMapper(result.getOrThrow())
            Result.success(rates)
        } else{
            Result.failure(result.exceptionOrNull()?: Exception("Failed to get shipping rates"))
        }
    }
}
