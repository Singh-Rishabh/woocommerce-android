package com.cataloghub.android.ui.analytics

import com.cataloghub.android.model.BundleItem
import com.cataloghub.android.model.BundleStat
import com.cataloghub.android.model.DeltaPercentage
import com.cataloghub.android.model.GiftCardsStat
import com.cataloghub.android.model.OrdersStat
import com.cataloghub.android.model.ProductsStat
import com.cataloghub.android.model.RevenueStat
import com.cataloghub.android.model.SessionStat
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.BundlesResult
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.BundlesResult.BundlesData
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.GiftCardResult
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.GiftCardResult.GiftCardData
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.OrdersResult
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.OrdersResult.OrdersData
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.ProductsResult
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.ProductsResult.ProductsData
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.RevenueResult
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.RevenueResult.RevenueData
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.VisitorsResult
import com.cataloghub.android.ui.analytics.hub.sync.AnalyticsRepository.VisitorsResult.VisitorsData
import com.cataloghub.android.ui.analytics.ranges.StatsTimeRangeSelection.SelectionType.CUSTOM
import com.cataloghub.android.ui.analytics.ranges.StatsTimeRangeSelection.SelectionType.TODAY
import java.util.Calendar
import java.util.Date
import java.util.Locale

val testRangeSelection = TODAY.generateSelectionData(
    calendar = Calendar.getInstance(),
    locale = Locale.getDefault(),
    referenceStartDate = Date(),
    referenceEndDate = Date()
)

val testCustomRangeSelection = CUSTOM.generateSelectionData(
    calendar = Calendar.getInstance(),
    locale = Locale.getDefault(),
    referenceStartDate = Date(),
    referenceEndDate = Date()
)

val testRevenueStat = RevenueStat(
    totalValue = 1234.5,
    totalDelta = DeltaPercentage.NotExist,
    netValue = 1000.0,
    netDelta = DeltaPercentage.NotExist,
    currencyCode = "",
    totalRevenueByInterval = emptyList(),
    netRevenueByInterval = emptyList()
)

val testOrdersStat = OrdersStat(
    ordersCount = 23,
    ordersCountDelta = DeltaPercentage.NotExist,
    avgOrderValue = 500.0,
    avgOrderDelta = DeltaPercentage.NotExist,
    currencyCode = "",
    ordersCountByInterval = emptyList(),
    avgOrderValueByInterval = emptyList()
)

val testProductsStat = ProductsStat(
    itemsSold = 123,
    itemsSoldDelta = DeltaPercentage.NotExist,
    products = emptyList()
)

const val testVisitorsCount = 150

val testSessionStat = SessionStat(
    ordersCount = testOrdersStat.ordersCount,
    visitorsCount = testVisitorsCount
)

val testBundleStat = BundleStat(
    bundlesSold = 123,
    bundlesSoldDelta = DeltaPercentage.Value(34),
    bundles = listOf(
        BundleItem(
            name = "item 1",
            netSales = 300.00,
            image = null,
            quantity = 5,
            currencyCode = null
        ),
        BundleItem(
            name = "item 1",
            netSales = 300.00,
            image = null,
            quantity = 5,
            currencyCode = null
        )
    )
)

val testGiftCardStat = GiftCardsStat(
    usedValue = 200,
    usedDelta = DeltaPercentage.Value(34),
    netValue = 567.90,
    netDelta = DeltaPercentage.Value(45),
    currencyCode = "",
    usedByInterval = emptyList(),
    netRevenueByInterval = emptyList()
)

val testRevenueResult = RevenueData(testRevenueStat) as RevenueResult
val testOrdersResult = OrdersData(testOrdersStat) as OrdersResult
val testProductsResult = ProductsData(testProductsStat) as ProductsResult
val testVisitorsResult = VisitorsData(testVisitorsCount) as VisitorsResult
val testBundlesResult = BundlesData(testBundleStat) as BundlesResult
val testGiftCardResult = GiftCardData(testGiftCardStat) as GiftCardResult
