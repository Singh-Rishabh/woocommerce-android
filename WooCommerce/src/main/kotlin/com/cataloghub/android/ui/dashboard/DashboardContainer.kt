package com.cataloghub.android.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cataloghub.android.R
import com.cataloghub.android.model.DashboardWidget
import com.cataloghub.android.ui.blaze.creation.BlazeCampaignCreationDispatcher
import com.cataloghub.android.ui.compose.component.WCColoredButton
import com.cataloghub.android.ui.compose.component.WCOutlinedButton
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardCardsState
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardEvent.OpenRangePicker
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetUiModel
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetUiModel.ConfigurableWidget
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetUiModel.FeedbackWidget
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetUiModel.GoLiveWidget
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetUiModel.NewWidgetsCard
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetUiModel.ShareStoreWidget
import com.cataloghub.android.ui.dashboard.blaze.DashboardBlazeCard
import com.cataloghub.android.ui.dashboard.coupons.DashboardCouponsCard
import com.cataloghub.android.ui.dashboard.google.DashboardGoogleAdsCard
import com.cataloghub.android.ui.dashboard.inbox.DashboardInboxCard
import com.cataloghub.android.ui.dashboard.onboarding.DashboardOnboardingCard
import com.cataloghub.android.ui.dashboard.orders.DashboardOrdersCard
import com.cataloghub.android.ui.dashboard.reviews.DashboardReviewsCard
import com.cataloghub.android.ui.dashboard.stats.DashboardStatsCard
import com.cataloghub.android.ui.dashboard.stock.DashboardProductStockCard
import com.cataloghub.android.ui.dashboard.topperformers.DashboardTopPerformersWidgetCard
import com.cataloghub.android.ui.main.MainActivityViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardContainer(
    mainActivityViewModel: MainActivityViewModel,
    dashboardViewModel: DashboardViewModel,
    blazeCampaignCreationDispatcher: BlazeCampaignCreationDispatcher,
) {
    dashboardViewModel.dashboardCardsState.observeAsState().value?.let { state ->

        val pullRefreshState = rememberPullRefreshState(state.isRefreshing, dashboardViewModel::onPullToRefresh)
        BoxWithConstraints(Modifier.pullRefresh(pullRefreshState)) {
            val boxWithConstraintsScope = this
            DashboardWidgets(
                widgetUiModels = state.widgets,
                mainActivityViewModel = mainActivityViewModel,
                dashboardViewModel = dashboardViewModel,
                blazeCampaignCreationDispatcher = blazeCampaignCreationDispatcher,
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.surface)
                    .padding(horizontal = 16.dp),
                numberOfColumns = calculateColumnNumber(boxWithConstraintsScope.maxWidth, state)
            )
            PullRefreshIndicator(
                refreshing = state.isRefreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
                contentColor = MaterialTheme.colors.primary,
            )
        }
    }
}

@Composable
private fun DashboardWidgets(
    widgetUiModels: List<DashboardWidgetUiModel>,
    mainActivityViewModel: MainActivityViewModel,
    dashboardViewModel: DashboardViewModel,
    blazeCampaignCreationDispatcher: BlazeCampaignCreationDispatcher,
    modifier: Modifier = Modifier,
    numberOfColumns: Int = 1
) {
    val nestedScrollInterop = rememberNestedScrollInteropConnection()

    if (numberOfColumns == 1) {
        Column(
            modifier = modifier
                .nestedScroll(nestedScrollInterop)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(modifier = Modifier)
            widgetUiModels.forEach { widget ->
                AnimatedVisibility(widget.isVisible) {
                    DashboardWidgetCard(
                        widget,
                        mainActivityViewModel,
                        dashboardViewModel,
                        blazeCampaignCreationDispatcher,
                        Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(modifier = Modifier)
        }
    } else {
        val widgetColumns = splitWidgetsIntoColumns(
            numberOfColumns = numberOfColumns,
            visibleUiWidgets = widgetUiModels.filter { it.isVisible }
        )
        Row(
            modifier = modifier
                .nestedScroll(nestedScrollInterop)
                .verticalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier)
            widgetColumns.forEach { columnWidgets ->
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    columnWidgets.forEach { widget ->
                        DashboardWidgetCard(
                            widget,
                            mainActivityViewModel,
                            dashboardViewModel,
                            blazeCampaignCreationDispatcher,
                            Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier)
        }
    }
}

@Suppress("MagicNumber")
private fun calculateColumnNumber(
    availableWidthInDp: Dp,
    state: DashboardCardsState
) = when {
    availableWidthInDp < 600.dp -> 1 // 600dp covers 99.96% of phones in portrait
    availableWidthInDp < 1000.dp -> 2 // 1000dp should be enough to avoid 3 columns on big phones in landscape
    else -> 3 // 3 columns should only display on tablets in landscape
}.coerceAtMost(
    maximumValue = state.widgets.filter { it.isVisible }.size
)

private fun splitWidgetsIntoColumns(
    numberOfColumns: Int,
    visibleUiWidgets: List<DashboardWidgetUiModel>
): MutableList<MutableList<DashboardWidgetUiModel>> {
    val widgetColumns = MutableList<MutableList<DashboardWidgetUiModel>>(numberOfColumns) { mutableListOf() }
    for ((index, item) in visibleUiWidgets.withIndex()) {
        widgetColumns[index % numberOfColumns].add(item)
    }
    return widgetColumns
}

@Composable
private fun DashboardWidgetCard(
    it: DashboardWidgetUiModel,
    mainActivityViewModel: MainActivityViewModel,
    dashboardViewModel: DashboardViewModel,
    blazeCampaignCreationDispatcher: BlazeCampaignCreationDispatcher,
    widgetModifier: Modifier
) {
    when (it) {
        is ConfigurableWidget -> {
            ConfigurableWidgetCard(
                widgetUiModel = it,
                mainActivityViewModel = mainActivityViewModel,
                dashboardViewModel = dashboardViewModel,
                blazeCampaignCreationDispatcher = blazeCampaignCreationDispatcher,
                modifier = widgetModifier
            )
        }

        is ShareStoreWidget -> {
            ShareStoreCard(
                onShareClicked = it.onShareClicked,
                modifier = widgetModifier
            )
        }

        is FeedbackWidget -> {
            FeedbackCard(
                widget = it,
                modifier = widgetModifier
            )
        }

        is NewWidgetsCard -> {
            NewWidgetsCard(
                state = it,
                modifier = widgetModifier
            )
        }
        is GoLiveWidget -> {
            GoLiveCard(
                onGoLiveClicked = it.onGoLiveClicked,
                modifier = widgetModifier
            )
        }
    }
}

@Composable
private fun ConfigurableWidgetCard(
    widgetUiModel: DashboardWidgetUiModel.ConfigurableWidget,
    mainActivityViewModel: MainActivityViewModel,
    dashboardViewModel: DashboardViewModel,
    blazeCampaignCreationDispatcher: BlazeCampaignCreationDispatcher,
    modifier: Modifier
) {
    when (widgetUiModel.widget.type) {
        DashboardWidget.Type.STATS -> {
            DashboardStatsCard(
                openDatePicker = { start, end, callback ->
                    dashboardViewModel.onDashboardWidgetEvent(OpenRangePicker(start, end, callback))
                },
                parentViewModel = dashboardViewModel,
                modifier = modifier
            )
        }

        DashboardWidget.Type.POPULAR_PRODUCTS -> DashboardTopPerformersWidgetCard(
            parentViewModel = dashboardViewModel,
            modifier = modifier
        )

        DashboardWidget.Type.BLAZE -> DashboardBlazeCard(
            blazeCampaignCreationDispatcher = blazeCampaignCreationDispatcher,
            activityViewModel = mainActivityViewModel,
            parentViewModel = dashboardViewModel,
            modifier = modifier
        )

        DashboardWidget.Type.ONBOARDING -> DashboardOnboardingCard(
            parentViewModel = dashboardViewModel,
            modifier = modifier
        )

        DashboardWidget.Type.ORDERS -> DashboardOrdersCard(
            parentViewModel = dashboardViewModel,
            modifier = modifier
        )

        DashboardWidget.Type.REVIEWS -> DashboardReviewsCard(
            parentViewModel = dashboardViewModel,
            modifier = modifier
        )

        DashboardWidget.Type.COUPONS -> DashboardCouponsCard(
            parentViewModel = dashboardViewModel,
            modifier = modifier
        )

        DashboardWidget.Type.STOCK -> DashboardProductStockCard(
            parentViewModel = dashboardViewModel,
            modifier = modifier
        )

        DashboardWidget.Type.INBOX -> DashboardInboxCard(
            parentViewModel = dashboardViewModel,
            modifier = modifier
        )

        DashboardWidget.Type.GOOGLE_ADS -> {
            DashboardGoogleAdsCard(
                parentViewModel = dashboardViewModel,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun GoLiveCard(
    onGoLiveClicked: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.woo_gray_5),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_go_live),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.go_live_and_sell),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.go_live_and_sell_message),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        WCColoredButton(
            onClick = onGoLiveClicked,
            text = stringResource(id = R.string.go_live)
        )
    }
}

@Composable
private fun ShareStoreCard(
    onShareClicked: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.woo_gray_5),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.blaze_campaign_created_success),
            contentDescription = null
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(id = R.string.get_the_word_out),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(id = R.string.share_your_store_message),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        WCColoredButton(
            onClick = onShareClicked,
            text = stringResource(id = R.string.share_store_button)
        )
    }
}

@Composable
private fun FeedbackCard(
    widget: DashboardWidgetUiModel.FeedbackWidget,
    modifier: Modifier
) {
    LaunchedEffect(Unit) {
        widget.onShown()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.woo_gray_5),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(id = R.string.feedback_request_title),
            style = MaterialTheme.typography.body1,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 8.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            WCOutlinedButton(
                onClick = widget.onNegativeClick,
                text = stringResource(id = R.string.feedback_request_make_better),
                modifier = Modifier.weight(1f)
            )
            WCColoredButton(
                onClick = widget.onPositiveClick,
                text = stringResource(id = R.string.feedback_request_like_it),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NewWidgetsCard(
    state: DashboardWidgetUiModel.NewWidgetsCard,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .border(
                width = 1.dp,
                color = colorResource(id = R.color.woo_gray_5),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.dashboard_new_widgets_card_title),
            style = MaterialTheme.typography.h6,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dashboard_new_widgets_card_description),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        WCColoredButton(
            onClick = state.onShowCardsClick,
            text = stringResource(id = R.string.dashboard_new_widgets_card_button)
        )
    }
}