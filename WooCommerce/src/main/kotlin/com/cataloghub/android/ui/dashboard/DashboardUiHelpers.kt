package com.cataloghub.android.ui.dashboard

import com.cataloghub.android.R
import com.cataloghub.android.model.DashboardWidget
import com.cataloghub.android.model.UiString.UiStringRes
import com.cataloghub.android.ui.dashboard.DashboardViewModel.DashboardWidgetAction

fun DashboardWidget.Type.defaultHideMenuEntry(onHideClicked: () -> Unit): DashboardWidgetAction {
    return DashboardWidgetAction(
        title = UiStringRes(
            R.string.dynamic_dashboard_widget_menu_item_hide,
            params = listOf(UiStringRes(titleResource))
        ),
        action = onHideClicked
    )
}
