package com.cataloghub.android.apifaker.di

import android.content.Context
import com.cataloghub.android.apifaker.ApiFakerConfig
import com.cataloghub.android.apifaker.ApiFakerInterceptor
import com.cataloghub.android.apifaker.EndpointProcessor
import com.cataloghub.android.apifaker.db.ApiFakerDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import okhttp3.Interceptor
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiFakerModule {
    @Provides
    @Singleton
    internal fun providesDatabase(context: Context) = ApiFakerDatabase.buildDb(context)

    @Provides
    internal fun providesEndpointDao(db: ApiFakerDatabase) = db.endpointDao

    @Provides
    @IntoSet
    @Named("interceptors")
    internal fun providesInterceptor(
        apiFakerConfig: ApiFakerConfig,
        endpointProcessor: EndpointProcessor
    ): Interceptor = ApiFakerInterceptor(apiFakerConfig, endpointProcessor)
}
