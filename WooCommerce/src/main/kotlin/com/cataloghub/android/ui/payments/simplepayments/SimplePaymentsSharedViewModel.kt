package com.cataloghub.android.ui.payments.simplepayments

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.tracker.OrderDurationRecorder
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.wordpress.android.fluxc.store.WooCommerceStore
import javax.inject.Inject

@HiltViewModel
class SimplePaymentsSharedViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val selectedSite: SelectedSite,
    private val wooCommerceStore: WooCommerceStore,
) : ScopedViewModel(savedState) {
    val currencyCode: String
        get() = wooCommerceStore.getSiteSettings(selectedSite.get())?.currencyCode ?: ""

    init {
        // Reset order duration recorder to ensure we don't track Simple Payments flow
        OrderDurationRecorder.reset()
    }
}
