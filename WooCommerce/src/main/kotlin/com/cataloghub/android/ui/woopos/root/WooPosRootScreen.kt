package com.cataloghub.android.ui.woopos.root

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.ui.woopos.common.composeui.WooPosPreview
import com.cataloghub.android.ui.woopos.common.composeui.designsystem.WooPosTheme
import com.cataloghub.android.ui.woopos.root.navigation.WooPosRootHost
import com.cataloghub.android.ui.woopos.root.navigation.handleNavigationEvent
import com.cataloghub.android.ui.woopos.util.analytics.WooPosAnalyticsCommonPropertiesProvider
import com.cataloghub.android.ui.woopos.util.analytics.WooPosAnalyticsTracker

@Composable
fun WooPosRootScreen(modifier: Modifier = Modifier, wooPosAnalyticsTracker: WooPosAnalyticsTracker) {
    val rootController = rememberNavController()
    val activity = LocalContext.current as ComponentActivity

    WooPosRootHost(
        modifier = modifier.fillMaxSize(),
        rootController = rootController,
        onNavigationEvent = { event ->
            rootController.handleNavigationEvent(event, activity, wooPosAnalyticsTracker)
        }
    )
}

@WooPosPreview
@Composable
fun PreviewWooPosRootScreen() {
    val tracker = WooPosAnalyticsTracker(
        analyticsTrackerWrapper = AnalyticsTrackerWrapper(),
        commonPropertiesProvider = WooPosAnalyticsCommonPropertiesProvider(),
    )
    WooPosTheme { WooPosRootScreen(wooPosAnalyticsTracker = tracker) }
}
