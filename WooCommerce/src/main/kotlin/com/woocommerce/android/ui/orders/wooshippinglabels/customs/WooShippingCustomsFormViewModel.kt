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
import kotlinx.coroutines.flow.update

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
        val currentSelection = _viewState.value.contentType
        triggerEvent(ShowContentTypeDialog(currentSelection))
    }

    fun onRestrictionTypeClick() {
        val currentSelection = _viewState.value.restrictionType
        triggerEvent(ShowRestrictionTypeDialog(currentSelection))
    }

    fun onITNChanged(newItnValue: String) {
        _viewState.update {
            it.copy(itnValue = newItnValue)
        }
    }

    fun onReturnToSenderChanged(isChecked: Boolean) {
        _viewState.update {
            it.copy(returnToSenderChecked = isChecked)
        }
    }

    fun onContentTypeSelected(contentType: ContentType) {
        _viewState.update {
            it.copy(contentType = contentType)
        }
    }

    fun onRestrictionTypeSelected(restrictionType: RestrictionType) {
        _viewState.update {
            it.copy(restrictionType = restrictionType)
        }
    }

    @Parcelize
    data class ViewState(
        val contentType: ContentType = ContentType.MERCHANDISE,
        val restrictionType: RestrictionType = RestrictionType.NONE,
        val itnValue: String = "",
        val returnToSenderChecked: Boolean = false,
        val isAddCustomsButtonEnabled: Boolean = false
    ) : Parcelable

    enum class ContentType {
        MERCHANDISE,
        GIFT,
        RETURNED_GOODS,
        SAMPLE,
        DOCUMENTS,
        OTHER
    }

    enum class RestrictionType {
        NONE,
        QUARANTINE,
        SANITARY_INSPECTION,
        OTHER
    }

    data class ShowContentTypeDialog(val currentSelection: ContentType): MultiLiveEvent.Event()
    data class ShowRestrictionTypeDialog(val currentSelection: RestrictionType): MultiLiveEvent.Event()
}
