package com.cataloghub.android.ui.orders.shippinglabels.creation

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.model.ShippingPackage
import com.cataloghub.android.ui.orders.shippinglabels.ShippingLabelRepository
import com.cataloghub.android.ui.products.ParameterRepository
import com.cataloghub.android.viewmodel.LiveDataDelegate
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class ShippingPackageSelectorViewModel @Inject constructor(
    savedState: SavedStateHandle,
    parameterRepository: ParameterRepository,
    private val shippingLabelRepository: ShippingLabelRepository
) : ScopedViewModel(savedState) {
    companion object {
        private const val KEY_PARAMETERS = "key_parameters"
    }

    private val arguments: ShippingPackageSelectorFragmentArgs by savedState.navArgs()

    /**
     * Saving more data than necessary into the SavedState has associated risks which were not known at the time this
     * field was implemented - after we ensure we don't save unnecessary data, we can replace @Suppress("OPT_IN_USAGE")
     * with @OptIn(LiveDelegateSavedStateAPI::class).
     */
    @Suppress("OPT_IN_USAGE")
    val viewStateData = LiveDataDelegate(savedState, ViewState())
    private var viewState by viewStateData

    val dimensionUnit: String by lazy {
        parameterRepository.getParameters(KEY_PARAMETERS, savedState).dimensionUnit ?: ""
    }

    init {
        launch {
            loadPackages()
        }
    }

    private fun loadPackages() {
        launch {
            viewState = viewState.copy(isLoading = true)
            val packagesListResult = shippingLabelRepository.getShippingPackages()
            if (packagesListResult.isError) {
                triggerEvent(ShowSnackbar(R.string.shipping_label_packages_loading_error))
                triggerEvent(Exit)
                return@launch
            }
            viewState = viewState.copy(isLoading = false, packagesList = packagesListResult.model!!)
        }
    }

    fun onPackageSelected(shippingPackage: ShippingPackage) {
        triggerEvent(
            ExitWithResult(
                ShippingPackageSelectorResult(
                    position = arguments.position,
                    selectedPackage = shippingPackage
                )
            )
        )
    }

    fun onCreateNewPackageButtonClicked() {
        AnalyticsTracker.track(AnalyticsEvent.SHIPPING_LABEL_ADD_PACKAGE_TAPPED)
        triggerEvent(ShowCreatePackageScreen(arguments.position))
    }

    @Parcelize
    data class ViewState(
        val packagesList: List<ShippingPackage> = emptyList(),
        val isLoading: Boolean = false
    ) : Parcelable

    data class ShowCreatePackageScreen(val position: Int) : Event()
}

@Parcelize
data class ShippingPackageSelectorResult(
    val position: Int,
    val selectedPackage: ShippingPackage
) : Parcelable
