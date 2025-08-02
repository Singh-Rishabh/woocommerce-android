package com.cataloghub.android.ui.live

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LiveSessionModule {

    @Provides
    @Singleton
    fun provideLiveSessionApi(retrofit: Retrofit): LiveSessionApi {
        return retrofit.create(LiveSessionApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLiveSessionRepository(liveSessionApi: LiveSessionApi): LiveSessionRepository {
        return LiveSessionRepository(liveSessionApi)
    }
}
