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
import kotlinx.parcelize.Parcelize
import javax.inject.Inject
import kotlinx.coroutines.flow.update

@HiltViewModel
class WooShippingLabelPackageCreationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val resourceProvider: ResourceProvider
) : ScopedViewModel(savedState) {

    private val _viewState = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = ViewState(pageTabs, CustomPackageCreationData.EMPTY)
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

    fun onAddPackageClick() {
        triggerEvent(PackageSelected)
    }

    fun onPackageTypeSpinnerClick() {

    }

    fun onLengthChange(length: String) {
    }

    fun onWidthChange(width: String) {

    }

    fun onHeightChange(height: String) {

    }

    fun onSavePackageChanged(checked: Boolean) {

    }

    @Parcelize
    data class ViewState(
        val pageTabs: List<PageTab> = emptyList(),
        val customPackageCreationData: CustomPackageCreationData
    ) : Parcelable

    @Parcelize
    data class PageTab(
        val title: String,
        val type: PageType
    ) : Parcelable

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

    data object PackageSelected : MultiLiveEvent.Event()
}
