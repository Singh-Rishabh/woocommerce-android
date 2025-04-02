package com.woocommerce.android.ui.woopos.home.items.search

import com.woocommerce.android.ui.woopos.home.items.WooPosItemSelectionViewState
import com.woocommerce.android.ui.woopos.util.datastore.WooPosPreferencesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@Suppress("MagicNumber")
class WooPosItemsSearchEmptyStateProvider @Inject constructor(
    private val preferencesRepository: WooPosPreferencesRepository
) {
    suspend fun getPopularItems(): List<WooPosItemSelectionViewState.Product> {
        delay(50)
        return listOf(
            WooPosItemSelectionViewState.Product.Simple(
                id = 1,
                name = "Popular Item 1",
                price = "10.0$",
                imageUrl = "https://example.com/image1.jpg",
            ),
            WooPosItemSelectionViewState.Product.Simple(
                id = 2,
                name = "Popular Item 2",
                price = "20.0$",
                imageUrl = "https://example.com/image2.jpg",
            ),
            WooPosItemSelectionViewState.Product.Variable(
                id = 3,
                name = "Popular Item 3",
                price = "30.0$",
                imageUrl = "https://example.com/image3.jpg",
                numOfVariations = 2,
                variationIds = listOf(4, 5),
            ),
        )
    }

    suspend fun getLastSearches(): List<String> = preferencesRepository.recentProductSearches.first()
}
