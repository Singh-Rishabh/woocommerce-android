package com.cataloghub.android.ui.dashboard.coupons

import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.ui.analytics.ranges.StatsTimeRangeSelection.SelectionType
import com.cataloghub.android.ui.dashboard.data.CouponsCustomDateRangeDataStore
import com.cataloghub.android.ui.dashboard.domain.GetSelectedDateRange
import com.cataloghub.android.util.DateUtils
import javax.inject.Inject

class GetSelectedRangeForCoupons @Inject constructor(
    private val appPrefs: AppPrefsWrapper,
    customDateRangeDataStore: CouponsCustomDateRangeDataStore,
    dateUtils: DateUtils
) : GetSelectedDateRange(appPrefs, customDateRangeDataStore, dateUtils) {
    override fun getSelectedRange(): SelectionType =
        runCatching {
            SelectionType.valueOf(appPrefs.getActiveCouponsTab())
        }.getOrDefault(SelectionType.TODAY)
}
