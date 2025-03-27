package com.cataloghub.android.ui.woopos.common.di

import com.cataloghub.android.ui.woopos.home.items.navigation.WooPosItemsNavigator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@InstallIn(ActivityRetainedComponent::class)
@Module
class WooPosActivityProvidesModule {
    @ActivityRetainedScoped
    @Provides
    fun provideLeftPaneNavigator(): WooPosItemsNavigator = WooPosItemsNavigator()
}
