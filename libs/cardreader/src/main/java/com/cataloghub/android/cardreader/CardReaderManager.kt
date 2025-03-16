package com.cataloghub.android.cardreader

import com.cataloghub.android.cardreader.connection.CardReader
import com.cataloghub.android.cardreader.connection.CardReaderDiscoveryEvents
import com.cataloghub.android.cardreader.connection.CardReaderStatus
import com.cataloghub.android.cardreader.connection.CardReaderTypesToDiscover
import com.cataloghub.android.cardreader.connection.event.BluetoothCardReaderMessages
import com.cataloghub.android.cardreader.connection.event.CardReaderBatteryStatus
import com.cataloghub.android.cardreader.connection.event.SoftwareUpdateAvailability
import com.cataloghub.android.cardreader.connection.event.SoftwareUpdateStatus
import com.cataloghub.android.cardreader.payments.CardInteracRefundStatus
import com.cataloghub.android.cardreader.payments.CardPaymentStatus
import com.cataloghub.android.cardreader.payments.PaymentData
import com.cataloghub.android.cardreader.payments.PaymentInfo
import com.cataloghub.android.cardreader.payments.RefundConfig
import com.cataloghub.android.cardreader.payments.RefundParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for consumers who want to start accepting POC card payments.
 */
@Suppress("TooManyFunctions")
interface CardReaderManager {
    val initialized: Boolean
    val readerStatus: StateFlow<CardReaderStatus>
    val softwareUpdateStatus: Flow<SoftwareUpdateStatus>
    val softwareUpdateAvailability: Flow<SoftwareUpdateAvailability>
    val batteryStatus: Flow<CardReaderBatteryStatus>
    val displayBluetoothCardReaderMessages: Flow<BluetoothCardReaderMessages>

    fun initialize(
        updateFrequency: SimulatorUpdateFrequency,
        useInterac: Boolean,
        isDebug: Boolean,
    )

    fun reinitializeSimulatedTerminal(updateFrequency: SimulatorUpdateFrequency, useInterac: Boolean)

    fun discoverReaders(
        isSimulated: Boolean,
        cardReaderTypesToDiscover: CardReaderTypesToDiscover,
    ): Flow<CardReaderDiscoveryEvents>

    fun startConnectionToReader(cardReader: CardReader, locationId: String)
    suspend fun disconnectReader(): Boolean

    suspend fun collectPayment(paymentInfo: PaymentInfo): Flow<CardPaymentStatus>
    suspend fun refundInteracPayment(
        refundParams: RefundParams,
        refundConfig: RefundConfig
    ): Flow<CardInteracRefundStatus>

    suspend fun retryCollectPayment(orderId: Long, paymentData: PaymentData): Flow<CardPaymentStatus>
    fun cancelPayment(paymentData: PaymentData)

    suspend fun startAsyncSoftwareUpdate()
    suspend fun clearCachedCredentials()
    fun cancelOngoingFirmwareUpdate()

    enum class SimulatorUpdateFrequency {
        NEVER,
        ALWAYS,
        LOW_BATTERY_ERROR,
        LOW_BATTERY_SUCCEED_CONNECT,
        RANDOM
    }
}
