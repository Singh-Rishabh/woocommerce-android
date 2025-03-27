package com.cataloghub.android.ui.payments.cardreader

import com.cataloghub.android.cardreader.config.CardReaderConfigFactory
import javax.inject.Inject

class CardReaderCountryConfigProvider @Inject constructor(
    private val cardReaderConfigFactory: CardReaderConfigFactory,
) {
    fun provideCountryConfigFor(countryCode: String?) =
        cardReaderConfigFactory.getCardReaderConfigFor(countryCode)
}
