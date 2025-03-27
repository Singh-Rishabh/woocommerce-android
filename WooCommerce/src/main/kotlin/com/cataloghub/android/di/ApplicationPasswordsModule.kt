package com.cataloghub.android.di

import com.cataloghub.android.BuildConfig
import com.cataloghub.android.applicationpasswords.ApplicationPasswordsNotifier
import com.cataloghub.android.util.DeviceInfo
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.wordpress.android.fluxc.module.ApplicationPasswordsClientId
import org.wordpress.android.fluxc.network.rest.wpapi.applicationpasswords.ApplicationPasswordsListener

@Module
@InstallIn(SingletonComponent::class)
interface ApplicationPasswordsModule {
    @Binds
    fun bindApplicationPasswordsListener(
        notifier: ApplicationPasswordsNotifier
    ): ApplicationPasswordsListener

    companion object {
        @Provides
        @ApplicationPasswordsClientId
        fun providesApplicationPasswordClientId() =
            "${BuildConfig.APPLICATION_ID}.app-client.${DeviceInfo.name.replace(' ', '-')}"
    }
}
