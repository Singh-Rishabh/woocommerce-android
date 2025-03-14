package com.woocommerce.android.ui.woopos.home.items.search

import com.woocommerce.android.ui.woopos.home.items.WooPosItem
import kotlinx.coroutines.delay
import javax.inject.Inject

@Suppress("MagicNumber")
class WooPosItemsSearchEmptyStateProvider @Inject constructor() {
    suspend fun getPopularItems(): List<WooPosItem> {
        delay(50)
        return listOf<WooPosItem>(
            WooPosItem.SimpleProduct(
                id = 1,
                name = "Popular Item 1",
                price = "10.0$",
                imageUrl = "https://example.com/image1.jpg",
            ),
            WooPosItem.SimpleProduct(
                id = 2,
                name = "Popular Item 2",
                price = "20.0$",
                imageUrl = "https://example.com/image2.jpg",
            ),
            WooPosItem.Variation(
                id = 3,
                name = "Popular Item 3",
                productId = 1,
                price = "30.0$",
                imageUrl = "https://example.com/image3.jpg",
            ),
        )
    }

    suspend fun getLastSearches(): List<String> {
        delay(70)
        return listOf(
            "T-shirt",
            "Jeans",
            "Shoes",
        )
    }
}
