package com.cataloghub.android.ui.onboarding.payments

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.onboarding.StoreOnboardingRepository.OnboardingTaskType
import com.cataloghub.android.util.WooLog
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ScopedViewModel
import com.cataloghub.android.viewmodel.navArgs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flowOf
import org.wordpress.android.fluxc.utils.extensions.slashJoin
import javax.inject.Inject

@HiltViewModel
class GetPaidViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    selectedSite: SelectedSite,
    private val analyticsTracker: AnalyticsTrackerWrapper
) : ScopedViewModel(savedStateHandle) {
    companion object {
        private const val SUCCESS_FLAG = "wcpay-connection-success"
        private const val CANCELLATION_FLAG = "wcpay-connection-error"
    }

    private val args: GetPaidFragmentArgs by savedStateHandle.navArgs()
    private val isWooPaymentsSetup = args.taskId == OnboardingTaskType.WC_PAYMENTS.id
    private var isDismissed = false

    private val setupUrl = selectedSite.get().url.slashJoin("/wp-admin/admin.php?page=wc-admin&task=${args.taskId}")

    val viewState = flowOf(
        ViewState(
            url = setupUrl,
            shouldAuthenticate = selectedSite.get().isWPComAtomic
        )
    ).asLiveData()

    fun onBackPressed() {
        triggerEvent(MultiLiveEvent.Event.Exit)
    }

    fun onUrlLoaded(url: String) {
        if (isDismissed || !isWooPaymentsSetup) return

        when {
            url.contains(SUCCESS_FLAG) -> {
                WooLog.d(WooLog.T.ONBOARDING, "WooPayments setup completed successfully")
                analyticsTracker.track(
                    stat = AnalyticsEvent.STORE_ONBOARDING_TASK_COMPLETED,
                    properties = mapOf(AnalyticsTracker.ONBOARDING_TASK_KEY to AnalyticsTracker.VALUE_WOO_PAYMENTS)
                )
                triggerEvent(ShowWooPaymentsSetupSuccess)
                isDismissed = true
            }

            url.contains(CANCELLATION_FLAG) -> {
                WooLog.d(WooLog.T.ONBOARDING, "WooPayments setup dismissed")
                triggerEvent(MultiLiveEvent.Event.Exit)
                isDismissed = true
            }
        }
    }

    data class ViewState(
        val url: String,
        val shouldAuthenticate: Boolean
    )

    object ShowWooPaymentsSetupSuccess : MultiLiveEvent.Event()
}
