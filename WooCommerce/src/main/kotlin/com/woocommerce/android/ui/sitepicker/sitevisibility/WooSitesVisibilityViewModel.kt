package com.woocommerce.android.ui.sitepicker.sitevisibility

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.sitepicker.SitePickerRepository
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event.Exit
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.model.SiteModel
import javax.inject.Inject

@HiltViewModel
class WooSitesVisibilityViewModel @Inject constructor(
    private val sitePickerRepository: SitePickerRepository,
    private val selectedSite: SelectedSite,
    savedStateHandle: SavedStateHandle
) : ScopedViewModel(savedStateHandle) {
    private var initiallySelectedSiteIds: List<Long> = emptyList()
    private val _wooStores = MutableStateFlow(
        WooStoresUiState(
            wooStores = emptyList(),
            currentSite = selectedSite.get().toWooStoreUi(),
            isSaveButtonEnabled = false
        )
    )
    val viewState = _wooStores.asLiveData()

    init {
        launch {
            _wooStores.value = _wooStores.value.copy(
                wooStores = sitePickerRepository.getSites()
                    .filter { it.hasWooCommerce && it.siteId != selectedSite.get().siteId }
                    .map { it.toWooStoreUi() }
                +
                sitePickerRepository.getSites()
                    .filter { it.hasWooCommerce && it.siteId != selectedSite.get().siteId }
                    .map { it.toWooStoreUi() }
            )
            initiallySelectedSiteIds = _wooStores.value.wooStores
                .filter { it.isSelected }
                .map { it.siteId }
        }
    }

    fun onBackPressed() {
        triggerEvent(Exit)
    }

    fun onSaveTapped() {
        TODO("Not yet implemented")
    }

    fun onSiteSelected(wooStoreUi: WooStoreUi) {
        _wooStores.value = _wooStores.value.copy(
            wooStores = _wooStores.value.wooStores.map {
                when {
                    it.siteId == wooStoreUi.siteId -> it.copy(isSelected = !it.isSelected)
                    else -> it
                }
            }
        )
        _wooStores.value = _wooStores.value.copy(
            isSaveButtonEnabled = _wooStores.value.wooStores
                .filter { it.isSelected }
                .map { it.siteId } != initiallySelectedSiteIds
        )
    }

    private fun SiteModel.toWooStoreUi() = WooStoreUi(
        siteName = name,
        siteUrl = url,
        siteId = siteId,
        isSelected = true // TODO remove hardcoded value
    )

    data class WooStoresUiState(
        val wooStores: List<WooStoreUi>,
        val currentSite: WooStoreUi,
        val isSaveButtonEnabled: Boolean
    )

    data class WooStoreUi(
        val siteName: String,
        val siteUrl: String,
        val siteId: Long,
        val isSelected: Boolean
    )
}
