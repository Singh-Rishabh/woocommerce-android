package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingNetworkingMapperTest : BaseUnitTest() {
    val sut = WooShippingNetworkingMapper()

    @Test
    fun `when all field are null then return empty model`() {
        val emptyDTO = StoreOptionsDTO(
            weightUnit = null,
            currencySymbol = null,
            dimensionUnit = null,
            originCountry = null
        )

        val result = sut.invoke(emptyDTO)

        assert(result == StoreOptionsModel.EMPTY)
    }

    @Test
    fun `when a dto is received then return the expected model`() {
        val dto = StoreOptionsDTO(
            weightUnit = "kg",
            currencySymbol = "$",
            dimensionUnit = "cm",
            originCountry = "US"
        )

        val result = sut.invoke(dto)

        assertEquals(result.currencySymbol, dto.currencySymbol)
        assertEquals(result.weightUnit, dto.weightUnit)
        assertEquals(result.dimensionUnit, dto.dimensionUnit)
        assertEquals(result.originCountry, dto.originCountry)
    }
}
