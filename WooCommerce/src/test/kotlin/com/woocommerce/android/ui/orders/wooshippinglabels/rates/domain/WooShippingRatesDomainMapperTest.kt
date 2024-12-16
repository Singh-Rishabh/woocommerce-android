package com.woocommerce.android.ui.orders.wooshippinglabels.rates.domain

import com.woocommerce.android.ui.orders.wooshippinglabels.models.WooShippingCarrier
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRateModel
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRateOptionsModel
import com.woocommerce.android.util.CurrencyFormatter
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.ResourceProvider
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import java.math.BigDecimal
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingRatesDomainMapperTest : BaseUnitTest() {
    private val currencyFormatter: CurrencyFormatter = mock {
        on(it.formatCurrency(any<BigDecimal>(), any(), any())).thenAnswer { i -> "${i.arguments[1]}${i.arguments[0]}" }
    }
    private val resourceProvider: ResourceProvider = mock {
        on(it.getString(any(), any())).thenAnswer { i -> "formatted ${i.arguments[1]}" }
        on(it.getString(any())).thenAnswer { i -> "formatted ${i.arguments[0]}" }
        on(it.getQuantityString(any(), anyOrNull(), anyOrNull(), anyOrNull()))
            .thenAnswer { i -> "formatted ${i.arguments[0]}" }
    }


    private val sut = WooShippingRatesDomainMapper(
        resourceProvider = resourceProvider,
        currencyFormatter = currencyFormatter
    )

    @Test
    fun `mapping rates generates the expected number of carriers and rates`() {
        // Randomly generating the number of carriers and rates to test the mapper
        val random = Random(85)
        var carriersNum = random.nextInt(0, WooShippingCarrier.entries.size)
        var ratesNum = random.nextInt(WooShippingCarrier.entries.size + 1, 20)

        var rates = generateRates(carriersNum, ratesNum)
        var mappedRates = sut(rates, "USD")

        assertEquals(mappedRates.keys.size, carriersNum)
        assertEquals(mappedRates.values.sumOf { it.size }, ratesNum)

        // Repeat the test with other random values
        carriersNum = random.nextInt(0, WooShippingCarrier.entries.size)
        ratesNum = random.nextInt(WooShippingCarrier.entries.size + 1, 20)

        rates = generateRates(carriersNum, ratesNum)
        mappedRates = sut(rates, "USD")

        assertEquals(mappedRates.keys.size, carriersNum)
        assertEquals(mappedRates.values.sumOf { it.size }, ratesNum)

        carriersNum = random.nextInt(0, WooShippingCarrier.entries.size)
        ratesNum = random.nextInt(WooShippingCarrier.entries.size + 1, 20)

        rates = generateRates(carriersNum, ratesNum)
        mappedRates = sut(rates, "USD")

        assertEquals(mappedRates.keys.size, carriersNum)
        assertEquals(mappedRates.values.sumOf { it.size }, ratesNum)
    }

    private fun generateRates(carriersNum: Int, ratesNum: Int): List<WooShippingRateOptionsModel> {
        require(WooShippingCarrier.entries.size >= carriersNum) {
            "The number of carriers should be less than the number of options"
        }
        require(carriersNum <= ratesNum) {
            "At least one carrier option should be used for the rates"
        }
        val carriers = WooShippingCarrier.entries.take(carriersNum)
        val rates = mutableListOf<WooShippingRateOptionsModel>()
        val lastPosition = ratesNum -1
        for (i in 0..lastPosition) {
            val ratesMap = mutableMapOf<WooShippingRateModel.Option, WooShippingRateModel>()
            val carrier = if (i < carriersNum) carriers[i] else carriers.random()
            ratesMap[WooShippingRateModel.Option.DEFAULT] = WooShippingRateModel(
                carrier = carrier,
                deliveryDays = (1..10).random(),
                discount = BigDecimal.ZERO,
                hasFreePickup = true,
                insurance = BigDecimal.TEN,
                isTrackingEnabled = true,
                carrierId = carrier.ordinal.toString(),
                option = WooShippingRateModel.Option.DEFAULT,
                packageId = i.toString(),
                price = BigDecimal.TEN,
                rateId = i.toString(),
                shipmentId = i.toString(),
                serviceId = i.toString(),
                serviceName = "Default"
            )

            ratesMap[WooShippingRateModel.Option.SIGNATURE] = WooShippingRateModel(
                carrier = carrier,
                deliveryDays = (1..10).random(),
                discount = BigDecimal.ZERO,
                hasFreePickup = true,
                insurance = BigDecimal.TEN,
                isTrackingEnabled = true,
                carrierId = carrier.ordinal.toString(),
                option = WooShippingRateModel.Option.SIGNATURE,
                packageId = i.toString(),
                price = BigDecimal.TEN,
                rateId = i.toString(),
                shipmentId = i.toString(),
                serviceId = i.toString(),
                serviceName = "Signature Required"
            )

            ratesMap[WooShippingRateModel.Option.ADULT_SIGNATURE] = WooShippingRateModel(
                carrier = carrier,
                deliveryDays = (1..10).random(),
                discount = BigDecimal.ZERO,
                hasFreePickup = true,
                insurance = BigDecimal.TEN,
                isTrackingEnabled = true,
                carrierId = carrier.ordinal.toString(),
                option = WooShippingRateModel.Option.ADULT_SIGNATURE,
                packageId = i.toString(),
                price = BigDecimal.TEN,
                rateId = i.toString(),
                shipmentId = i.toString(),
                serviceId = i.toString(),
                serviceName = "Adult Signature Required"
            )
            rates.add(WooShippingRateOptionsModel(ratesMap))
        }
        return rates
    }
}
