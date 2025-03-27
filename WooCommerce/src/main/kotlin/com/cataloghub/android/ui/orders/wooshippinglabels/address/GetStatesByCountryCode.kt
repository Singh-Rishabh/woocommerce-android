package com.cataloghub.android.ui.orders.wooshippinglabels.address

import com.cataloghub.android.model.Location
import com.cataloghub.android.model.toAppModel
import com.cataloghub.android.util.CoroutineDispatchers
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WCDataStore
import javax.inject.Inject

class GetStatesByCountryCode @Inject constructor(
    private val dataStore: WCDataStore,
    private val coroutineDispatchers: CoroutineDispatchers
) {
    suspend operator fun invoke(countryCode: String): List<Location> {
        return withContext(coroutineDispatchers.io) {
            dataStore.getStates(countryCode).map { it.toAppModel() }
        }
    }
}
