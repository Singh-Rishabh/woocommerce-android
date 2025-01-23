package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class WooShippingCustomsFormViewModel @Inject constructor(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {

    private val _viewState = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = ViewState()
    )
    val viewState = _viewState.asLiveData()

    fun onContentTypeClick() {
        triggerEvent(ContentTypeClicked)
    }

    fun onRestrictionTypeClick() {
        triggerEvent(RestrictionTypeClicked)
    }

    @Parcelize
    data class ViewState(
        val contentType: String = "",
        val restrictionType: String = "",
        val itnValue: String = "",
        val returnToSenderChecked: Boolean = false,
        val isAddCustomsButtonEnabled: Boolean = false
    ) : Parcelable

    object ContentTypeClicked: MultiLiveEvent.Event()
    object RestrictionTypeClicked: MultiLiveEvent.Event()
}
