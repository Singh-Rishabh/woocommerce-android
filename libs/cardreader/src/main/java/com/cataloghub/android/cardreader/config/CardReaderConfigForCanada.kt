package com.cataloghub.android.cardreader.config

import com.cataloghub.android.cardreader.connection.ReaderType
import com.cataloghub.android.cardreader.payments.CardPaymentStatus.PaymentMethodType
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
object CardReaderConfigForCanada : CardReaderConfigForSupportedCountry(
    currency = "CAD",
    countryCode = "CA",
    supportedReaders = listOf(
        ReaderType.ExternalReader.WisePade3,
        ReaderType.BuildInReader.CotsDevice,
    ),
    paymentMethodTypes = listOf(
        PaymentMethodType.CARD_PRESENT,
        PaymentMethodType.INTERAC_PRESENT
    ),
    supportedExtensions = listOf(
        SupportedExtension(
            type = SupportedExtensionType.WC_PAY,
            supportedSince = "4.0.0"
        ),
    ),
    minimumAllowedChargeAmount = BigDecimal("0.50"),
    maximumTTPAllowedChargeAmountWithoutPin = BigDecimal("250.00"),
)
