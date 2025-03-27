package com.cataloghub.android.ui.payments.cardreader.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.R
import com.cataloghub.android.model.UiString.UiStringRes
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CardReaderWelcomeViewModel @Inject constructor(
    savedState: SavedStateHandle,
    appPrefsWrapper: AppPrefsWrapper,
) : ScopedViewModel(savedState) {
    private val arguments: CardReaderWelcomeDialogFragmentArgs by savedState.navArgs()

    private val _viewState = MutableLiveData(ViewState(::onButtonClick))
    val viewState: LiveData<ViewState> = _viewState

    init {
        appPrefsWrapper.setCardReaderWelcomeDialogShown()
    }

    private fun onButtonClick() {
        triggerEvent(
            CardReaderWelcomeDialogEvent.NavigateToOnboardingFlow(
                arguments.cardReaderFlowParam,
                arguments.cardReaderType
            )
        )
    }

    sealed class CardReaderWelcomeDialogEvent : Event() {
        data class NavigateToOnboardingFlow(
            val cardReaderFlowParam: CardReaderFlowParam,
            val cardReaderType: CardReaderType
        ) : Event()
    }

    data class ViewState(val buttonAction: () -> Unit) {
        val header = UiStringRes(R.string.card_reader_welcome_dialog_header)
        val img: Int = R.drawable.img_woman_payment_card
        val text = UiStringRes(R.string.card_reader_welcome_dialog_text)
        val buttonLabel = UiStringRes(R.string.continue_button)
    }
}
