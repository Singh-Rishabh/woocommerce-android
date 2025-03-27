package com.cataloghub.android.ui.dashboard.data

import com.cataloghub.android.model.DashboardWidget
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.google.IsGoogleForWooEnabled
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveGoogleAdsWidgetStatus @Inject constructor(
    private val selectedSite: SelectedSite,
    private val isGoogleForWooEnabled: IsGoogleForWooEnabled
) {
    operator fun invoke() = selectedSite.observe()
        .filterNotNull()
        .map {
            if (isGoogleForWooEnabled()) {
                DashboardWidget.Status.Available
            } else {
                DashboardWidget.Status.Hidden
            }
        }
}
