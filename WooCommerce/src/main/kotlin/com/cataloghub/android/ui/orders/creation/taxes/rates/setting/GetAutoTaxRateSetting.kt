package com.cataloghub.android.ui.orders.creation.taxes.rates.setting

import com.cataloghub.android.AppPrefs
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.orders.creation.taxes.rates.TaxRate
import com.cataloghub.android.ui.orders.creation.taxes.rates.TaxRateRepository
import javax.inject.Inject

class GetAutoTaxRateSetting @Inject constructor(
    private val selectedSite: SelectedSite,
    private val prefs: AppPrefs,
    private val taxRateRepository: TaxRateRepository
) {
    suspend operator fun invoke(): TaxRate? {
        val taxRateId = prefs.getAutoTaxRateId()
        return if (prefs.isAutoTaxRateEnabled() && taxRateId != -1L) {
            taxRateRepository.getTaxRate(
                selectedSite = selectedSite,
                taxRateId = taxRateId
            )
        } else {
            null
        }
    }
}
