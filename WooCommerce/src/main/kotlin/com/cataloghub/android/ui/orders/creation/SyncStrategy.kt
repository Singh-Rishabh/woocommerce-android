package com.cataloghub.android.ui.orders.creation

import com.cataloghub.android.model.Order
import kotlinx.coroutines.flow.Flow

interface SyncStrategy {
    fun syncOrderChanges(changes: Flow<Order>, retryTrigger: Flow<Unit>): Flow<CreateUpdateOrder.OrderUpdateStatus>
}
