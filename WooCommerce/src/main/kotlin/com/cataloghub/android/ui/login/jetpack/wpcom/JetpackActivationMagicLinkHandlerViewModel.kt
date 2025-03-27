package com.cataloghub.android.ui.login.jetpack.wpcom

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cataloghub.android.analytics.AnalyticsEvent.JETPACK_SETUP_LOGIN_FLOW
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.login.jetpack.JetpackActivationRepository
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JetpackActivationMagicLinkHandlerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    selectedSite: SelectedSite,
    jetpackActivationRepository: JetpackActivationRepository,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) : JetpackActivationWPComPostLoginViewModel(
    savedStateHandle,
    selectedSite,
    jetpackActivationRepository,
    analyticsTrackerWrapper
) {
    private val navArgs: JetpackActivationMagicLinkHandlerFragmentArgs by savedStateHandle.navArgs()

    private val _viewState = MutableStateFlow(ViewState.Loading)
    val viewState = _viewState.asLiveData()

    init {
        continueLogin()
    }

    fun continueLogin() = launch {
        _viewState.value = ViewState.Loading
        onLoginSuccess(navArgs.jetpackStatus).onFailure {
            _viewState.value = ViewState.Error
        }
    }

    fun onCloseClick() {
        // Disable back button when fetching sites
        if (viewState.value == ViewState.Loading) return
        triggerEvent(Exit)

        analyticsTrackerWrapper.track(
            JETPACK_SETUP_LOGIN_FLOW,
            mapOf(
                AnalyticsTracker.KEY_STEP to AnalyticsTracker.VALUE_JETPACK_SETUP_STEP_MAGIC_LINK,
                AnalyticsTracker.KEY_TAP to AnalyticsTracker.VALUE_DISMISS
            )
        )
    }

    enum class ViewState {
        Loading, Error
    }
}
