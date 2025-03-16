package com.cataloghub.android.cardreader.connection.event

import com.cataloghub.android.cardreader.payments.CardPaymentStatus.AdditionalInfoType

sealed class BluetoothCardReaderMessages {
    data class CardReaderDisplayMessage(val message: AdditionalInfoType) : BluetoothCardReaderMessages()
    data class CardReaderInputMessage(val options: String) : BluetoothCardReaderMessages()
    object CardReaderNoMessage : BluetoothCardReaderMessages()
}
