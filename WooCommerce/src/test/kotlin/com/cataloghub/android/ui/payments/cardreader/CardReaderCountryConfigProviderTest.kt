package com.cataloghub.android.ui.payments.cardreader

import com.cataloghub.android.cardreader.config.CardReaderConfigFactory
import com.cataloghub.android.cardreader.config.CardReaderConfigForCanada
import com.cataloghub.android.cardreader.config.CardReaderConfigForGB
import com.cataloghub.android.cardreader.config.CardReaderConfigForUSA
import com.cataloghub.android.cardreader.config.CardReaderConfigForUnsupportedCountry
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CardReaderCountryConfigProviderTest {
    private val cardReaderConfigFactory: CardReaderConfigFactory = CardReaderConfigFactory()

    private val cardReaderCountryConfigProvider = CardReaderCountryConfigProvider(
        cardReaderConfigFactory,
    )

    @Test
    fun `given CA country code, when config provide, then Canada returned`() {
        // GIVEN
        val countryCode = "CA"

        // WHEN
        val config = cardReaderCountryConfigProvider.provideCountryConfigFor(countryCode)

        // THEN
        assertThat(config).isInstanceOf(CardReaderConfigForCanada::class.java)
    }

    @Test
    fun `given US country code, when config provide, then USA returned`() {
        // GIVEN
        val countryCode = "US"

        // WHEN
        val config = cardReaderCountryConfigProvider.provideCountryConfigFor(countryCode)

        // THEN
        assertThat(config).isInstanceOf(CardReaderConfigForUSA::class.java)
    }

    @Test
    fun `given RU country code, when config provide, then unsupported config returned`() {
        // GIVEN
        val countryCode = "RU"

        // WHEN
        val config = cardReaderCountryConfigProvider.provideCountryConfigFor(countryCode)

        // THEN
        assertThat(config).isInstanceOf(CardReaderConfigForUnsupportedCountry::class.java)
    }

    @Test
    fun `given GB country code, when config provide, then GB config returned`() {
        // GIVEN
        val countryCode = "GB"

        // WHEN
        val config = cardReaderCountryConfigProvider.provideCountryConfigFor(countryCode)

        // THEN
        assertThat(config).isInstanceOf(CardReaderConfigForGB::class.java)
    }
}
