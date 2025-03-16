package com.cataloghub.android.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val AI_BASE_URL = "https://cataloghub-a5hyfchgaxgtecd6.centralindia-01.azurewebsites.net/"

    @Provides
    @Singleton
    @Named("ai-retrofit")
    fun provideAIRetrofit(@Named("regular") okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AI_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
