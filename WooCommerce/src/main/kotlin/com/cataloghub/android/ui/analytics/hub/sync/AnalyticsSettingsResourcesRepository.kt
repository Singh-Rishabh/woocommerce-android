package com.cataloghub.android.ui.analytics.hub.sync

import com.cataloghub.android.model.AnalyticCardConfiguration
import com.cataloghub.android.model.AnalyticsCards
import com.cataloghub.android.ui.analytics.hub.GetAnalyticPluginsCardActive
import com.cataloghub.android.viewmodel.ResourceProvider
import javax.inject.Inject

class AnalyticsSettingsResourcesRepository @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val getAnalyticPluginsCardActive: GetAnalyticPluginsCardActive
) {
    suspend fun getDefaultAnalyticsCardsConfiguration(): List<AnalyticCardConfiguration> {
        val activePluginCards = getAnalyticPluginsCardActive()

        return AnalyticsCards.entries.map { card ->
            AnalyticCardConfiguration(
                card = card,
                title = resourceProvider.getString(card.resId),
                isVisible = card.isPlugin.not() || card in activePluginCards
            )
        }
    }
}
