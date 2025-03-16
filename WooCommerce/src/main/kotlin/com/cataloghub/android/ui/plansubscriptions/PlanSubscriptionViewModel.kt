package com.cataloghub.android.ui.plansubscriptions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.extensions.formatStyleFull
import com.cataloghub.android.extensions.isFreeTrial
import com.cataloghub.android.support.help.HelpOrigin
import com.cataloghub.android.support.zendesk.ZendeskTags
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.plans.domain.CalculatePlanRemainingPeriod
import com.cataloghub.android.ui.plans.domain.FREE_TRIAL_PERIOD
import com.cataloghub.android.ui.plans.domain.FREE_TRIAL_UPGRADE_PLAN
import com.cataloghub.android.ui.plans.domain.SitePlan
import com.cataloghub.android.ui.plans.domain.SitePlan.Type.FREE_TRIAL
import com.cataloghub.android.ui.plans.domain.SitePlan.Type.OTHER
import com.cataloghub.android.ui.plans.repository.SitePlanRepository
import com.cataloghub.android.ui.plansubscriptions.PlanSubscriptionViewModel.UpgradesViewState.Error
import com.cataloghub.android.ui.plansubscriptions.PlanSubscriptionViewModel.UpgradesViewState.Loading
import com.cataloghub.android.ui.plansubscriptions.PlanSubscriptionViewModel.UpgradesViewState.NonUpgradeable
import com.cataloghub.android.ui.plansubscriptions.PlanSubscriptionViewModel.UpgradesViewState.PlanEnded
import com.cataloghub.android.ui.plansubscriptions.PlanSubscriptionViewModel.UpgradesViewState.TrialEnded
import com.cataloghub.android.ui.plansubscriptions.PlanSubscriptionViewModel.UpgradesViewState.TrialInProgress
import com.cataloghub.android.util.StringUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ResourceProvider
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Period
import javax.inject.Inject

@HiltViewModel
class PlanSubscriptionViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val selectedSite: SelectedSite,
    private val planRepository: SitePlanRepository,
    private val calculatePlanRemainingPeriod: CalculatePlanRemainingPeriod,
    private val resourceProvider: ResourceProvider,
    private val tracks: AnalyticsTrackerWrapper
) : ScopedViewModel(savedState) {

    private val _upgradesState = MutableLiveData<UpgradesViewState>()
    val upgradesState: LiveData<UpgradesViewState> = _upgradesState

    private val SitePlan.formattedPlanName
        get() = generateFormattedPlanName(resourceProvider)

    init {
        loadSubscriptionState()
    }

    fun onReportSubscriptionIssueClicked() {
        tracks.track(AnalyticsEvent.UPGRADES_REPORT_SUBSCRIPTION_ISSUE_TAPPED, tracksProperties)

        val tags = selectedSite.getIfExists()
            ?.takeIf { it.isFreeTrial }
            ?.let { listOf(ZendeskTags.freeTrialTag) }
            ?: emptyList()
        triggerEvent(OpenSupportRequestForm(HelpOrigin.UPGRADES, tags))
    }

    private fun loadSubscriptionState() {
        launch {
            _upgradesState.value = Loading

            _upgradesState.value = planRepository
                .fetchCurrentPlanDetails(selectedSite.get())
                ?.let { createViewStateFrom(it) }
                ?: Error
        }
    }

    private fun createViewStateFrom(sitePlan: SitePlan) = when (sitePlan.type) {
        FREE_TRIAL -> createFreeTrialViewStateFrom(sitePlan)
        OTHER -> createOtherPlansViewStateFrom(sitePlan)
    }

    private fun createOtherPlansViewStateFrom(sitePlan: SitePlan) =
        sitePlan.mapAsViewState(
            isExpired = {
                PlanEnded(name = resourceProvider.getString(R.string.upgrades_plan_ended_name, formattedPlanName))
            },
            isNotExpired = {
                NonUpgradeable(
                    name = formattedPlanName,
                    currentPlanEndDate = expirationDate.toLocalDate().formatStyleFull()
                )
            }
        )

    private fun createFreeTrialViewStateFrom(sitePlan: SitePlan) =
        sitePlan.mapAsViewState(
            isExpired = {
                TrialEnded(name = resourceProvider.getString(R.string.free_trial_trial_ended))
            },
            isNotExpired = { remainingPeriod ->
                TrialInProgress(
                    name = formattedPlanName,
                    freeTrialDuration = FREE_TRIAL_PERIOD,
                    daysLeftInFreeTrial = StringUtils.getQuantityString(
                        resourceProvider = resourceProvider,
                        quantity = remainingPeriod.days,
                        default = R.string.free_trial_days_left_plural,
                        one = R.string.free_trial_one_day_left
                    )
                )
            }
        )

    private inline fun SitePlan.mapAsViewState(
        crossinline isNotExpired: SitePlan.(Period) -> UpgradesViewState.HasPlan,
        crossinline isExpired: SitePlan.() -> UpgradesViewState.HasPlan
    ): UpgradesViewState.HasPlan {
        val daysUntilExpiration = calculatePlanRemainingPeriod(expirationDate)
        return when {
            daysUntilExpiration.isZero || daysUntilExpiration.isNegative -> isExpired()
            else -> isNotExpired(daysUntilExpiration)
        }
    }

    sealed interface UpgradesViewState {

        sealed interface HasPlan : UpgradesViewState {
            val name: String
        }

        object Loading : UpgradesViewState

        object Error : UpgradesViewState

        data class TrialEnded(
            override val name: String,
            val planToUpgrade: String = FREE_TRIAL_UPGRADE_PLAN
        ) : HasPlan

        data class TrialInProgress(
            override val name: String,
            val freeTrialDuration: Period,
            val daysLeftInFreeTrial: String
        ) : HasPlan

        data class PlanEnded(
            override val name: String
        ) : HasPlan

        data class NonUpgradeable(
            override val name: String,
            val currentPlanEndDate: String
        ) : HasPlan
    }

    data class OpenSupportRequestForm(
        val origin: HelpOrigin,
        val extraTags: List<String>
    ) : MultiLiveEvent.Event()

    companion object {
        private val tracksProperties = mapOf(AnalyticsTracker.KEY_SOURCE to AnalyticsTracker.VALUE_UPGRADES_SCREEN)
    }
}
