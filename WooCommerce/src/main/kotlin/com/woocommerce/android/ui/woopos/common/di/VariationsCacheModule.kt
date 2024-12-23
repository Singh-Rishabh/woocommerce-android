package com.woocommerce.android.ui.woopos.common.di

import com.woocommerce.android.model.ProductVariation
import com.woocommerce.android.ui.woopos.home.items.variations.VariationsLRUCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object VariationsCacheModule {
    private const val VARIATION_CACHE_MAX_SIZE = 50

    @Provides
    @Singleton
    fun provideVariationsLRUCache(): VariationsLRUCache<Long, List<ProductVariation>> {
        val maxSize = VARIATION_CACHE_MAX_SIZE
        return VariationsLRUCache(maxSize)
    }
}
