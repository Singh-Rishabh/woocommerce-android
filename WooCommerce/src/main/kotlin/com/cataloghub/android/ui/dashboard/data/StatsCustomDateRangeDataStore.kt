package com.cataloghub.android.ui.dashboard.data

import androidx.datastore.core.DataStore
import com.cataloghub.android.datastore.DataStoreQualifier
import com.cataloghub.android.datastore.DataStoreType
import com.cataloghub.android.ui.mystore.data.CustomDateRange
import javax.inject.Inject

class StatsCustomDateRangeDataStore @Inject constructor(
    @DataStoreQualifier(DataStoreType.DASHBOARD_STATS) dataStore: DataStore<CustomDateRange>
) : CustomDateRangeDataStore(dataStore)
