package com.woocommerce.android.ui.orders.shippinglabels.creation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.analytics.AnalyticsEvent
import com.woocommerce.android.analytics.AnalyticsTracker
import com.woocommerce.android.model.ShippingPackage
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.woocommerce.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.woocommerce.android.viewmodel.ScopedViewModel
import com.woocommerce.android.viewmodel.getStateFlow
import com.woocommerce.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.update

@HiltViewModel
class ShippingLabelCreatePackageViewModel @Inject constructor(
    savedState: SavedStateHandle
) : ScopedViewModel(savedState) {
    private val arguments: ShippingLabelCreatePackageFragmentArgs by savedState.navArgs()
    private val selectedTabType = savedState.getStateFlow(
        scope = viewModelScope,
        initialValue = PackageType.CUSTOM
    )

    fun onPackageCreated(madePackage: ShippingPackage) {
        val type = if (madePackage.category == ShippingPackage.CUSTOM_PACKAGE_CATEGORY) "custom" else "predefined"
        AnalyticsTracker.track(
            stat = AnalyticsEvent.SHIPPING_LABEL_PACKAGE_ADDED_SUCCESSFULLY,
            properties = mapOf("type" to type)
        )

        triggerEvent(
            ShowSnackbar(
                message = R.string.shipping_label_create_custom_package_success_message,
                args = arrayOf(madePackage.title)
            )
        )

        triggerEvent(
            ExitWithResult(
                ShippingPackageSelectorResult(
                    position = arguments.position,
                    selectedPackage = madePackage
                )
            )
        )
    }

    fun onSelectedPageChanged(selectedTab: PackageType) {
        selectedTabType.update { selectedTab }
    }

    fun onDoneButtonClicked() {
        triggerEvent(OnDoneButtonClicked(selectedTab = selectedTabType.value))
    }

    enum class PackageType {
        CUSTOM,
        SERVICE;

        companion object {
            fun fromOrdinal(ordinal: Int) = entries.first { it.ordinal == ordinal }
        }
    }

    data class OnDoneButtonClicked(
        val selectedTab: PackageType
    ) : MultiLiveEvent.Event()
}
