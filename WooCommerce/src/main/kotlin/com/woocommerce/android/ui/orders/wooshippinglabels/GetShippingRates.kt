package com.woocommerce.android.ui.orders.wooshippinglabels

import com.woocommerce.android.R
import com.woocommerce.android.model.Address
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.PackageDAO
import kotlinx.coroutines.delay
import javax.inject.Inject
import kotlin.random.Random

class GetShippingRates @Inject constructor() {
    private val cheapestComparator = Comparator<ShippingRateUI> { r1, r2 ->
        r1.rate.substring(1).toBigDecimal().compareTo(r2.rate.substring(1).toBigDecimal())
    }

    private val fastestComparator = Comparator<ShippingRateUI> { r1, r2 ->
        r1.deliveryDays.compareTo(r2.deliveryDays)
    }

    suspend operator fun invoke(
        selectedPackage: PackageDAO,
        sortOrder: ShippingSortOption,
        shipTo: Address,
        shipFrom: OriginShippingAddress
    ): Result<Map<Carrier, List<ShippingRateUI>>> {
        delay(1_000)
        val comparator = when (sortOrder) {
            ShippingSortOption.CHEAPEST -> {
                cheapestComparator
            }

            ShippingSortOption.FASTEST -> {
                fastestComparator
            }
        }
        val carriers = if (selectedPackage.isLetter) {
            listOf(
                Carrier(
                    id = "dhl",
                    name = "DHL Express",
                    logoRes = R.drawable.dhl_logo
                ),
                Carrier(
                    id = "usps",
                    name = "USPS",
                    logoRes = R.drawable.usps_logo
                )
            )
        } else {
            listOf(
                Carrier(
                    id = "dhl",
                    name = "DHL Express",
                    logoRes = R.drawable.dhl_logo
                ),
                Carrier(
                    id = "usps",
                    name = "USPS",
                    logoRes = R.drawable.usps_logo
                ),
                Carrier(
                    id = "ups",
                    name = "UPS",
                    logoRes = R.drawable.ups_logo
                )
            )
        }

        return Result.success(
            carriers.associateWith {
                generateRates(
                    it.name,
                    Random(0).nextInt(from = 3, until = 10)
                ).sortedWith(comparator)
            }
        )
    }
}
