package com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.models.WooShippingCarrier
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRateModel.Option
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.networking.WooShippingRatesDTO
import javax.inject.Inject

class WooShippingRatesDatasourceMapper @Inject constructor() {
    companion object {
        private const val DEFAULT_RATE_OPTION = "default"
        private const val SIGNATURE_RATE_OPTION = "signature_required"
        private const val ADULT_SIGNATURE_RATE_OPTION = "adult_signature_required"

        private const val CARRIER_USPS_KEY = "usps"
        private const val CARRIER_UPS_KEY = "ups"
        private const val CARRIER_FEDEX_KEY = "fedex"
        private const val CARRIER_DHL_EXPRESS_KEY = "dhlexpress"
        private const val CARRIER_DHL_ECOMMERCE_KEY = "dhlecommerce"
        private const val CARRIER_DHL_ECOMMERCE_ASIA_KEY = "dhlecommerceasia"
    }

    operator fun invoke(response: Map<String, Map<String, WooShippingRatesDTO>>?): List<WooShippingRateOptionsModel> {
        val optionsMap = mutableMapOf<String, MutableList<WooShippingRateModel>>()
        response?.forEach { (packageId, ratesMap) ->
            ratesMap.forEach { (rateOptionId, wooShippingRates) ->
                wooShippingRates.rates.forEach { rate ->
                    val option = WooShippingRateModel(
                        packageId = packageId,
                        shipmentId = rate.shipmentId,
                        rateId = rate.rateId,
                        serviceId = rate.serviceId,
                        carrierId = rate.carrierId,
                        serviceName = rate.title,
                        deliveryDays = rate.deliveryDays,
                        price = rate.rate,
                        discount = rate.retailRate.minus(rate.rate),
                        option = getOption(rateOptionId),
                        carrier = getCarrier(rate.carrierId)
                    )
                    optionsMap.getOrPut(key = option.serviceId, defaultValue = { mutableListOf() }).add(option)
                }
            }
        }
        return optionsMap.mapNotNull {
            if (it.value.isEmpty()) {
                return@mapNotNull null
            }
            WooShippingRateOptionsModel(
                rateOptions = it.value.associateBy { rate -> rate.option }
            )
        }
    }

    private fun getOption(rateOptionId: String): Option {
        return when (rateOptionId) {
            DEFAULT_RATE_OPTION -> Option.DEFAULT
            SIGNATURE_RATE_OPTION -> Option.SIGNATURE
            ADULT_SIGNATURE_RATE_OPTION -> Option.ADULT_SIGNATURE
            else -> Option.DEFAULT
        }
    }

    private fun getCarrier(carrierId: String) =
        when (carrierId) {
            CARRIER_USPS_KEY -> WooShippingCarrier.USPS
            CARRIER_FEDEX_KEY -> WooShippingCarrier.FEDEX
            CARRIER_UPS_KEY -> WooShippingCarrier.UPS
            CARRIER_DHL_EXPRESS_KEY, CARRIER_DHL_ECOMMERCE_KEY, CARRIER_DHL_ECOMMERCE_ASIA_KEY -> WooShippingCarrier.DHL
            else -> WooShippingCarrier.UNKNOWN
        }
}
