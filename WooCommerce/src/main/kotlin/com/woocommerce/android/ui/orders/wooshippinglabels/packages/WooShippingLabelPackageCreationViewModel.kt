package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.viewmodel.ResourceProvider
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class WooShippingLabelPackageCreationViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val resourceProvider: ResourceProvider
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

    fun onAddPackageClick() {

    }

    @Parcelize
    data class ViewState(
        val pageTabs: List<PageTab> = emptyList(),
        val customPackageCreationData: CustomPackageCreationData? = null
    ) : Parcelable

    @Parcelize
    data class PageTab(
        val title: String,
        val type: PageType
    ) : Parcelable

    @Parcelize
    data class CustomPackageCreationData(
        val type: String,
        val weight: String,
        val length: String,
        val width: String,
        val saveAsTemplate: Boolean
    ) : Parcelable {
        val isValid: Boolean
            get() = weight.isNotEmpty() && length.isNotEmpty() && width.isNotEmpty()
    }

    enum class PageType {
        CUSTOM,
        CARRIER,
        SAVED
    }
}
