package com.cataloghub.android.di

import android.content.Context
import android.webkit.CookieManager
import com.cataloghub.android.AppPrefs
import com.cataloghub.android.BuildConfig
import com.cataloghub.android.FeedbackPrefs
import com.cataloghub.android.util.StringUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.wordpress.android.fluxc.network.UserAgent
import org.wordpress.android.fluxc.network.rest.wpcom.auth.AppSecrets
import java.util.Locale
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class AppConfigModule {
    companion object {
        private const val USER_AGENT_APPNAME = "wc-android"
    }

    @Provides
    fun provideAppSecrets() = AppSecrets(BuildConfig.OAUTH_APP_ID, BuildConfig.OAUTH_APP_SECRET)

    @Provides
    @Singleton
    fun provideUserAgent(appContext: Context) = UserAgent(appContext, USER_AGENT_APPNAME)

    @Provides
    fun provideDefaultLocale(): Locale = Locale.getDefault()

    @Provides
    @Singleton
    fun providesAppPrefs(appContext: Context): AppPrefs {
        AppPrefs.init(appContext)
        return AppPrefs
    }

    @Provides
    @Singleton
    fun provideFeedbackPrefs(appContext: Context) = FeedbackPrefs(appContext)

    @Provides
    @Singleton
    fun provideStringUtils() = StringUtils

    @Provides
    fun provideWebViewCookieManager() = CookieManager.getInstance()
}
