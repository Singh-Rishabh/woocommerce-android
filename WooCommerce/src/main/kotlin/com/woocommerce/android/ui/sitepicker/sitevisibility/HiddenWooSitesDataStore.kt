package com.woocommerce.android.ui.sitepicker.sitevisibility

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.woocommerce.android.datastore.DataStoreQualifier
import com.woocommerce.android.datastore.DataStoreType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HiddenWooSitesDataStore @Inject constructor(
    @DataStoreQualifier(DataStoreType.SITE_PICKER_HIDDEN_SITES) private val dataStore: DataStore<Preferences>
) {
    suspend fun updateHiddenSites(hiddenSiteIds: Map<String, Boolean>) {
        hiddenSiteIds.forEach { (siteId, hidden) ->
            updateHiddenSite(siteId, hidden)
        }
    }

    fun isSiteHidden(siteId: String): Flow<Boolean> {
        return dataStore.data.map { prefs -> prefs[booleanPreferencesKey(siteId)] ?: true }
    }

    private suspend fun updateHiddenSite(siteId: String, isHidden: Boolean) {
        dataStore.edit { preferences ->
            preferences[booleanPreferencesKey(siteId)] = isHidden
        }
    }
}
