package com.woocommerce.android.ui.orders.wooshippinglabels.hazmat

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize

@HiltViewModel
class WooShippingLabelHazmatFormViewModel(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {

    @Parcelize
    data class ViewState(
        val containsHazmatChecked: Boolean = false
    ) : Parcelable
}
