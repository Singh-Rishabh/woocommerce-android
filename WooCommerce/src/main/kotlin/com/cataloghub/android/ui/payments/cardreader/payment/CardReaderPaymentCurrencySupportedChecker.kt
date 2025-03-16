package com.cataloghub.android.ui.payments.cardreader.payment

import com.cataloghub.android.cardreader.config.CardReaderConfigForSupportedCountry
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.payments.cardreader.CardReaderCountryConfigProvider
import com.cataloghub.android.util.CoroutineDispatchers
import com.cataloghub.android.util.WooLog
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.store.WooCommerceStore
import javax.inject.Inject

class CardReaderPaymentCurrencySupportedChecker @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val wooStore: WooCommerceStore,
    private val selectedSite: SelectedSite,
    private val cardReaderCountryConfigProvider: CardReaderCountryConfigProvider,
) {
    suspend fun isCurrencySupported(currency: String): Boolean {
        val cardReaderConfigFor = cardReaderCountryConfigProvider.provideCountryConfigFor(getStoreCountryCode())
        return (cardReaderConfigFor as? CardReaderConfigForSupportedCountry)?.currency == currency
    }

    private suspend fun getStoreCountryCode(): String? {
        return withContext(dispatchers.io) {
            wooStore.getStoreCountryCode(selectedSite.get()) ?: null.also {
                WooLog.e(WooLog.T.CARD_READER, "Store's country code not found.")
            }
        }
    }
}
