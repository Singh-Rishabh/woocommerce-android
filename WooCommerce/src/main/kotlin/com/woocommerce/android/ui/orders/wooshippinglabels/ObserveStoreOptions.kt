package com.woocommerce.android.ui.orders.wooshippinglabels

import com.woocommerce.android.ui.orders.wooshippinglabels.datasource.ShippingLabelsDataStore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.transformLatest
import javax.inject.Inject

class ObserveStoreOptions @Inject constructor(
    val dataStore: ShippingLabelsDataStore,
    val fetchAccountSettings: FetchAccountSettings,
) {
    private var isFirstValue = true

    @OptIn(ExperimentalCoroutinesApi::class)
    // We will use data store as the source of truth and after the first emission we will refresh the values async.
    operator fun invoke() = dataStore.observeStoreOptions().transformLatest { options ->
        when {
            isFirstValue && options == null -> {
                // If there is no cached data, refresh the store options before emitting any value
                if (fetchAccountSettings().isFailure) {
                    // We will use null as not available
                    emit(null)
                }
            }

            isFirstValue && options != null -> {
                // If there is cached data, emit cached values and refresh the store options async
                emit(options)
                if (fetchAccountSettings().isFailure) {
                    emit(null)
                }
            }

            else -> emit(options)
        }
        isFirstValue = false
    }
}
