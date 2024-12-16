package com.woocommerce.android.ui.sitepicker.storevisibility

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.woocommerce.android.ui.sitepicker.SitePickerRepository
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreVisibilityViewModel @Inject constructor(
    private val sitePickerRepository: SitePickerRepository,
    savedStateHandle: SavedStateHandle
) : ScopedViewModel(savedStateHandle) {

    private val _wooStores = MutableStateFlow(emptyList<WooStoreUi>())
    val wooStoresState = _wooStores.asLiveData()

    init {
        launch {
            _wooStores.update {
                sitePickerRepository.getSites()
                    .filter { it.hasWooCommerce }
                    .map {
                        WooStoreUi(
                            siteName = it.name,
                            siteUrl = it.url,
                            siteId = it.siteId,
                            isVisible = it.isVisible
                        )
                    }
            }
        }
    }

    data class WooStoreUi(
        val siteName: String,
        val siteUrl: String,
        val siteId: Long,
        val isVisible: Boolean
    )
}
