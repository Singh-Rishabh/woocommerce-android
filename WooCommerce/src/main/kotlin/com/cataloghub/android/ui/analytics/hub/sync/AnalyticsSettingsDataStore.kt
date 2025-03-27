package com.cataloghub.android.ui.analytics.hub.sync

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.cataloghub.android.datastore.DataStoreQualifier
import com.cataloghub.android.datastore.DataStoreType
import com.cataloghub.android.model.AnalyticCardConfiguration
import com.cataloghub.android.tools.SelectedSite
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AnalyticsSettingsDataStore @Inject constructor(
    @DataStoreQualifier(DataStoreType.ANALYTICS_CONFIGURATION) private val dataStore: DataStore<Preferences>,
    private val gson: Gson,
    private val selectedSite: SelectedSite
) {
    private fun getCardsConfigurationKey() = "${selectedSite.getOrNull()?.siteId ?: ""}AnalyticsCardsConfiguration"
    private val configurationType = object : TypeToken<List<AnalyticCardConfiguration>>() {}.type

    fun observeCardsConfiguration(): Flow<List<AnalyticCardConfiguration>?> {
        return dataStore.data.map { prefs ->
            val rawCards = prefs[stringPreferencesKey(getCardsConfigurationKey())]
            runCatching {
                gson.fromJson<List<AnalyticCardConfiguration>>(rawCards, configurationType)
            }.getOrNull()
        }
    }

    suspend fun saveAnalyticsCardsConfiguration(cards: List<AnalyticCardConfiguration>) {
        dataStore.edit { preferences ->
            preferences[stringPreferencesKey(getCardsConfigurationKey())] = gson.toJson(cards)
        }
    }
}
