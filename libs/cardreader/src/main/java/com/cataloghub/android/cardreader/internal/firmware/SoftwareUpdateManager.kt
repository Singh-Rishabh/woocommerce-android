package com.cataloghub.android.cardreader.internal.firmware

import com.stripe.stripeterminal.external.callable.Callback
import com.stripe.stripeterminal.external.models.TerminalException
import com.cataloghub.android.cardreader.LogWrapper
import com.cataloghub.android.cardreader.connection.event.SoftwareUpdateStatus
import com.cataloghub.android.cardreader.internal.LOG_TAG
import com.cataloghub.android.cardreader.internal.connection.BluetoothReaderListenerImpl
import com.cataloghub.android.cardreader.internal.wrappers.TerminalWrapper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

internal class SoftwareUpdateManager(
    private val terminalWrapper: TerminalWrapper,
    private val bluetoothReaderListenerImpl: BluetoothReaderListenerImpl,
    private val logWrapper: LogWrapper,
) {
    suspend fun startAsyncSoftwareUpdate() = coroutineScope {
        val deferred = CompletableDeferred<Unit>()
        val job = launch {
            try {
                withTimeout(timeMillis = UPDATE_STARTED_TIMEOUT_MS) {
                    bluetoothReaderListenerImpl.updateStatusEvents.collect { status ->
                        if (status !is SoftwareUpdateStatus.Unknown) {
                            deferred.complete(Unit)
                        }
                    }
                }
            } catch (e: TimeoutCancellationException) {
                logWrapper.e(LOG_TAG, "update haven't started after $UPDATE_STARTED_TIMEOUT_MS ms")
                deferred.complete(Unit)
            }
        }
        terminalWrapper.installSoftwareUpdate()
        deferred.await()
        job.cancel()
    }

    fun cancelOngoingFirmwareUpdate() {
        bluetoothReaderListenerImpl.cancelUpdateAction?.cancel(object : Callback {
            override fun onFailure(e: TerminalException) {
                logWrapper.w(LOG_TAG, "Update cancellation failed ${e.message.orEmpty()}")
            }

            override fun onSuccess() {
                logWrapper.d(LOG_TAG, "Update cancellation succeeded")
            }
        }) ?: logWrapper.e(LOG_TAG, "Attempt to cancel on null cancellable")
        bluetoothReaderListenerImpl.cancelUpdateAction = null
    }

    companion object {
        private const val UPDATE_STARTED_TIMEOUT_MS = 30_000L
    }
}
