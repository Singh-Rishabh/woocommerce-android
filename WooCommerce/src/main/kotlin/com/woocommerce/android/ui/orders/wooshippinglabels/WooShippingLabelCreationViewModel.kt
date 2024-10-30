package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel
class WooShippingLabelCreationViewModel(
    savedState: SavedStateHandle
): ScopedViewModel(savedState) {
    fun onSelectPackageClicked() { triggerEvent(StartPackageSelection) }

    data object StartPackageSelection: Event()
}
