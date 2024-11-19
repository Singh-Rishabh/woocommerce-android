package com.woocommerce.android.wear.ui.stats.datasource

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
        val revenueRequest = StoreStatsData.StatRequest.Waiting<StoreStatsData.RevenueData>()
        val visitorRequest = StoreStatsData.StatRequest.Waiting<Int>()

        val storeStatsData = StoreStatsData(revenueRequest, visitorRequest)

        assertEquals("", storeStatsData.revenue)
        assertEquals(0, storeStatsData.ordersCount)
        assertEquals(0, storeStatsData.visitorsCount)
        assertEquals("0%", storeStatsData.conversionRate)
        assertFalse(storeStatsData.isComplete)
    }

    @Test
    fun `StoreStatsData with error requests should return default values`() {
        val revenueRequest = StoreStatsData.StatRequest.Error<StoreStatsData.RevenueData>()
        val visitorRequest = StoreStatsData.StatRequest.Error<Int>()

        val storeStatsData = StoreStatsData(revenueRequest, visitorRequest)

        assertEquals("", storeStatsData.revenue)
        assertEquals(0, storeStatsData.ordersCount)
        assertEquals(0, storeStatsData.visitorsCount)
        assertEquals("0%", storeStatsData.conversionRate)
        assertTrue(storeStatsData.isComplete)
    }
}
