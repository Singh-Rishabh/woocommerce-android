package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WooShippingCustomsFormViewModel @Inject constructor(
    savedState: SavedStateHandle
): ScopedViewModel(savedState) {
}
