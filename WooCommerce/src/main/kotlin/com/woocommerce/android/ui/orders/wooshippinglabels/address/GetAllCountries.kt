package com.woocommerce.android.ui.orders.wooshippinglabels.address

import com.woocommerce.android.model.Location
import com.woocommerce.android.model.toAppModel
import com.woocommerce.android.tools.SelectedSite
import org.wordpress.android.fluxc.store.WCDataStore
import javax.inject.Inject

class GetAllCountries @Inject constructor(
    private val dataStore: WCDataStore,
    private val site: SelectedSite
) {
    suspend operator fun invoke(): Result<List<Location>> {
        val cachedCountries = dataStore.getCountries().map { it.toAppModel() }
        return if (cachedCountries.isNotEmpty()) {
            Result.success(cachedCountries)
        } else {
            val siteModel = site.getOrNull() ?: return Result.failure(Exception("No site selected"))
            val result = dataStore.fetchCountriesAndStates(siteModel)
            if (result.isError) {
                val error = result.error.message ?: "Unknown error"
                Result.failure(Exception(error))
            } else {
                Result.success(dataStore.getCountries().map { it.toAppModel() })
            }
        }
    }
}
