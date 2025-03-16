package com.cataloghub.android.ui.dashboard.topperformers

import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.ui.analytics.ranges.StatsTimeRangeSelection.SelectionType
import com.cataloghub.android.ui.dashboard.data.TopPerformersCustomDateRangeDataStore
import com.cataloghub.android.ui.dashboard.domain.GetSelectedDateRange
import com.cataloghub.android.util.DateUtils
import javax.inject.Inject

class GetSelectedRangeForTopPerformers @Inject constructor(
    private val appPrefs: AppPrefsWrapper,
    customDateRangeDataStore: TopPerformersCustomDateRangeDataStore,
    dateUtils: DateUtils
) : GetSelectedDateRange(appPrefs, customDateRangeDataStore, dateUtils) {
    override fun getSelectedRange(): SelectionType =
        runCatching {
            SelectionType.valueOf(appPrefs.getActiveTopPerformersTab())
        }.getOrDefault(SelectionType.TODAY)
}
