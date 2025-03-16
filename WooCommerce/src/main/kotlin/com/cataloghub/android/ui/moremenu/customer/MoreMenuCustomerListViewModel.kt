package com.cataloghub.android.ui.moremenu.customer

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.model.CustomerWithAnalytics
import com.cataloghub.android.model.toCustomerWithAnalytics
import com.cataloghub.android.ui.customer.CustomerListViewModel
import com.cataloghub.android.ui.orders.creation.customerlist.CustomerListGetSupportedSearchModes
import com.cataloghub.android.ui.orders.creation.customerlist.CustomerListIsAdvancedSearchSupported
import com.cataloghub.android.ui.orders.creation.customerlist.CustomerListRepository
import com.cataloghub.android.ui.orders.creation.customerlist.CustomerListViewModelMapper
import com.cataloghub.android.util.StringUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import org.wordpress.android.fluxc.model.customer.WCCustomerModel
import javax.inject.Inject

@HiltViewModel
class CustomerListDetailsViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val repository: CustomerListRepository,
    private val mapper: CustomerListViewModelMapper,
    isAdvancedSearchSupported: CustomerListIsAdvancedSearchSupported,
    getSupportedSearchModes: CustomerListGetSupportedSearchModes,
    analyticsTracker: AnalyticsTrackerWrapper,
    stringUtils: StringUtils,
) : CustomerListViewModel(
    savedState,
    repository,
    mapper,
    isAdvancedSearchSupported,
    getSupportedSearchModes,
    analyticsTracker,
    stringUtils
) {
    override fun onCustomerSelected(customerModel: WCCustomerModel) {
        triggerEvent(CustomerSelected(customerModel.toCustomerWithAnalytics(repository, mapper)))
    }
}

data class CustomerSelected(val customer: CustomerWithAnalytics) : MultiLiveEvent.Event()
