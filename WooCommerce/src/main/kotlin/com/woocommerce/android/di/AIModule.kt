package com.woocommerce.android.di

import com.woocommerce.android.ui.ai.AIService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AIModule {
    @Provides
    @Singleton
    fun provideAIService(@Named("ai-retrofit") retrofit: Retrofit): AIService {
        return retrofit.create(AIService::class.java)
    }
} 