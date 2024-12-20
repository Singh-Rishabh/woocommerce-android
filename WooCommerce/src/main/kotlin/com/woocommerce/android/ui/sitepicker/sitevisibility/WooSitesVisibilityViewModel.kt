package com.woocommerce.android.ui.sitepicker.sitevisibility

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.sitepicker.SitePickerRepository
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event.Exit
import com.woocommerce.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.model.SiteModel
import javax.inject.Inject

@HiltViewModel
class WooSitesVisibilityViewModel @Inject constructor(
    private val sitePickerRepository: SitePickerRepository,
    private val selectedSite: SelectedSite,
    private val hiddenSitesDataStore: HiddenWooSitesDataStore,
    savedStateHandle: SavedStateHandle
) : ScopedViewModel(savedStateHandle) {
    private var initiallySelectedSiteIds: List<Long> = emptyList()
    private val _wooStores = MutableStateFlow(
        WooStoresUiState(
            wooStores = emptyList(),
            currentSite = selectedSite.get().toWooStoreUi(true),
            isSaveButtonEnabled = false
        )
    )
    val viewState = _wooStores.asLiveData()

    init {
        launch {
            _wooStores.value = _wooStores.value.copy(
                wooStores = sitePickerRepository.getSites()
                    .filter { it.hasWooCommerce && it.siteId != selectedSite.get().siteId }
                    .map {
                        val isSelected = getSiteVisibility(it.siteId)
                        it.toWooStoreUi(isSelected)
                    }
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
        launch {
            hiddenSitesDataStore.updateHiddenSites(
                _wooStores.value.wooStores
                    .associate { it.siteId.toString() to it.isSelected }
            )
            triggerEvent(Exit)
        }
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

    private suspend fun getSiteVisibility(siteId: Long): Boolean =
        hiddenSitesDataStore.isSiteHidden(siteId.toString()).first()

    private fun SiteModel.toWooStoreUi(isSelected: Boolean) = WooStoreUi(
        siteName = name,
        siteUrl = url,
        siteId = siteId,
        isSelected = isSelected
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
