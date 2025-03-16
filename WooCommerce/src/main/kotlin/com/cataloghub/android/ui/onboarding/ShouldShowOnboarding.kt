package com.cataloghub.android.ui.onboarding

import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.analytics.AnalyticsEvent.STORE_ONBOARDING_COMPLETED
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.onboarding.StoreOnboardingRepository.OnboardingTask
import javax.inject.Inject
import javax.inject.Singleton

@Singleton // Needed to keep track of the pending tasks when tracking
class ShouldShowOnboarding @Inject constructor(
    private val appPrefsWrapper: AppPrefsWrapper,
    private val selectedSite: SelectedSite,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper
) {
    private var pendingTasks: List<OnboardingTask> = emptyList()

    fun showForTasks(tasks: List<OnboardingTask>): Boolean {
        if (tasks.isEmpty()) return false

        pendingTasks = tasks.filter { !it.isComplete }

        val siteId = selectedSite.getSelectedSiteId()
        val areAllTaskCompleted = if (tasks.all { it.isComplete }) {
            if (appPrefsWrapper.getStoreOnboardingShown(siteId) && !appPrefsWrapper.isOnboardingCompleted(siteId)) {
                analyticsTrackerWrapper.track(stat = STORE_ONBOARDING_COMPLETED)
            }
            true
        } else {
            false
        }

        return if (!areAllTaskCompleted) {
            appPrefsWrapper.setStoreOnboardingShown(siteId)
            true
        } else {
            false
        }
    }

    fun isOnboardingMarkedAsCompleted(): Boolean =
        appPrefsWrapper.isOnboardingCompleted(selectedSite.getSelectedSiteId())
}
