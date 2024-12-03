package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.FetchPredefinedPackagesFromStore
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageSelection
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CustomPackageCreationData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.SavedPackageSelection
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ResourceProvider
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelPackageCreationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val resourceProvider: ResourceProvider,
    private val fetchPredefinedPackages: FetchPredefinedPackagesFromStore
) : ScopedViewModel(savedState) {

    private val _viewState = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = ViewState(pageTabs)
    )
    val viewState = _viewState.asLiveData()

    private val pageTabs
        get() = listOf(
            PageTab(
                type = PageType.CUSTOM,
                title = resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_custom)
            ),
            PageTab(
                type = PageType.CARRIER,
                title = resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_carrier)
            ),
            PageTab(
                type = PageType.SAVED,
                title = resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_saved)
            )
        )

    init {
        launch {
            fetchPredefinedPackages()?.let {
                _viewState.update { viewState ->
                    viewState.copy(
                        savedPackageSelection = it.savedPackageSelection,
                        carrierPackageSection = it.carrierPackageSelection
                    )
                }
            }
        }
    }

    fun onCarrierPackageSelected(selectedPackage: PackageData, isSelected: Boolean) {
        _viewState.update { viewState ->
            viewState.carrierPackageSection.carrierPackages
                .map { updateCarrierPackagesSelection(it, selectedPackage, isSelected) }
                .let { viewState.copy(carrierPackageSection = CarrierPackageSelection(it.toMap())) }
        }
    }

    fun onSavedPackageSelected(selectedPackage: PackageData, isSelected: Boolean) {
        _viewState.update { viewState ->
            viewState.savedPackageSelection.packages
                .map { it.copy(isSelected = false) }
                .toMutableList()
                .safelyUpdate(selectedPackage, selectedPackage.copy(isSelected = isSelected))
                .let { SavedPackageSelection(it) }
                .let { viewState.copy(savedPackageSelection = it) }
        }
    }

    fun onAddCarrierPackageClick() {
        _viewState.value.carrierPackageSection.carrierPackages
            .asSequence()
            .flatMap { it.value }
            .flatMap { it.packages }
            .find { it.isSelected }
            ?.let { triggerEvent(PackageSelected(it)) }
    }

    fun onAddSavedPackageClick() {
        _viewState.value.savedPackageSelection.packages.find { it.isSelected }
            ?.let { triggerEvent(PackageSelected(it)) }
    }

    fun onAddCustomPackageClick() {
        _viewState.value.customPackageCreationData
            .toPackageData()
            .let { triggerEvent(PackageSelected(it)) }
    }

    fun onPackageTypeSpinnerClick() {
        triggerEvent(ShowPackageTypeDialog(_viewState.value.customPackageCreationData.type))
    }

    fun onPackageTypeSelected(type: PackageType) {
        _viewState.update {
            val newPackageData = it.customPackageCreationData.copy(type = type)
            it.copy(customPackageCreationData = newPackageData)
        }
    }

    fun onLengthChange(length: String) {
        _viewState.update {
            val newPackageData = it.customPackageCreationData.copy(length = length)
            it.copy(customPackageCreationData = newPackageData)
        }
    }

    fun onWidthChange(width: String) {
        _viewState.update {
            val newPackageData = it.customPackageCreationData.copy(width = width)
            it.copy(customPackageCreationData = newPackageData)
        }
    }

    fun onHeightChange(height: String) {
        _viewState.update {
            val newPackageData = it.customPackageCreationData.copy(height = height)
            it.copy(customPackageCreationData = newPackageData)
        }
    }

    fun onSavePackageChanged(checked: Boolean) {
        _viewState.update {
            val newPackageData = it.customPackageCreationData.copy(saveAsTemplate = checked)
            it.copy(customPackageCreationData = newPackageData)
        }
    }

    private fun updateCarrierPackagesSelection(
        carrierPackages: Map.Entry<Carrier, List<CarrierPackageGroup>>,
        packageData: PackageData,
        isSelected: Boolean
    ) = carrierPackages.value.map { packageGroup ->
        packageGroup.packages
            .map { it.copy(isSelected = false) }
            .toMutableList()
            .safelyUpdate(packageData, packageData.copy(isSelected = isSelected))
            .let { packageGroup.copy(packages = it) }
    }.let { carrierPackages.key to it }

    private fun MutableList<PackageData>.safelyUpdate(
        originalPackage: PackageData,
        updatedPackage: PackageData
    ) = apply {
        indexOf(originalPackage)
            .takeIf { it != -1 }
            ?.let { set(it, updatedPackage) }
    }

    @Parcelize
    data class ViewState(
        val pageTabs: List<PageTab> = emptyList(),
        val customPackageCreationData: CustomPackageCreationData = CustomPackageCreationData.EMPTY,
        val savedPackageSelection: SavedPackageSelection = SavedPackageSelection(emptyList()),
        val carrierPackageSection: CarrierPackageSelection = CarrierPackageSelection(emptyMap())
    ) : Parcelable

    @Parcelize
    data class PageTab(
        val title: String,
        val type: PageType
    ) : Parcelable

    enum class PageType {
        CUSTOM,
        CARRIER,
        SAVED
    }

    enum class PackageType(val resourceId: Int) {
        BOX(R.string.woo_shipping_labels_package_creation_box_type),
        ENVELOPE(R.string.woo_shipping_labels_package_creation_envelope_type)
    }

    data class PackageSelected(val packageData: PackageData) : MultiLiveEvent.Event()
    data class ShowPackageTypeDialog(val currentSelection: PackageType) : MultiLiveEvent.Event()
}
