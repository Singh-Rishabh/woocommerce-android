package com.cataloghub.android.ui.orders.creation.taxes

import com.cataloghub.android.extensions.isNotNullOrEmpty
import com.cataloghub.android.model.Address
import com.cataloghub.android.model.AmbiguousLocation
import com.cataloghub.android.model.Location
import com.cataloghub.android.model.toAppModel
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.orders.creation.taxes.rates.TaxRate
import org.wordpress.android.fluxc.store.WCDataStore
import javax.inject.Inject

class GetAddressFromTaxRate @Inject constructor(
    private val dataStore: WCDataStore,
    private val selectedSite: SelectedSite,
) {
    suspend operator fun Address.invoke(taxRate: TaxRate): Address {
        dataStore.fetchCountriesAndStates(selectedSite.get())
        val country: Location = dataStore.getCountries().firstOrNull {
            it.code == taxRate.countryCode
        }
            ?.toAppModel()
            ?: Location.EMPTY

        val state = dataStore.getStates(country.code).firstOrNull {
            it.code == taxRate.stateCode
        }
            ?.toAppModel()
            ?.let { AmbiguousLocation.Defined(it) }
            ?: AmbiguousLocation.Defined(Location.EMPTY)

        val city = if (taxRate.city.isNotNullOrEmpty()) {
            taxRate.city
        } else {
            taxRate.cities?.firstOrNull() ?: ""
        }

        val postCode = if (taxRate.postcode.isNotNullOrEmpty()) {
            taxRate.postcode
        } else {
            taxRate.postCodes?.firstOrNull() ?: ""
        }
        return copy(
            city = city,
            state = state,
            country = country,
            postcode = postCode
        )
    }
}
