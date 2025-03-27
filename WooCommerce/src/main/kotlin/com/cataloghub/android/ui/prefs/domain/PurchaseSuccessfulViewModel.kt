package com.cataloghub.android.ui.prefs.domain

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.ui.prefs.domain.DomainFlowSource.SETTINGS
import com.cataloghub.android.ui.prefs.domain.DomainFlowSource.STORE_ONBOARDING
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.getStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class PurchaseSuccessfulViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    appPrefsWrapper: AppPrefsWrapper,
) : ScopedViewModel(savedStateHandle) {
    companion object {
        private const val KEY_DOMAIN = "domain"
    }

    private val _viewState = savedStateHandle.getStateFlow(this, ViewState(savedStateHandle[KEY_DOMAIN] ?: ""))
    val viewState = _viewState.asLiveData()

    init {
        analyticsTrackerWrapper.track(
            AnalyticsEvent.CUSTOM_DOMAINS_STEP,
            mapOf(
                AnalyticsTracker.KEY_SOURCE to appPrefsWrapper.getCustomDomainsSourceAsString(),
                AnalyticsTracker.KEY_STEP to AnalyticsTracker.VALUE_STEP_PURCHASE_SUCCESS
            )
        )
        if (appPrefsWrapper.getCustomDomainsSource() == STORE_ONBOARDING) {
            analyticsTrackerWrapper.track(
                stat = AnalyticsEvent.STORE_ONBOARDING_TASK_COMPLETED,
                properties = mapOf(AnalyticsTracker.ONBOARDING_TASK_KEY to AnalyticsTracker.VALUE_ADD_DOMAIN)
            )
            appPrefsWrapper.setCustomDomainsSource(SETTINGS) // Ensure onboarding task completed is only tracked once
        }
    }

    fun onDoneButtonClicked() {
        triggerEvent(NavigateToDashboard)
    }

    @Parcelize
    data class ViewState(
        val domain: String
    ) : Parcelable

    object NavigateToDashboard : MultiLiveEvent.Event()
}
