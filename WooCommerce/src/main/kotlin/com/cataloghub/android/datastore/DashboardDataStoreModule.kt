package com.cataloghub.android.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.automattic.android.tracks.crashlogging.CrashLogging
import com.cataloghub.android.di.SiteComponent
import com.cataloghub.android.di.SiteCoroutineScope
import com.cataloghub.android.di.SiteScope
import com.cataloghub.android.ui.dashboard.data.DashboardSerializer
import com.cataloghub.android.ui.mystore.data.DashboardDataModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.wordpress.android.fluxc.model.SiteModel

@Module
@InstallIn(SiteComponent::class)
object DashboardDataStoreModule {
    @Provides
    @SiteScope
    fun provideDashboardDataStore(
        appContext: Context,
        crashLogging: CrashLogging,
        @SiteCoroutineScope siteCoroutineScope: CoroutineScope,
        site: SiteModel
    ): DataStore<DashboardDataModel> = DataStoreFactory.create(
        produceFile = {
            appContext.dataStoreFile("dashboard_configuration_${site.id}")
        },
        corruptionHandler = ReplaceFileCorruptionHandler {
            crashLogging.recordEvent("Corrupted data store. DataStore Type: DASHBOARD")
            DashboardDataModel.getDefaultInstance()
        },
        scope = CoroutineScope(siteCoroutineScope.coroutineContext + Dispatchers.IO),
        serializer = DashboardSerializer
    )
}
