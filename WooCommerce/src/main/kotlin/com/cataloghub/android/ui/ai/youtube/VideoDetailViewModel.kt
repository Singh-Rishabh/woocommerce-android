package com.cataloghub.android.ui.ai.youtube

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.R
import com.cataloghub.android.model.AIProduct
import com.cataloghub.android.model.AIProductStatus
import com.cataloghub.android.ui.ai.AINetworkLogger
import com.cataloghub.android.ui.ai.AIRepository
import com.cataloghub.android.ui.ai.YouTubeVideo
import com.cataloghub.android.util.WooLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import com.cataloghub.android.ui.ai.ProductReviewResponse

@HiltViewModel
class VideoDetailViewModel @Inject constructor(
    private val repository: AIRepository
) : ViewModel() {

    private val _videoDetails = MutableLiveData<YouTubeVideo>()
    val videoDetails: LiveData<YouTubeVideo> = _videoDetails

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<Int?>()
    val errorMessage: LiveData<Int?> = _errorMessage

    private val _productsExist = MutableLiveData<Boolean>()
    val productsExist: LiveData<Boolean> = _productsExist

    private val _processingComplete = MutableLiveData<Boolean>()
    val processingComplete: LiveData<Boolean> = _processingComplete

    private val _pendingProducts = MutableLiveData<List<AIProduct>>()
    val pendingProducts: LiveData<List<AIProduct>> = _pendingProducts

    private val _approvedProducts = MutableLiveData<List<AIProduct>>()
    val approvedProducts: LiveData<List<AIProduct>> = _approvedProducts

    private val _rejectedProducts = MutableLiveData<List<AIProduct>>()
    val rejectedProducts: LiveData<List<AIProduct>> = _rejectedProducts

    private val _processingError = MutableLiveData<String?>()
    val processingError: LiveData<String?> = _processingError

    private var videoId: String? = null
    private var youtubeUrl: String? = null

    init {
        AINetworkLogger.logRequest("VideoDetailViewModel", "Initialized")
        WooLog.d(WooLog.T.AI, "VideoDetailViewModel initialized")
    }

    fun loadVideoDetails(storeUrl: String, videoId: String) {
        this.videoId = videoId

        viewModelScope.launch {
            _isLoading.value = true

            try {
                val video = repository.getYouTubeVideoDetails(storeUrl, videoId)
                _videoDetails.value = video
                youtubeUrl = "https://www.youtube.com/watch?v=${video.videoId}"

                AINetworkLogger.logResponse(
                    "YouTube Video Details",
                    "ID: ${video.videoId}, Title: ${video.title}"
                )
                WooLog.d(WooLog.T.AI, "Loaded YouTube video details: ${video.title}")
            } catch (e: Exception) {
                AINetworkLogger.logError("YouTube Video Details Error", e)
                WooLog.e(WooLog.T.AI, "Failed to load YouTube video details", e)
                _errorMessage.value = R.string.ai_error_loading_video
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun checkExistingProducts(storeUrl: String, videoId: String) {
        viewModelScope.launch {
            try {
                val youtubeUrl = "https://www.youtube.com/watch?v=$videoId"
                val products = repository.getProducts(youtubeUrl, storeUrl)

                // Sort products by status
                val pendingProducts = products.filter { it.status == AIProductStatus.PENDING.name }
                    .map { convertToAIProduct(it) }
                val approvedProducts = products.filter { it.status == AIProductStatus.APPROVED.name }
                    .map { convertToAIProduct(it) }
                val rejectedProducts = products.filter { it.status == AIProductStatus.REJECTED.name }
                    .map { convertToAIProduct(it) }

                _pendingProducts.value = pendingProducts
                _approvedProducts.value = approvedProducts
                _rejectedProducts.value = rejectedProducts

                // Products exist if any list has items
                _productsExist.value = pendingProducts.isNotEmpty() ||
                        approvedProducts.isNotEmpty() ||
                        rejectedProducts.isNotEmpty()

            } catch (e: Exception) {
                // For 404 errors, just assume no products exist yet (normal case for new videos)
                if (e.message?.contains("404") == true) {
                    _productsExist.value = false
                    WooLog.d(WooLog.T.AI, "No existing products found for video $videoId")
                } else {
                    AINetworkLogger.logError("Check Existing Products Error", e)
                    WooLog.e(WooLog.T.AI, "Failed to check existing products", e)
                    // Don't show error to user as this is a background check
                }
            }
        }
    }

    fun processVideo(storeUrl: String, videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                AINetworkLogger.logRequest("Process Video", "Starting video processing for videoId=$videoId, storeUrl=$storeUrl")
                WooLog.d(WooLog.T.AI, "Starting video processing for videoId=$videoId, storeUrl=$storeUrl")
                
                val result = repository.processYouTubeVideo(videoId, storeUrl, false)
                
                // Convert ProductReviewResponse objects to AIProduct objects
                val products = result.products.map { product ->
                    AIProduct(
                        id = product.id,
                        title = product.name,
                        description = product.description ?: "",
                        price = product.price ?: 0.0,
                        imageUrl = product.thumbnailUrl ?: "",
                        videoId = videoId,
                        videoTitle = "", // Not available
                        videoThumbnailUrl = product.thumbnailUrl ?: "",
                        createdAt = try {
                            product.createdAt?.let { createdAtStr -> 
                                java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                                    .parse(createdAtStr) 
                            } ?: Date()
                        } catch (e: Exception) {
                            Date()
                        },
                        status = when {
                            product.status.equals("approved", ignoreCase = true) -> AIProductStatus.APPROVED
                            product.status.equals("rejected", ignoreCase = true) -> AIProductStatus.REJECTED
                            else -> AIProductStatus.PENDING
                        },
                        categories = emptyList(),
                        tags = emptyList()
                    )
                }
                
                categorizeProducts(products)
                _processingComplete.value = true

                AINetworkLogger.logResponse(
                    "Process Video Success",
                    "Processed ${products.size} products for video $videoId"
                )
                WooLog.d(
                    WooLog.T.AI,
                    "Successfully processed video $videoId, generated ${products.size} products"
                )
            } catch (e: retrofit2.HttpException) {
                val errorCode = e.code()
                val errorBody = e.response()?.errorBody()?.string() ?: "Unknown error"
                
                AINetworkLogger.logError("Process Video Error (HTTP $errorCode)", e)
                WooLog.e(WooLog.T.AI, "Error processing video: HTTP $errorCode - $errorBody", e)
                
                val errorMessage = when (errorCode) {
                    400 -> "Invalid request. Please check video URL and try again."
                    401 -> "Authentication error. Please log in again."
                    403 -> "You don't have permission to process this video."
                    404 -> "Video not found. Please check the URL and try again."
                    429 -> "Too many requests. Please try again later."
                    500, 502, 503, 504 -> "Server error (HTTP $errorCode). Our team has been notified."
                    else -> "Error processing video (HTTP $errorCode). Please try again later."
                }
                
                _processingError.value = "$errorMessage\n\nDetails: $errorBody"
            } catch (e: Exception) {
                AINetworkLogger.logError("Process Video Error", e)
                WooLog.e(WooLog.T.AI, "Error processing video", e)
                _processingError.value = "Error: ${e.message ?: "Unknown error"}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                val products = repository.getProducts()
                val videoProducts = videoId?.let { id -> products.filter { it.videoId == id } } ?: products
                categorizeProducts(videoProducts)

                AINetworkLogger.logResponse(
                    "Load Products",
                    "Loaded ${videoProducts.size} products"
                )
                WooLog.d(WooLog.T.AI, "Loaded ${videoProducts.size} products")
            } catch (e: Exception) {
                AINetworkLogger.logError("Load Products Error", e)
                WooLog.e(WooLog.T.AI, "Failed to load products", e)
                _errorMessage.value = R.string.ai_error_loading_products
            }
        }
    }

    private fun categorizeProducts(products: List<AIProduct>) {
        val pending = mutableListOf<AIProduct>()
        val approved = mutableListOf<AIProduct>()
        val rejected = mutableListOf<AIProduct>()

        products.forEach { product ->
            when (product.status) {
                AIProductStatus.PENDING -> pending.add(product)
                AIProductStatus.APPROVED -> approved.add(product)
                AIProductStatus.REJECTED -> rejected.add(product)
            }
        }

        _pendingProducts.value = pending
        _approvedProducts.value = approved
        _rejectedProducts.value = rejected
    }

    fun errorMessageShown() {
        _errorMessage.value = null
    }

    fun processingCompleteHandled() {
        _processingComplete.value = false
    }

    fun setupFallbackVideoPlayer(videoId: String) {
        // Create a minimal video details object so the player can still work
        if (_videoDetails.value == null) {
            _videoDetails.value = YouTubeVideo(
                videoId = videoId,
                title = "YouTube Video",
                description = "",
                publishedAt = "",
                channelTitle = "",
                thumbnails = emptyMap(),
                viewCount = null,
                likeCount = null,
                duration = null
            )
        }
    }

    // Convert ProductReviewResponse to AIProduct
    private fun convertToAIProduct(product: ProductReviewResponse): AIProduct {
        return AIProduct(
            id = product.id,
            title = product.name,
            description = product.description ?: "",
            price = product.price ?: 0.0,
            imageUrl = product.thumbnailUrl ?: "",
            videoId = "", // Extract from URL if needed
            videoTitle = "", // May need to set this based on context
            videoThumbnailUrl = product.thumbnailUrl ?: "",
            createdAt = try {
                product.createdAt?.let {
                    java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
                        .parse(it)
                } ?: Date()
            } catch (e: Exception) {
                Date()
            },
            status = AIProductStatus.valueOf(product.status),
            categories = emptyList(), // Set if available in API response
            tags = emptyList()  // Set if available in API response
        )
    }
}
