package com.cataloghub.android.ui.onboarding.launchstore

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.extensions.isFreeTrial
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.common.webview.WebViewAuthenticator
import com.cataloghub.android.ui.onboarding.StoreOnboardingRepository
import com.cataloghub.android.ui.onboarding.StoreOnboardingRepository.Error
import com.cataloghub.android.ui.onboarding.StoreOnboardingRepository.LaunchStoreError.ALREADY_LAUNCHED
import com.cataloghub.android.ui.onboarding.StoreOnboardingRepository.LaunchStoreError.GENERIC_ERROR
import com.cataloghub.android.ui.onboarding.StoreOnboardingRepository.Success
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialog
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.UserAgent
import java.net.URL
import javax.inject.Inject

@HiltViewModel
class LaunchStoreViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val launchStoreOnboardingRepository: StoreOnboardingRepository,
    private val selectedSite: SelectedSite,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    val webViewAuthenticator: WebViewAuthenticator,
    val userAgent: UserAgent
) : ScopedViewModel(savedStateHandle), DefaultLifecycleObserver {

    private val _viewState = MutableStateFlow(
        LaunchStoreState(
            isTrialPlan = selectedSite.get().isFreeTrial,
            isStoreLaunched = false,
            isLoading = false,
            siteUrl = selectedSite.get().url,
            displayUrl = selectedSite.get().getAbsoluteUrl()
        )
    )
    val viewState = _viewState.asLiveData()

    override fun onResume(owner: LifecycleOwner) {
        _viewState.value = _viewState.value.copy(
            isTrialPlan = selectedSite.get().isFreeTrial
        )
    }

    fun launchStore() {
        _viewState.value = _viewState.value.copy(isLoading = true)
        launch {
            val result = launchStoreOnboardingRepository.launchStore()
            when (result) {
                Success -> {
                    analyticsTrackerWrapper.track(
                        stat = AnalyticsEvent.STORE_ONBOARDING_TASK_COMPLETED,
                        properties = mapOf(AnalyticsTracker.ONBOARDING_TASK_KEY to AnalyticsTracker.VALUE_LAUNCH_SITE)
                    )
                    _viewState.value = _viewState.value.copy(isStoreLaunched = true)
                }
                is Error -> {
                    when (result.type) {
                        ALREADY_LAUNCHED -> triggerEvent(
                            ShowDialog(
                                titleId = R.string.store_onboarding_store_already_launched_error_title,
                                messageId = R.string.store_onboarding_store_already_launched_error_description,
                                positiveButtonId = R.string.store_onboarding_launch_store_ok
                            )
                        )
                        GENERIC_ERROR -> triggerEvent(
                            ShowDialog(
                                titleId = R.string.store_onboarding_launch_store_generic_error_title,
                                messageId = R.string.store_onboarding_launch_store_generic_error_description,
                                positiveButtonId = R.string.try_again,
                                positiveBtnAction = { _, _ -> launchStore() },
                                negativeButtonId = R.string.cancel
                            )
                        )
                    }
                }
            }
            _viewState.value = _viewState.value.copy(isLoading = false)
        }
    }

    fun onBackPressed() {
        triggerEvent(MultiLiveEvent.Event.Exit)
    }

    fun shareStoreUrl() {
        triggerEvent(ShareStoreUrl(selectedSite.get().url))
    }

    private fun SiteModel.getAbsoluteUrl(): String = runCatching { URL(url).host }.getOrDefault("")

    data class LaunchStoreState(
        val isTrialPlan: Boolean,
        val isStoreLaunched: Boolean,
        val isLoading: Boolean,
        val siteUrl: String,
        val displayUrl: String
    )

    data class ShareStoreUrl(val url: String) : MultiLiveEvent.Event()
}
