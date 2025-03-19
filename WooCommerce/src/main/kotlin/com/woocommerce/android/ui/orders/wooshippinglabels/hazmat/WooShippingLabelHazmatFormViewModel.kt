package com.woocommerce.android.ui.orders.wooshippinglabels.hazmat

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelHazmatFormViewModel @Inject constructor(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {

    private val _viewState = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = ViewState()
    )
    val viewState = _viewState.asLiveData()

    fun onContainsHazmatChanged(containsHazmatChecked: Boolean) {
        _viewState.update {
            _viewState.value.copy(containsHazmatChecked = containsHazmatChecked)
        }
    }

    fun onSelectCategoryClick() {
        triggerEvent(OnSelectCategoryClicked)
    }

    fun onUrlSelected(url: String) {
        triggerEvent(OnUrlSelected(url))
    }

    @Parcelize
    data class ViewState(
        val containsHazmatChecked: Boolean = false
    ) : Parcelable

    data object OnSelectCategoryClicked : Event()
    data class OnUrlSelected(val url: String) : Event()
}
