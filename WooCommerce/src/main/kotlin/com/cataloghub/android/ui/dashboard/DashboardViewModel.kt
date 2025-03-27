package com.cataloghub.android.ui.dashboard

import androidx.annotation.StringRes
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import com.cataloghub.android.AppPrefsWrapper
import com.cataloghub.android.FeedbackPrefs
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsEvent.DYNAMIC_DASHBOARD_CARD_INTERACTED
import com.cataloghub.android.analytics.AnalyticsEvent.FEATURE_JETPACK_BENEFITS_BANNER
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.extensions.isSitePublic
import com.cataloghub.android.model.DashboardWidget
import com.cataloghub.android.model.UiString
import com.cataloghub.android.model.UiString.UiStringRes
import com.cataloghub.android.network.ConnectionChangeReceiver
import com.cataloghub.android.network.ConnectionChangeReceiver.ConnectionChangeEvent
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.tools.SiteConnectionType
import com.cataloghub.android.tools.connectionType
import com.cataloghub.android.ui.analytics.ranges.StatsTimeRangeSelection.SelectionType
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardEvent.OpenEditWidgets
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardEvent.RefreshJitm
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetUiModel.NewWidgetsCard
import com.cataloghub.android.ui.dashboard.data.DashboardRepository
import com.cataloghub.android.ui.prefs.privacy.banner.domain.ShouldShowPrivacyBanner
import com.cataloghub.android.util.PackageUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event
import com.cataloghub.android.viewmodel.ResourceProvider
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.Calendar
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DashboardViewModel @Inject constructor(
    savedState: SavedStateHandle,
    private val resourceProvider: ResourceProvider,
    private val selectedSite: SelectedSite,
    private val appPrefsWrapper: AppPrefsWrapper,
    private val usageTracksEventEmitter: DashboardStatsUsageTracksEventEmitter,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    dashboardTransactionLauncher: DashboardTransactionLauncher,
    shouldShowPrivacyBanner: ShouldShowPrivacyBanner,
    dashboardRepository: DashboardRepository,
    private val feedbackPrefs: FeedbackPrefs,
) : ScopedViewModel(savedState) {
    companion object {
        private const val DAYS_TO_REDISPLAY_JP_BENEFITS_BANNER = 5
        val SUPPORTED_RANGES_ON_MY_STORE_TAB = listOf(
            SelectionType.TODAY,
            SelectionType.WEEK_TO_DATE,
            SelectionType.MONTH_TO_DATE,
            SelectionType.YEAR_TO_DATE,
            SelectionType.CUSTOM
        )
    }

    val performanceObserver: LifecycleObserver = dashboardTransactionLauncher

    private var _appbarState = MutableLiveData<AppbarState>()
    val appbarState: LiveData<AppbarState> = _appbarState

    private val _refreshTrigger = MutableSharedFlow<RefreshEvent>(extraBufferCapacity = 1)
    val refreshTrigger: Flow<RefreshEvent> = _refreshTrigger.asSharedFlow()

    val storeName = selectedSite.observe().map { site ->
        if (!site?.displayName.isNullOrBlank()) {
            site?.displayName
        } else {
            site?.name
        } ?: resourceProvider.getString(R.string.store_name_default)
    }.asLiveData()

    val jetpackBenefitsBannerState = selectedSite.observe()
        .filterNotNull()
        .flatMapLatest { site ->
            jetpackBenefitsBannerState(site.connectionType)
        }.asLiveData()

    private val dashboardWidgets = combine(
        dashboardRepository.widgets,
        dashboardRepository.hasNewWidgets,
        feedbackPrefs.userFeedbackIsDueObservable
    ) { configurableWidgets, hasNewWidgets, userFeedbackIsDue ->
        mapWidgetsToUiModels(configurableWidgets, hasNewWidgets, userFeedbackIsDue)
    }

    val hasNewWidgets = dashboardRepository.hasNewWidgets.asLiveData()

    private val refreshingOnBackground = MutableStateFlow(-1)

    fun displayRefreshingIndicator() {
        refreshingOnBackground.value += 1
    }

    fun hideRefreshingIndicator() {
        val value = (refreshingOnBackground.value - 1).coerceAtLeast(-1)
        refreshingOnBackground.value = value
    }

    val dashboardCardsState = combine(
        dashboardWidgets,
        refreshingOnBackground.map { it > -1 }
    ) { widgets, isRefreshing ->
        DashboardCardsState(
            widgets = widgets,
            isRefreshing = isRefreshing
        )
    }.asLiveData()

    init {
        ConnectionChangeReceiver.getEventBus().register(this)

        launch {
            shouldShowPrivacyBanner().let {
                if (it && !PackageUtils.isTesting()) {
                    triggerEvent(DashboardEvent.ShowPrivacyBanner)
                }
            }
        }

        updateShareStoreButtonVisibility()
    }

    private fun updateShareStoreButtonVisibility() {
        _appbarState.value = AppbarState(
            showShareStoreButton = selectedSite.get().let {
                it.isSitePublic && it.url != null
            }
        )
    }

    override fun onCleared() {
        ConnectionChangeReceiver.getEventBus().unregister(this)
        super.onCleared()
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ConnectionChangeEvent) {
        if (event.isConnected) {
            _refreshTrigger.tryEmit(RefreshEvent())
        }
    }

    fun onPullToRefresh() {
        usageTracksEventEmitter.interacted()
        analyticsTrackerWrapper.track(AnalyticsEvent.DASHBOARD_PULLED_TO_REFRESH)
        _refreshTrigger.tryEmit(RefreshEvent(isForced = true))
        triggerEvent(RefreshJitm)
    }

    fun onResume() {
        _refreshTrigger.tryEmit(RefreshEvent())
    }

    fun onShareStoreClicked() {
        AnalyticsTracker.track(AnalyticsEvent.DASHBOARD_SHARE_YOUR_STORE_BUTTON_TAPPED)
        triggerEvent(
            DashboardEvent.ShareStore(storeUrl = selectedSite.get().url)
        )
    }

    fun onEditWidgetsClicked() {
        analyticsTrackerWrapper.track(
            AnalyticsEvent.DYNAMIC_DASHBOARD_EDIT_LAYOUT_BUTTON_TAPPED,
            mapOf(AnalyticsTracker.KEY_NEW_CARD_AVAILABLE to hasNewWidgets.value.toString())
        )
        triggerEvent(OpenEditWidgets)
    }

    fun onDashboardWidgetEvent(event: DashboardEvent) {
        triggerEvent(event)
    }

    fun onHideWidgetClicked(type: DashboardWidget.Type) {
        analyticsTrackerWrapper.track(
            AnalyticsEvent.DYNAMIC_DASHBOARD_HIDE_CARD_TAPPED,
            mapOf(AnalyticsTracker.KEY_TYPE to type.trackingIdentifier)
        )
        triggerEvent(OpenEditWidgets)
    }

    fun onContactSupportClicked() {
        triggerEvent(DashboardEvent.ContactSupport)
    }

    fun onShowSnackbar(@StringRes message: Int) {
        triggerEvent(Event.ShowSnackbar(message))
    }

    fun trackCardInteracted(type: String) {
        analyticsTrackerWrapper.track(
            DYNAMIC_DASHBOARD_CARD_INTERACTED,
            mapOf(AnalyticsTracker.KEY_TYPE to type)
        )
    }

    private fun mapWidgetsToUiModels(
        widgets: List<DashboardWidget>,
        hasNewWidgets: Boolean,
        userFeedbackIsDue: Boolean
    ): List<DashboardWidgetUiModel> = buildList {
        addAll(
            widgets.map { DashboardWidgetUiModel.ConfigurableWidget(it) }
        )

        // We add the other cards even if they should not be visible, so that the addition/deletion
        // of the cards is animated properly

        val shouldShowShareCard = !widgets.first { it.type == DashboardWidget.Type.STATS }.isAvailable &&
            selectedSite.get().isSitePublic &&
            selectedSite.get().url != null
        add(DashboardWidgetUiModel.ShareStoreWidget(shouldShowShareCard, ::onShareStoreClicked))

        add(
            // Show at the second row
            (indexOfFirst { it.isVisible } + 1).coerceIn(0..<size),
            DashboardWidgetUiModel.FeedbackWidget(
                isVisible = userFeedbackIsDue,
                onShown = {
                    analyticsTrackerWrapper.track(
                        AnalyticsEvent.APP_FEEDBACK_PROMPT,
                        mapOf(AnalyticsTracker.KEY_FEEDBACK_ACTION to AnalyticsTracker.VALUE_FEEDBACK_SHOWN)
                    )
                },
                onPositiveClick = {
                    analyticsTrackerWrapper.track(
                        AnalyticsEvent.APP_FEEDBACK_PROMPT,
                        mapOf(AnalyticsTracker.KEY_FEEDBACK_ACTION to AnalyticsTracker.VALUE_FEEDBACK_LIKED)
                    )
                    feedbackPrefs.lastFeedbackDate = Calendar.getInstance().time
                    triggerEvent(DashboardEvent.FeedbackPositiveAction)
                },
                onNegativeClick = {
                    analyticsTrackerWrapper.track(
                        AnalyticsEvent.APP_FEEDBACK_PROMPT,
                        mapOf(AnalyticsTracker.KEY_FEEDBACK_ACTION to AnalyticsTracker.VALUE_FEEDBACK_NOT_LIKED)
                    )
                    feedbackPrefs.lastFeedbackDate = Calendar.getInstance().time
                    triggerEvent(DashboardEvent.FeedbackNegativeAction)
                }
            )
        )

        add(
            NewWidgetsCard(
                isVisible = hasNewWidgets && !shouldShowShareCard,
                onShowCardsClick = {
                    analyticsTrackerWrapper.track(AnalyticsEvent.DYNAMIC_DASHBOARD_ADD_NEW_SECTIONS_TAPPED)
                    triggerEvent(OpenEditWidgets)
                }
            )
        )
    }

    private fun jetpackBenefitsBannerState(
        connectionType: SiteConnectionType
    ): Flow<JetpackBenefitsBannerUiModel?> {
        if (connectionType == SiteConnectionType.Jetpack || appPrefsWrapper.isSiteWPComSuspended) {
            return flowOf(null)
        }

        val dismissTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

        return dismissTrigger.onStart { emit(Unit) }
            .map {
                val durationSinceDismissal =
                    (System.currentTimeMillis() - appPrefsWrapper.getJetpackBenefitsDismissalDate()).milliseconds
                val showBanner = durationSinceDismissal >= DAYS_TO_REDISPLAY_JP_BENEFITS_BANNER.days
                JetpackBenefitsBannerUiModel(
                    show = showBanner,
                    onDismiss = {
                        appPrefsWrapper.recordJetpackBenefitsDismissal()
                        analyticsTrackerWrapper.track(
                            stat = FEATURE_JETPACK_BENEFITS_BANNER,
                            properties = mapOf(AnalyticsTracker.KEY_JETPACK_BENEFITS_BANNER_ACTION to "dismissed")
                        )
                        dismissTrigger.tryEmit(Unit)
                    }
                )
            }
    }

    sealed interface DashboardWidgetUiModel {
        val isVisible: Boolean

        data class ConfigurableWidget(
            val widget: DashboardWidget,
        ) : DashboardWidgetUiModel {
            override val isVisible: Boolean
                get() = widget.isVisible
        }

        data class ShareStoreWidget(
            override val isVisible: Boolean,
            val onShareClicked: () -> Unit
        ) : DashboardWidgetUiModel

        data class FeedbackWidget(
            override val isVisible: Boolean,
            val onShown: () -> Unit,
            val onPositiveClick: () -> Unit,
            val onNegativeClick: () -> Unit
        ) : DashboardWidgetUiModel

        data class NewWidgetsCard(
            override val isVisible: Boolean,
            val onShowCardsClick: () -> Unit
        ) : DashboardWidgetUiModel
    }

    data class AppbarState(
        val showShareStoreButton: Boolean = false,
    )

    sealed class DashboardEvent : Event() {

        data object ShowPrivacyBanner : DashboardEvent()

        data class ShareStore(val storeUrl: String) : DashboardEvent()

        data object OpenEditWidgets : DashboardEvent()

        data class OpenRangePicker(
            val start: Long,
            val end: Long,
            val callback: (Long, Long) -> Unit
        ) : DashboardEvent()

        data object ContactSupport : DashboardEvent()

        data object FeedbackPositiveAction : DashboardEvent()

        data object FeedbackNegativeAction : DashboardEvent()

        data object RefreshJitm : DashboardEvent()
    }

    data class RefreshEvent(val isForced: Boolean = false)

    data class JetpackBenefitsBannerUiModel(
        val show: Boolean = false,
        val onDismiss: () -> Unit = {}
    )

    data class DashboardWidgetMenu(
        val items: List<DashboardWidgetAction>
    )

    data class DashboardWidgetAction(
        val title: UiString,
        val action: () -> Unit
    ) {
        constructor(@StringRes titleResource: Int, action: () -> Unit) : this(
            title = UiStringRes(titleResource),
            action = action
        )
    }

    data class DashboardCardsState(
        val widgets: List<DashboardWidgetUiModel>,
        val isRefreshing: Boolean
    )
}
