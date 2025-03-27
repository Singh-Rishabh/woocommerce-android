package com.cataloghub.android.ui.orders.list

import com.cataloghub.android.background.LastUpdateDataStore
import javax.inject.Inject

class ObserveOrdersListLastUpdate @Inject constructor(private val lastUpdateDataStore: LastUpdateDataStore) {
    operator fun invoke(listId: Int) = lastUpdateDataStore.getLastUpdateKeyByOrdersListId(listId).let { key ->
        lastUpdateDataStore.observeLastUpdate(key)
    }
}
