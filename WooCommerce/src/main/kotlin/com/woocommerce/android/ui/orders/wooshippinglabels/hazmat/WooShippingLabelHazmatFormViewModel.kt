package com.woocommerce.android.ui.orders.wooshippinglabels.hazmat

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.ui.orders.shippinglabels.creation.ShippingLabelHazmatCategory
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
        val currentCategory = _viewState.value.currentHazmatSelection
        triggerEvent(OnSelectCategoryClicked(currentCategory))
    }

    fun onHazmatCategorySelected(selectedCategory: ShippingLabelHazmatCategory) {
        _viewState.update {
            _viewState.value.copy(currentHazmatSelection = selectedCategory)
        }
    }

    fun onUrlSelected(url: String) {
        triggerEvent(OnUrlSelected(url))
    }

    @Parcelize
    data class ViewState(
        val containsHazmatChecked: Boolean = false,
        val currentHazmatSelection: ShippingLabelHazmatCategory? = null
    ) : Parcelable

    data class OnSelectCategoryClicked(
        val currentSelection: ShippingLabelHazmatCategory? = null
    ) : Event()

    data class OnUrlSelected(val url: String) : Event()

    companion object {
        const val KEY_HAZMAT_CATEGORY_SELECTOR_RESULT = "hazmat_category_selector_result"
    }
}
