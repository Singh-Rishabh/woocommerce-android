package com.woocommerce.android.ui.orders.wooshippinglabels.rates.domain

import com.woocommerce.android.R
import com.woocommerce.android.ui.orders.wooshippinglabels.models.WooShippingCarrier
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRateOptionsModel
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.CarrierUI
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.ui.ShippingRateUI
import javax.inject.Inject

class WooShippingRatesDomainMapper @Inject constructor() {
    operator fun invoke(rates: List<WooShippingRateOptionsModel>): Map<CarrierUI, List<ShippingRateUI>> {
        return rates.groupBy { it.defaultRate.carrier }.map { entry ->
            getCarrier(entry.key) to entry.value.map {
                ShippingRateUI(
                    name = it.defaultRate.serviceName,
                    price = it.defaultRate.price,
                    formattedPrice = it.defaultRate.price.toString(),
                    deliveryDays = it.defaultRate.deliveryDays,
                    insurance = "$200.00",
                    tracking = true,
                    freePickup = true,
                    options = it
                )
            }
        }.toMap()
    }

    private fun getCarrier(carrier: WooShippingCarrier): CarrierUI {
        return when (carrier) {
            WooShippingCarrier.FEDEX -> CarrierUI(
                carrier = carrier,
                name = "FEDEX",
                logoRes = R.drawable.fedex_logo
            )

            WooShippingCarrier.USPS -> CarrierUI(
                carrier = WooShippingCarrier.USPS,
                name = "USPS",
                logoRes = R.drawable.usps_logo
            )

            WooShippingCarrier.UPS -> CarrierUI(
                carrier = WooShippingCarrier.UPS,
                name = "UPS",
                logoRes = R.drawable.ups_logo
            )

            WooShippingCarrier.DHL -> CarrierUI(
                carrier = WooShippingCarrier.DHL,
                name = "DHL Express",
                logoRes = R.drawable.dhl_logo
            )

            WooShippingCarrier.UNKNOWN -> CarrierUI(
                carrier = WooShippingCarrier.UNKNOWN,
                name = "Unknown",
                logoRes = null
            )
        }
    }
}
