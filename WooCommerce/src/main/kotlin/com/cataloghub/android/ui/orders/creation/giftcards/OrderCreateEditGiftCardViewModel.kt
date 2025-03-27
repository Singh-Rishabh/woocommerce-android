package com.cataloghub.android.ui.orders.creation.giftcards

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.getStateFlow
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class OrderCreateEditGiftCardViewModel @Inject constructor(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    private val navArgs: OrderCreateEditGiftCardFragmentArgs by savedState.navArgs()

    private val codeFormatRegex by lazy {
        "^[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}-[a-zA-Z0-9]{4}$".toRegex()
    }

    private val selectedGiftCard = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = navArgs.giftCard.orEmpty()
    )

    val viewState = selectedGiftCard
        .map {
            val isValidCode = it.isEmpty() || it.matches(codeFormatRegex)
            ViewState(giftCard = it, isValidCode = isValidCode)
        }.asLiveData()

    fun onGiftCardChanged(giftCard: String) {
        selectedGiftCard.value = giftCard
    }

    fun onDoneButtonClicked() {
        triggerEvent(ExitWithResult(GiftCardResult(selectedGiftCard.value)))
    }

    data class ViewState(
        val giftCard: String,
        val isValidCode: Boolean
    )

    @Parcelize
    data class GiftCardResult(val selectedGiftCard: String) : Parcelable
}
