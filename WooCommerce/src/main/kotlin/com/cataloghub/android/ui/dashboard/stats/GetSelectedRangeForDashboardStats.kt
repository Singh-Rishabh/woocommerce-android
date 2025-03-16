package com.cataloghub.android.ui.dashboard.stats

import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.ui.analytics.ranges.StatsTimeRangeSelection.SelectionType
import com.cataloghub.android.ui.dashboard.data.StatsCustomDateRangeDataStore
import com.cataloghub.android.ui.dashboard.domain.GetSelectedDateRange
import com.cataloghub.android.util.DateUtils
import javax.inject.Inject

class GetSelectedRangeForDashboardStats @Inject constructor(
    private val appPrefs: AppPrefsWrapper,
    customDateRangeDataStore: StatsCustomDateRangeDataStore,
    dateUtils: DateUtils
) : GetSelectedDateRange(appPrefs, customDateRangeDataStore, dateUtils) {
    override fun getSelectedRange(): SelectionType =
        runCatching {
            SelectionType.valueOf(appPrefs.getActiveStoreStatsTab())
        }.getOrDefault(SelectionType.TODAY)
}
