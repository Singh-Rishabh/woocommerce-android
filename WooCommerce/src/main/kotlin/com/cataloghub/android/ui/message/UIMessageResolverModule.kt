package com.cataloghub.android.ui.message

import com.cataloghub.android.ui.base.UIMessageResolver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
interface UIMessageResolverModule {
    @Binds
    fun provideUiMessageResolver(defaultUIMessageResolver: DefaultUIMessageResolver): UIMessageResolver
}
