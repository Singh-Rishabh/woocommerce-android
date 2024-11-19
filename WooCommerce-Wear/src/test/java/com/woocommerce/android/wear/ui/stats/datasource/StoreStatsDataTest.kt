package com.woocommerce.android.wear.ui.stats.datasource

import com.woocommerce.android.wear.ui.stats.datasource.StoreStatsData.StatRequest.*
import org.junit.Assert.*
import org.junit.Test

class StoreStatsDataTest {

    @Test
    fun `StoreStatsData with finished requests should return correct values`() {
        val revenueData = StoreStatsData.RevenueData(totalRevenue = "1000", orderCount = 10)
        val visitorData = 100

        val storeStatsData = StoreStatsData(revenueData, visitorData)

        assertEquals("1000", storeStatsData.revenue)
        assertEquals(10, storeStatsData.ordersCount)
        assertEquals(100, storeStatsData.visitorsCount)
        assertEquals("10%", storeStatsData.conversionRate)
        assertTrue(storeStatsData.isComplete)
    }

    @Test
    fun `StoreStatsData with waiting requests should return default values`() {
        val revenueRequest = Waiting<StoreStatsData.RevenueData>()
        val visitorRequest = Waiting<Int>()

        val storeStatsData = StoreStatsData(revenueRequest, visitorRequest)

        assertEquals("", storeStatsData.revenue)
        assertEquals(0, storeStatsData.ordersCount)
        assertEquals(0, storeStatsData.visitorsCount)
        assertEquals("0%", storeStatsData.conversionRate)
        assertFalse(storeStatsData.isComplete)
    }

    @Test
    fun `StoreStatsData with error requests should return default values`() {
        val revenueRequest = Error<StoreStatsData.RevenueData>()
        val visitorRequest = Error<Int>()

        val storeStatsData = StoreStatsData(revenueRequest, visitorRequest)

        assertEquals("", storeStatsData.revenue)
        assertEquals(0, storeStatsData.ordersCount)
        assertEquals(0, storeStatsData.visitorsCount)
        assertEquals("0%", storeStatsData.conversionRate)
        assertFalse(storeStatsData.isComplete)
    }

    @Test
    fun `StoreStatsData with mixed finished and waiting requests should return incomplete`() {
        val revenueData = StoreStatsData.RevenueData(totalRevenue = "1000", orderCount = 10)
        val visitorRequest = Waiting<Int>()

        val storeStatsData = StoreStatsData(Finished(revenueData), visitorRequest)

        assertFalse(storeStatsData.isComplete)
    }

    @Test
    fun `StoreStatsData with mixed finished and error requests should return complete`() {
        val revenueData = StoreStatsData.RevenueData(totalRevenue = "1000", orderCount = 10)
        val visitorRequest = Error<Int>()

        val storeStatsData = StoreStatsData(Finished(revenueData), visitorRequest)

        assertTrue(storeStatsData.isComplete)
    }

    @Test
    fun `StoreStatsData with mixed waiting and error requests should return complete`() {
        val revenueRequest = Waiting<StoreStatsData.RevenueData>()
        val visitorRequest = Error<Int>()

        val storeStatsData = StoreStatsData(revenueRequest, visitorRequest)

        assertFalse(storeStatsData.isComplete)
    }
}
