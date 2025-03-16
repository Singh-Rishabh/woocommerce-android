package com.cataloghub.android.ui.dashboard.data

import androidx.datastore.core.DataStore
import com.cataloghub.android.datastore.DataStoreQualifier
import com.cataloghub.android.datastore.DataStoreType
import com.cataloghub.android.ui.mystore.data.CustomDateRange
import javax.inject.Inject

class CouponsCustomDateRangeDataStore @Inject constructor(
    @DataStoreQualifier(DataStoreType.COUPONS) dataStore: DataStore<CustomDateRange>
) : CustomDateRangeDataStore(dataStore)
