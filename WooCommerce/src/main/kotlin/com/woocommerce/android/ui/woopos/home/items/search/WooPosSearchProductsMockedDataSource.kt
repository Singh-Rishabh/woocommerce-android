package com.woocommerce.android.ui.woopos.home.items.search

import com.woocommerce.android.model.Product
import com.woocommerce.android.model.ProductCategory
import com.woocommerce.android.model.ProductTag
import com.woocommerce.android.ui.products.ProductBackorderStatus
import com.woocommerce.android.ui.products.ProductStatus
import com.woocommerce.android.ui.products.ProductStockStatus
import com.woocommerce.android.ui.products.ProductTaxStatus
import com.woocommerce.android.ui.products.ProductType
import com.woocommerce.android.ui.products.settings.ProductCatalogVisibility
import com.woocommerce.android.util.WooLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class WooPosSearchProductsMockedDataSource @Inject constructor() {
    private var productCache: List<Product> = generateSampleProducts()
    private val filteredProductCache: MutableMap<String, List<Product>> = mutableMapOf()
    private val cacheMutex = Mutex()
    private val canLoadMore = AtomicBoolean(true)

    val hasMorePages: Boolean
        get() = canLoadMore.get()

    fun searchProducts(query: String, forceRefresh: Boolean = false): Flow<ProductsResult> = flow {
        if (forceRefresh) {
            updateFilteredProductCache(query, emptyList())
        }

        val cachedResults = getCachedSearchResults(query)
        emit(ProductsResult.Cached(cachedResults))

        delay(1500)

        try {
            val remoteResults = performSearch(query, true)
            updateFilteredProductCache(query, remoteResults)

            canLoadMore.set(remoteResults.size >= PAGE_SIZE)

            emit(ProductsResult.Remote(Result.success(remoteResults)))
        } catch (e: Exception) {
            WooLog.e(WooLog.T.POS, "Search products failed - ${e.message}", e)
            emit(ProductsResult.Remote(Result.failure(e)))
        }
    }.flowOn(Dispatchers.IO).take(2)

    fun loadSimpleProducts(forceRefreshProducts: Boolean): Flow<ProductsResult> = flow {
        if (forceRefreshProducts) {
            updateProductCache(emptyList())
        }

        emit(ProductsResult.Cached(productCache))

        delay(1000)

        try {
            val remoteProducts = if (forceRefreshProducts) {
                generateSampleProducts()
            } else {
                productCache
            }
            updateProductCache(remoteProducts)
            canLoadMore.set(remoteProducts.size >= PAGE_SIZE)
            emit(ProductsResult.Remote(Result.success(productCache)))
        } catch (e: Exception) {
            WooLog.e(WooLog.T.POS, "Loading products failed - ${e.message}", e)
            emit(
                ProductsResult.Remote(
                    Result.failure(e)
                )
            )
        }
    }.flowOn(Dispatchers.IO).take(2)

    suspend fun loadMore(query: String? = null): Result<List<Product>> = withContext(Dispatchers.IO) {
        try {
            delay(1500)

            if (query != null) {
                val currentResults = filteredProductCache[query.lowercase()] ?: emptyList()
                val moreResults = performSearch(query, true, currentResults.size)
                val combinedResults = currentResults + moreResults
                updateFilteredProductCache(query, combinedResults)

                canLoadMore.set(moreResults.size >= PAGE_SIZE)

                Result.success(combinedResults)
            } else {
                val moreProducts = generateMoreSampleProducts(productCache.size)
                val updatedList = productCache + moreProducts
                updateProductCache(updatedList)

                canLoadMore.set(moreProducts.size >= PAGE_SIZE)

                Result.success(productCache)
            }
        } catch (e: Exception) {
            WooLog.e(WooLog.T.POS, "Loading more products failed - ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun getCachedSearchResults(query: String): List<Product> {
        return cacheMutex.withLock {
            filteredProductCache[query.lowercase()] ?: run {
                val results = performSearch(query, false)
                updateFilteredProductCache(query, results)
                results
            }
        }
    }

    private fun performSearch(query: String, isRemote: Boolean = false, offset: Int = 0): List<Product> {
        val searchTerm = query.trim().lowercase()
        if (searchTerm.isEmpty()) return productCache

        val sourceList = if (isRemote) {
            val fullList = productCache.toMutableList()

            if (Random.nextBoolean()) {
                val newProducts = generateMoreSampleProducts(fullList.size, 3)
                fullList.addAll(0, newProducts)
            }

            val removeCount = Random.nextInt(0, 2)
            if (removeCount > 0 && fullList.size > removeCount) {
                for (i in 0 until removeCount) {
                    val indexToRemove = Random.nextInt(0, fullList.size)
                    fullList.removeAt(indexToRemove)
                }
            }

            fullList
        } else {
            productCache
        }

        // Apply the same search logic for both local and remote
        val results = sourceList.filter { product ->
            product.name.lowercase().contains(searchTerm) ||
                product.sku.lowercase().contains(searchTerm) ||
                product.description.lowercase().contains(searchTerm)
        }

        // Apply pagination for remote search
        return if (isRemote && offset > 0) {
            val endIndex = minOf(offset + PAGE_SIZE, results.size)
            if (offset < results.size) {
                results.subList(offset, endIndex)
            } else {
                emptyList()
            }
        } else {
            results
        }
    }

    private suspend fun updateProductCache(newList: List<Product>) {
        cacheMutex.withLock { productCache = newList }
    }

    private suspend fun updateFilteredProductCache(query: String, results: List<Product>) {
        cacheMutex.withLock {
            filteredProductCache[query.lowercase()] = results
        }
    }

    private fun generateSampleProducts(): List<Product> {
        return List(20) { index -> createSampleProduct(index.toLong() + 1) }
    }

    private fun generateMoreSampleProducts(offset: Int, count: Int = 10): List<Product> {
        return List(count) { index ->
            createSampleProduct((offset + index + 1).toLong())
        }
    }

    private fun createSampleProduct(id: Long): Product {
        val productTypes = listOf(
            ProductType.SIMPLE.value,
            ProductType.VARIABLE.value,
            ProductType.GROUPED.value
        )
        val categories = listOf(
            ProductCategory(1, "Electronics", "electronics"),
            ProductCategory(2, "Clothing", "clothing"),
            ProductCategory(3, "Food", "food"),
            ProductCategory(4, "Books", "books"),
            ProductCategory(5, "Toys", "toys")
        )
        val tags = listOf(
            ProductTag(1, "Featured", "featured"),
            ProductTag(2, "Sale", "sale"),
            ProductTag(3, "New", "new"),
            ProductTag(4, "Popular", "popular")
        )

        val productType = productTypes.random()
        val productName = getRandomProductName(id)
        val price = BigDecimal(Random.nextDouble(5.0, 500.0)).setScale(2, RoundingMode.HALF_DOWN)

        val stockStatuses = listOf(
            ProductStockStatus.InStock,
            ProductStockStatus.OutOfStock,
            ProductStockStatus.OnBackorder
        )

        val backorderStatuses = listOf(
            ProductBackorderStatus.No,
            ProductBackorderStatus.Yes,
            ProductBackorderStatus.Notify
        )

        return Product(
            remoteId = id,
            parentId = 0,
            name = productName,
            description = "This is a detailed description for $productName. Perfect for all your needs.",
            shortDescription = "Short description for $productName",
            slug = productName.lowercase().replace(" ", "-"),
            type = productType,
            status = ProductStatus.PUBLISH,
            catalogVisibility = ProductCatalogVisibility.VISIBLE,
            isFeatured = Random.nextBoolean(),
            stockStatus = stockStatuses.random(),
            backorderStatus = backorderStatuses.random(),
            dateCreated = Date(),
            firstImageUrl = getProductImageUrl(id, productName),
            totalSales = Random.nextLong(0, 1000),
            reviewsAllowed = true,
            isVirtual = Random.nextBoolean(),
            ratingCount = Random.nextInt(0, 100),
            averageRating = Random.nextFloat() * 5,
            permalink = "https://example.com/product/$id",
            externalUrl = "",
            buttonText = "",
            price = price,
            salePrice = if (Random.nextBoolean()) price.multiply(BigDecimal("0.8")) else null,
            regularPrice = price,
            taxClass = "standard",
            isStockManaged = Random.nextBoolean(),
            stockQuantity = Random.nextDouble(1.0, 100.0),
            sku = "SKU-${id.toString().padStart(4, '0')}",
            globalUniqueId = "",
            shippingClass = "",
            shippingClassId = 0,
            isDownloadable = false,
            downloads = emptyList(),
            downloadLimit = 0,
            downloadExpiry = 0,
            purchaseNote = "",
            numVariations = if (productType == ProductType.VARIABLE.value) Random.nextInt(1, 5) else 0,
            images = listOf(
                Product.Image(
                    id = id,
                    name = "Product image",
                    source = getProductImageUrl(id, productName),
                    dateCreated = Date(),
                    isCoverImage = true
                )
            ),
            attributes = emptyList(),
            saleEndDateGmt = null,
            saleStartDateGmt = null,
            isSoldIndividually = false,
            taxStatus = ProductTaxStatus.Taxable,
            isSaleScheduled = false,
            isPurchasable = true,
            menuOrder = 0,
            categories = listOf(categories.random()),
            tags = if (Random.nextBoolean()) listOf(tags.random()) else emptyList(),
            groupedProductIds = emptyList(),
            crossSellProductIds = emptyList(),
            upsellProductIds = emptyList(),
            variationIds = if (productType == ProductType.VARIABLE.value)
                List(Random.nextInt(1, 5)) { Random.nextLong(1000, 2000) } else emptyList(),
            length = Random.nextFloat() * 10,
            width = Random.nextFloat() * 10,
            height = Random.nextFloat() * 10,
            weight = Random.nextFloat() * 5,
            isSampleProduct = false,
            specialStockStatus = null,
            isConfigurable = false,
            minAllowedQuantity = if (Random.nextBoolean()) Random.nextInt(1, 3) else null,
            maxAllowedQuantity = if (Random.nextBoolean()) Random.nextInt(5, 10) else null,
            bundleMinSize = null,
            bundleMaxSize = null,
            groupOfQuantity = null,
            combineVariationQuantities = null,
            password = null
        )
    }

    private fun getRandomProductName(id: Long): String {
        val adjectives = listOf(
            "Premium", "Luxury", "Essential", "Organic", "Handcrafted",
            "Vintage", "Modern", "Classic", "Eco-friendly", "Professional"
        )
        val products = listOf(
            "T-Shirt", "Laptop", "Coffee Maker", "Headphones", "Smartphone",
            "Watch", "Camera", "Backpack", "Sneakers", "Blender",
            "Desk Chair", "Sunglasses", "Water Bottle", "Yoga Mat", "Bluetooth Speaker"
        )

        val adjective = adjectives[id.toInt() % adjectives.size]
        val product = products[id.toInt() % products.size]

        return "$adjective $product"
    }

    private fun getProductImageUrl(id: Long, productName: String): String {
        return if (id % 2 == 0L) {
            "https://example.com/images/$productName.jpg"
        } else {
            "https://i0.wp.com/paymentwithoutaddress.wpcomstaging.com/wp-content/" +
                    "uploads/2024/09/303c15ed-36ba-4cbb-b093-4b7b20c988ff-1128-0000008a1d4aec00_file." +
                    "jpeg?fit=3000%2C2250&ssl=1"
        }
    }

    sealed class ProductsResult {
        data class Cached(val products: List<Product>) : ProductsResult()
        data class Remote(val productsResult: Result<List<Product>>) : ProductsResult()
    }

    companion object {
        private const val PAGE_SIZE = 10
    }
}
