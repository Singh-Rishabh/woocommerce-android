package com.cataloghub.android.ui.payments.cardreader.statuschecker

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.cardreader.CardReaderManager
import com.cataloghub.android.cardreader.connection.CardReader
import com.cataloghub.android.cardreader.connection.CardReaderStatus.Connected
import com.cataloghub.android.cardreader.connection.ReaderType
import com.cataloghub.android.di.PointOfSaleMode
import com.cataloghub.android.di.StoreManagementMode
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderFlowParam
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderOnboardingChecker
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderOnboardingParams
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderOnboardingState
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderType
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderType.BUILT_IN
import com.cataloghub.android.ui.payments.cardreader.onboarding.CardReaderType.EXTERNAL
import com.cataloghub.android.ui.payments.cardreader.statuschecker.CardReaderStatusCheckerViewModel.StatusCheckerEvent.NavigateToConnection
import com.cataloghub.android.ui.payments.tracking.PaymentsFlowTracker
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.SingleLiveEvent
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardReaderStatusCheckerViewModel
@Inject constructor(
    savedState: SavedStateHandle,
    @StoreManagementMode storeManagementModePaymentsFlowTracker: PaymentsFlowTracker,
    @PointOfSaleMode pointOfSaleModePaymentsFlowTracker: PaymentsFlowTracker,
    private val cardReaderManager: CardReaderManager,
    private val cardReaderChecker: CardReaderOnboardingChecker,
    private val appPrefsWrapper: AppPrefsWrapper,
) : ScopedViewModel(savedState) {
    private val arguments: CardReaderStatusCheckerDialogFragmentArgs by savedState.navArgs()
    private val paymentsFlowTracker: PaymentsFlowTracker

    override val _event = SingleLiveEvent<MultiLiveEvent.Event>()
    override val event: LiveData<MultiLiveEvent.Event> = _event

    init {
        paymentsFlowTracker = when (arguments.cardReaderFlowParam) {
            is CardReaderFlowParam.WooPosConnection -> pointOfSaleModePaymentsFlowTracker
            else -> storeManagementModePaymentsFlowTracker
        }
        launch {
            checkStatus()
        }
    }

    private suspend fun checkStatus() {
        when (val param = arguments.cardReaderFlowParam) {
            is CardReaderFlowParam.CardReadersHub -> triggerEvent(
                StatusCheckerEvent.NavigateToOnboarding(
                    CardReaderOnboardingParams.Check(param),
                    arguments.cardReaderType
                )
            )
            is CardReaderFlowParam.PaymentOrRefund -> {
                val cardReaderStatus = cardReaderManager.readerStatus.value
                if (cardReaderStatus is Connected) {
                    if (cardReaderStatus.cardReader.toCardReaderType() != arguments.cardReaderType) {
                        handleNotSelectedReaderTypeConnected(param)
                    } else {
                        triggerEvent(
                            StatusCheckerEvent.NavigateToPayment(
                                param,
                                cardReaderStatus.cardReader.toCardReaderType()
                            )
                        )
                    }
                } else {
                    handleOnboardingStatus(param)
                }
            }
            is CardReaderFlowParam.WooPosConnection -> handleOnboardingStatus(param)
        }
    }

    private suspend fun handleNotSelectedReaderTypeConnected(param: CardReaderFlowParam) {
        paymentsFlowTracker.trackAutomaticReadDisconnectWhenConnectedAnotherType()
        cardReaderManager.disconnectReader()
        handleOnboardingStatus(param)
    }

    private suspend fun handleOnboardingStatus(param: CardReaderFlowParam) {
        when (val state = cardReaderChecker.getOnboardingState()) {
            is CardReaderOnboardingState.OnboardingCompleted -> {
                if (appPrefsWrapper.isCardReaderWelcomeDialogShown()) {
                    triggerEvent(NavigateToConnection(param, arguments.cardReaderType))
                } else {
                    triggerEvent(StatusCheckerEvent.NavigateToWelcome(param, arguments.cardReaderType))
                }
            }

            else -> triggerEvent(
                StatusCheckerEvent.NavigateToOnboarding(
                    CardReaderOnboardingParams.Failed(param, state),
                    arguments.cardReaderType
                )
            )
        }
    }

    private fun CardReader.toCardReaderType() =
        if (ReaderType.isExternalReaderType(type)) {
            EXTERNAL
        } else if (ReaderType.isBuiltInReaderType(type)) {
            BUILT_IN
        } else {
            error("Unknown reader type: $type")
        }

    sealed class StatusCheckerEvent : MultiLiveEvent.Event() {
        data class NavigateToWelcome(
            val cardReaderFlowParam: CardReaderFlowParam,
            val cardReaderType: CardReaderType,
        ) : MultiLiveEvent.Event()

        data class NavigateToConnection(
            val cardReaderFlowParam: CardReaderFlowParam,
            val cardReaderType: CardReaderType,
        ) : MultiLiveEvent.Event()

        data class NavigateToPayment(
            val cardReaderFlowParam: CardReaderFlowParam,
            val cardReaderType: CardReaderType,
        ) : MultiLiveEvent.Event()

        data class NavigateToOnboarding(
            val cardReaderOnboardingParams: CardReaderOnboardingParams,
            val cardReaderType: CardReaderType,
        ) : MultiLiveEvent.Event()
    }
}
