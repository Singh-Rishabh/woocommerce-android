package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ResourceProvider
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.update
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelPackageCreationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val resourceProvider: ResourceProvider,
    private val fetchSavedPackages: FetchSavedPackagesFromStore
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
        _viewState.update { viewState ->
            viewState.copy(savedPackageSelection = SavedPackageSelection(fetchSavedPackages()))
        }
    }

    fun onSavedPackageSelected(packageData: PackageData, isSelected: Boolean) {
        _viewState.update { viewState ->
            viewState.savedPackageSelection.packages
                .filter { it != packageData }
                .map { it.copy(isSelected = false) }
                .let { SavedPackageSelection(it + packageData.copy(isSelected = isSelected)) }
                .let { viewState.copy(savedPackageSelection = it) }
        }
    }

    fun onAddSavedPackageClick() {
        _viewState.value.savedPackageSelection.packages.find { it.isSelected }
            ?.let { triggerEvent(SavedPackageSelected(it)) }
    }

    fun onAddCustomPackageClick() {
        triggerEvent(CustomPackageCreated(_viewState.value.customPackageCreationData))
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

    @Parcelize
    data class ViewState(
        val pageTabs: List<PageTab> = emptyList(),
        val customPackageCreationData: CustomPackageCreationData = CustomPackageCreationData.EMPTY,
        val savedPackageSelection: SavedPackageSelection = SavedPackageSelection(emptyList())
    ) : Parcelable

    @Parcelize
    data class PageTab(
        val title: String,
        val type: PageType
    ) : Parcelable

    @Parcelize
    data class PackageData(
        val type: PackageType,
        val name: String,
        val description: String,
        val length: String,
        val width: String,
        val height: String,
        val isSelected: Boolean
    ) : Parcelable {
        val dimensionsForDisplay: String
            get() = "$length x $width x $height cm"
    }

    @Parcelize
    data class SavedPackageSelection(
        val packages: List<PackageData>
    ) : Parcelable {
        val hasSelection: Boolean
            get() = packages.find { it.isSelected } != null
    }

    @Parcelize
    data class CustomPackageCreationData(
        val type: PackageType,
        val length: String,
        val width: String,
        val height: String,
        val saveAsTemplate: Boolean
    ) : Parcelable {
        val isValid: Boolean
            get() = height.isNotEmpty() && length.isNotEmpty() && width.isNotEmpty()

        companion object {
            val EMPTY = CustomPackageCreationData(
                type = PackageType.BOX,
                length = "",
                width = "",
                height = "",
                saveAsTemplate = false
            )
        }
    }

    enum class PageType {
        CUSTOM,
        CARRIER,
        SAVED
    }

    enum class PackageType(val resourceId: Int) {
        BOX(R.string.woo_shipping_labels_package_creation_box_type),
        ENVELOPE(R.string.woo_shipping_labels_package_creation_envelope_type)
    }

    data class SavedPackageSelected(val packageData: PackageData) : MultiLiveEvent.Event()
    data class CustomPackageCreated(val packageData: CustomPackageCreationData) : MultiLiveEvent.Event()
    data class ShowPackageTypeDialog(val currentSelection: PackageType) : MultiLiveEvent.Event()
}
