package com.cataloghub.android.ui.ai

import com.cataloghub.android.model.AIProduct
import com.cataloghub.android.model.AIProductStatus
import com.google.gson.annotations.SerializedName
import com.cataloghub.android.tools.SelectedSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import android.util.Log
import retrofit2.Response

@Singleton
class AIRepository @Inject constructor(
    private val aiService: AIService,
    val selectedSite: SelectedSite
) {
    private val TAG = "AIRepository"

    /**
     * Process a YouTube video to extract products
     */
    suspend fun processVideo(
        youtubeUrl: String,
        storeUrl: String,
        autoApprove: Boolean = false
    ): ProcessingResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Processing video with URL: $youtubeUrl, Store URL: $storeUrl, Auto-approve: $autoApprove")

            // Extract video ID from YouTube URL
            val videoId = extractVideoId(youtubeUrl)
            if (videoId.isNullOrEmpty()) {
                Log.e(TAG, "Failed to extract video ID from URL: $youtubeUrl")
                throw IllegalArgumentException("Invalid YouTube URL: $youtubeUrl")
            }
            
            Log.d(TAG, "Extracted video ID: $videoId, creating request with storeUrl: $storeUrl")

            // Create request object using the updated schema with video_id
            val request = YouTubeProcessVideoRequest(
                videoId = videoId,
                storeUrl = storeUrl,
                autoApprove = autoApprove
            )
            
            // Log the exact request payload that will be sent
            Log.d(TAG, "Sending request to API endpoint: /api/v1/youtube-videos/process")
            Log.d(TAG, "Request payload: { \"video_id\": \"$videoId\", \"store_url\": \"$storeUrl\", \"auto_approve\": $autoApprove }")
            
            try {
                val result = aiService.processVideo(request)
                Log.d(TAG, "Video processing successful. CollectionId: ${result.collectionId}, Products: ${result.products.size}")
                
                // Log details about the response
                Log.d(TAG, "Processing result: success=${result.success}, status=${result.status}, totalProducts=${result.totalProducts}")
                
                if (result.products.isNotEmpty()) {
                    Log.d(TAG, "First product: id=${result.products.first().id}, name=${result.products.first().name}")
                }
                
                return@withContext result
            } catch (e: retrofit2.HttpException) {
                val responseCode = e.code()
                val responseMessage = e.message()
                val errorBody = e.response()?.errorBody()?.string() ?: "No error body"
                
                Log.e(TAG, "HTTP error during video processing: code=$responseCode, message=$responseMessage")
                Log.e(TAG, "HTTP error details: $errorBody")
                
                // Log more details about the request that failed
                Log.e(TAG, "Failed request details: endpoint=/api/v1/youtube-videos/process, method=POST")
                Log.e(TAG, "Failed request payload: { \"video_id\": \"$videoId\", \"store_url\": \"$storeUrl\", \"auto_approve\": $autoApprove }")
                
                AINetworkLogger.logApiError("HTTP $responseCode Error", Exception("API error: $errorBody"))
                throw e
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing video: ${e.message}", e)
            Log.e(TAG, "Error details: ${e.stackTraceToString()}")
            AINetworkLogger.logApiError("Video Processing Error", e)
            throw e
        }
    }

    /**
     * Process an Azure blob video/audio file to extract products and create them in WooCommerce.
     */
    suspend fun processAzureVideoWithCollection(
        azureUrl: String,
        storeUrl: String,
        collectionName: String,
        autoApprove: Boolean = false
    ): ProcessingResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Processing Azure video with URL: $azureUrl, Store URL: $storeUrl, Collection Name: $collectionName, Auto-approve: $autoApprove")

            val request = ProcessAzureRequest(
                azureUrl = azureUrl,
                storeUrl = storeUrl,
                autoApprove = autoApprove,
                collectionName = collectionName
            )

            Log.d(TAG, "Sending request to API endpoint: /azure-videos/process")
            Log.d(TAG, "Request payload: { \"azure_url\": \"$azureUrl\", \"store_url\": \"$storeUrl\", \"auto_approve\": $autoApprove, \"collection_name\": \"$collectionName\" }")

            val result = aiService.processAzureVideo(request)
            Log.d(TAG, "Azure video processing successful. CollectionId: ${result.collectionId}, Products: ${result.products.size}")
            Log.d(TAG, "Processing result: success=${result.success}, status=${result.status}, totalProducts=${result.totalProducts}")
            if (result.products.isNotEmpty()) {
                Log.d(TAG, "First product: id=${result.products.first().id}, name=${result.products.first().name}")
            }
            return@withContext result
        } catch (e: retrofit2.HttpException) {
            val responseCode = e.code()
            val responseMessage = e.message()
            val errorBody = e.response()?.errorBody()?.string() ?: "No error body"
            Log.e(TAG, "HTTP error during Azure video processing: code=$responseCode, message=$responseMessage")
            Log.e(TAG, "HTTP error details: $errorBody")
            Log.e(TAG, "Failed request details: endpoint=/azure-videos/process, method=POST")
            Log.e(TAG, "Failed request payload: { \"azure_url\": \"$azureUrl\", \"store_url\": \"$storeUrl\", \"auto_approve\": $autoApprove, \"collection_name\": \"$collectionName\" }")
            AINetworkLogger.logApiError("HTTP $responseCode Error", Exception("API error: $errorBody"))
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error processing Azure video: ${e.message}", e)
            Log.e(TAG, "Error details: ${e.stackTraceToString()}")
            AINetworkLogger.logApiError("Azure Video Processing Error", e)
            throw e
        }
    }

    /**
     * Extract video ID from YouTube URL
     */
    private fun extractVideoId(youtubeUrl: String): String? {
        val regex = Regex("(?:youtube\\.com\\/watch\\?v=|youtu\\.be\\/)([\\w-]+)")
        val matchResult = regex.find(youtubeUrl)
        return matchResult?.groupValues?.getOrNull(1)
    }

    /**
     * Get products generated from a YouTube video
     */
    suspend fun getProducts(
        youtubeUrl: String,
        storeUrl: String
    ): List<ProductReviewResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting products for youtubeUrl: $youtubeUrl, storeUrl: $storeUrl")
            aiService.getProducts(
                youtubeUrl = youtubeUrl,
                storeUrl = storeUrl
            )
        } catch (e: Exception) {
            // Log the error for debugging
            Log.e(TAG, "Error getting products: ${e.message}", e)

            // If it's a 404, just return an empty list
            if (e.message?.contains("HTTP 404") == true) {
                Log.d(TAG, "No products found (404), returning empty list")
                emptyList()
            } else {
                // For other errors, re-throw
                throw e
            }
        }
    }

    /**
     * Get products for a YouTube video by its URL using the new /api/v1/youtube-videos/by-youtube-url endpoint
     * This endpoint is recommended for getting products associated with a YouTube video
     * 
     * @param youtubeUrl The YouTube video URL
     * @param storeUrl WooCommerce store URL
     * @param status Optional filter by product status (draft/approved/rejected)
     * @return List of products associated with the YouTube video
     */
    suspend fun getProductsByYoutubeUrl(
        youtubeUrl: String,
        storeUrl: String,
        status: String? = null
    ): List<ProductReviewResponse> = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting products by YouTube URL: $youtubeUrl, storeUrl: $storeUrl, status: $status")
            AINetworkLogger.logRequest("Get Products by YouTube URL", 
                "youtube_url=$youtubeUrl, store_url=$storeUrl, status=$status")
            
            val products = aiService.getProductsByYoutubeUrl(
                youtubeUrl = youtubeUrl,
                storeUrl = storeUrl,
                status = status
            )
            
            Log.d(TAG, "Retrieved ${products.size} products for video $youtubeUrl")
            AINetworkLogger.logResponse("Get Products by YouTube URL", 
                "Retrieved ${products.size} products for video $youtubeUrl")
            
            products
        } catch (e: Exception) {
            // Log the error for debugging
            Log.e(TAG, "Error getting products by YouTube URL: ${e.message}", e)
            AINetworkLogger.logApiError("Get Products by YouTube URL Error", e)

            // If it's a 404, just return an empty list
            if (e is retrofit2.HttpException && e.code() == 404) {
                Log.d(TAG, "No products found (404), returning empty list")
                emptyList()
            } else {
                // For other errors, re-throw
                throw e
            }
        }
    }

    suspend fun editProducts(request: ProductEditRequest): List<ProductReviewResponse> = withContext(Dispatchers.IO) {
        aiService.editProducts(request)
    }

    // YouTube OAuth methods

    suspend fun getYouTubeAuthUrl(storeUrl: String): String {
        Log.d(TAG, "Getting YouTube auth URL for store: $storeUrl")
        try {
            Log.d(TAG, "Making API call to getYouTubeAuthUrl endpoint")
            val response = aiService.getYouTubeAuthUrl(storeUrl)
            val authUrl = response.authUrl
            Log.d(TAG, "Raw response received: $response")

            if (authUrl.isNullOrEmpty()) {
                Log.e(TAG, "Received null or empty auth URL from API")
                throw Exception("Failed to get authorization URL from server")
            }

            Log.d(TAG, "Received YouTube auth URL: $authUrl")
            AINetworkLogger.logResponse("YouTube Auth URL", "Received URL: $authUrl")
            return authUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error getting YouTube auth URL: ${e.message}", e)
            Log.e(TAG, "Error details: ${e.stackTraceToString()}")
            AINetworkLogger.logApiError("Getting YouTube auth URL", e)
            throw e
        }
    }

    /**
     * Check if YouTube is connected via token-status API
     */
    suspend fun checkYouTubeTokenStatus(storeUrl: String): YouTubeTokenStatusResponse {
        Log.d(TAG, "Checking YouTube token status for store: $storeUrl")
        try {
            val response = aiService.checkYouTubeTokenStatus(storeUrl)
            Log.d(TAG, "YouTube token status: hasToken=${response.hasToken}, validForAndroid=${response.validForAndroid}")
            return response
        } catch (e: Exception) {
            Log.e(TAG, "Error checking YouTube token status: ${e.message}", e)
            throw e
        }
    }

    /**
     * Save YouTube token with auth code
     */
    suspend fun saveYouTubeToken(authCode: String, storeUrl: String): YouTubeTokenResponse {
        Log.d(TAG, "Saving YouTube token with auth code for store: $storeUrl")

        if (authCode.isBlank() || storeUrl.isBlank()) {
            Log.e(TAG, "Invalid parameters for token exchange: authCode=${authCode.isBlank()}, storeUrl=${storeUrl.isBlank()}")
            return YouTubeTokenResponse(
                success = false,
                message = "Missing required parameters for token exchange",
                storeUrl = storeUrl
            )
        }

        try {
            // Make the API call to exchange the auth code for tokens
            val response = aiService.saveYouTubeToken(authCode, storeUrl)
            Log.d(TAG, "YouTube token saved: ${response.success}")
            return response
        } catch (e: Exception) {
            // Handle network/server errors
            Log.e(TAG, "Error saving YouTube token: ${e.message}", e)
            return YouTubeTokenResponse(
                success = false,
                message = e.message ?: "Network error during token exchange",
                storeUrl = storeUrl
            )
        }
    }

    suspend fun revokeYouTubeToken(storeUrl: String): YouTubeTokenRevokeResponse = withContext(Dispatchers.IO) {
        aiService.revokeYouTubeToken(storeUrl)
    }

    suspend fun refreshYouTubeToken(storeUrl: String): YouTubeTokenRefreshResponse = withContext(Dispatchers.IO) {
        aiService.refreshYouTubeToken(storeUrl)
    }

    // YouTube Videos methods

    suspend fun listYouTubeVideos(
        storeUrl: String,
        pageToken: String? = null,
        maxResults: Int = 10,
        sortBy: String = "date",
        order: String = "desc"
    ): YouTubeVideosResponse = withContext(Dispatchers.IO) {
        aiService.listYouTubeVideos(
            storeUrl = storeUrl,
            pageToken = pageToken,
            maxResults = maxResults,
            sortBy = sortBy,
            order = order
        )
    }

    suspend fun searchYouTubeVideos(
        storeUrl: String,
        query: String,
        pageToken: String? = null,
        maxResults: Int = 10
    ): YouTubeVideosResponse = withContext(Dispatchers.IO) {
        aiService.searchYouTubeVideos(
            storeUrl = storeUrl,
            query = query,
            pageToken = pageToken,
            maxResults = maxResults
        )
    }

    /**
     * Get YouTube video details by video ID
     */
    suspend fun getYouTubeVideoDetails(
        storeUrl: String,
        videoId: String
    ): YouTubeVideo = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Getting video details for videoId: $videoId, storeUrl: $storeUrl")
            aiService.getYouTubeVideoDetails(
                storeUrl = storeUrl,
                videoId = videoId
            )
        } catch (e: Exception) {
            // Log the error for debugging
            Log.e(TAG, "Error getting video details: ${e.message}", e)

            // Re-throw to let the ViewModel handle it appropriately
            throw e
        }
    }

    /**
     * Process a YouTube video with URL input
     * Extracts the video ID from the URL and calls the processVideo method
     */
    suspend fun processYouTubeVideo(
        videoId: String,
        storeUrl: String,
        autoApprove: Boolean = false
    ): ProcessingResult {
        Log.d(TAG, "Processing YouTube video with ID: $videoId, storeUrl: $storeUrl")
        
        return processVideo(
            youtubeUrl = "https://www.youtube.com/watch?v=$videoId",
            storeUrl = storeUrl,
            autoApprove = autoApprove
        )
    }

    // Social Media Connection Methods

    suspend fun getFacebookAuthUrl(): String = withContext(Dispatchers.IO) {
        aiService.getFacebookAuthUrl().url
    }

    suspend fun getInstagramAuthUrl(): String = withContext(Dispatchers.IO) {
        aiService.getInstagramAuthUrl().url
    }

    suspend fun completeYouTubeAuth(authCode: String): Boolean {
        Log.d(TAG, "Completing YouTube authentication with auth code")
        try {
            // Get the store URL from the selected site
            val storeUrl = selectedSite.get()?.url ?: throw Exception("Store URL not available")
            Log.d(TAG, "Using store URL for auth completion: $storeUrl")
            
            val response = aiService.completeYouTubeAuth(CompleteYouTubeAuthRequest(authCode), storeUrl)
            val success = response.isSuccess
            Log.d(TAG, "YouTube auth completion ${if (success) "successful" else "failed"}")
            AINetworkLogger.logResponse("YouTube Auth Completion", "Auth ${if (success) "successful" else "failed"}")
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error completing YouTube auth: ${e.message}", e)
            AINetworkLogger.logApiError("YouTube auth completion error", e)
            throw e
        }
    }

    suspend fun completeFacebookAuth(code: String) = withContext(Dispatchers.IO) {
        aiService.completeFacebookAuth(CompleteAuthRequest(code))
    }

    suspend fun completeInstagramAuth(code: String) = withContext(Dispatchers.IO) {
        aiService.completeInstagramAuth(CompleteAuthRequest(code))
    }

    suspend fun disconnectYouTube() = withContext(Dispatchers.IO) {
        aiService.disconnectYouTube()
    }

    suspend fun disconnectFacebook() = withContext(Dispatchers.IO) {
        aiService.disconnectFacebook()
    }

    suspend fun disconnectInstagram() = withContext(Dispatchers.IO) {
        aiService.disconnectInstagram()
    }

    // YouTube Videos Methods

    suspend fun getYouTubeVideos(): List<YouTubeVideo> = withContext(Dispatchers.IO) {
        val responses = aiService.getYouTubeVideos().videos
        responses.map { response ->
            YouTubeVideo(
                videoId = response.videoId,
                title = response.title,
                description = response.description,
                publishedAt = response.publishedAt.toString(),
                channelTitle = response.channelTitle,
                thumbnails = mapOf("default" to VideoThumbnail(response.thumbnailUrl, 120, 90)),
                viewCount = response.viewCount?.toInt(),
                likeCount = response.likeCount?.toInt(),
                duration = response.duration
            )
        }
    }

    // Product Methods
    suspend fun getProducts(): List<AIProduct> = withContext(Dispatchers.IO) {
        aiService.getProducts().products.map {
            AIProduct(
                id = it.id,
                title = it.title,
                description = it.description,
                price = it.price,
                imageUrl = it.imageUrl,
                videoId = it.videoId,
                videoTitle = it.videoTitle,
                videoThumbnailUrl = it.videoThumbnailUrl,
                createdAt = Date(it.createdAt),
                status = AIProductStatus.valueOf(it.status),
                categories = it.categories,
                tags = it.tags
            )
        }
    }

    suspend fun updateProductStatus(productId: String, status: AIProductStatus) = withContext(Dispatchers.IO) {
        aiService.updateProductStatus(productId, UpdateProductStatusRequest(status.name))
    }

    suspend fun generateProductFromVideo(videoId: String): AIProduct = withContext(Dispatchers.IO) {
        // This functionality has been removed from the new API
        // Instead, we should process the video and return the first product
        Log.d(TAG, "Generating product from video ID: $videoId using video processing")

        try {
            val storeUrl = selectedSite.get().url
            val youtubeUrl = "https://www.youtube.com/watch?v=$videoId"

            // Process the video to get products
            val result = processVideo(youtubeUrl, storeUrl, false)

            // Return the first product or throw an exception if none available
            if (result.products.isNotEmpty()) {
                // Convert to AIProduct
                val product = result.products.first()
                return@withContext AIProduct(
                    id = product.id,
                    title = product.name,
                    description = product.description ?: "",
                    price = product.price ?: 0.0,
                    imageUrl = product.thumbnailUrl ?: "",
                    videoId = videoId,
                    videoTitle = "", // Not available in the API response
                    videoThumbnailUrl = product.thumbnailUrl ?: "",
                    createdAt = try {
                        product.createdAt?.let { createdAtStr ->
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
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
                    categories = emptyList(), // Not available in the API response
                    tags = emptyList() // Not available in the API response
                )
            } else {
                throw Exception("No products generated from video processing")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating product from video: ${e.message}", e)
            throw e
        }
    }

    /**
     * Check if YouTube is connected for the store
     * @param storeUrl The store URL
     * @return true if YouTube is connected, false otherwise
     */
    suspend fun isYouTubeConnected(storeUrl: String): Boolean {
        Log.d(TAG, "Checking YouTube connection status for store: $storeUrl")
        try {
            val response = aiService.checkYouTubeConnection(storeUrl)
            Log.d(TAG, "YouTube connection status: ${response.isConnected}")
            AINetworkLogger.logResponse("YouTube Connection", "Connection status: ${response.isConnected}")
            return response.isConnected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking YouTube connection: ${e.message}", e)
            AINetworkLogger.logApiError("YouTube connection check error", e)
            throw e
        }
    }

    suspend fun isFacebookConnected(): Boolean = withContext(Dispatchers.IO) {
        aiService.getFacebookConnectionStatus().connected
    }

    suspend fun isInstagramConnected(): Boolean = withContext(Dispatchers.IO) {
        aiService.getInstagramConnectionStatus().connected
    }

    /**
     * Disconnect YouTube from the store
     * @param storeUrl The store URL
     * @return true if successful, false otherwise
     */
    suspend fun disconnectYouTube(storeUrl: String): Boolean {
        Log.d(TAG, "Disconnecting YouTube for store: $storeUrl")
        try {
            val response = aiService.disconnectYouTube(storeUrl)
            val success = response.isSuccess
            Log.d(TAG, "YouTube disconnect ${if (success) "successful" else "failed"}")
            AINetworkLogger.logResponse("YouTube Disconnect", "Operation ${if (success) "successful" else "failed"}")
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting YouTube: ${e.message}", e)
            AINetworkLogger.logApiError("YouTube disconnect error", e)
            throw e
        }
    }

    /**
     * Generate a SAS upload URL for Azure Blob Storage
     */
    suspend fun generateAzureUploadUrl(filename: String, container: String): GenerateUploadUrlResponse =
        aiService.generateAzureUploadUrl(GenerateUploadUrlRequest(filename, container))
}

data class ProcessVideoRequest(
    @SerializedName("youtube_url")
    val youtubeUrl: String,
    @SerializedName("store_url")
    val storeUrl: String,
    @SerializedName("auto_approve")
    val autoApprove: Boolean = false
)

data class YouTubeProcessVideoRequest(
    @SerializedName("video_id")
    val videoId: String,
    @SerializedName("store_url")
    val storeUrl: String,
    @SerializedName("auto_approve")
    val autoApprove: Boolean = false
)

data class ProcessingResult(
    val success: Boolean = false,
    @SerializedName("collection_id")
    val collectionId: String = "",
    @SerializedName("collection_url")
    val collectionUrl: String = "",
    @SerializedName("total_products")
    val totalProducts: Int = 0,
    val status: String = "",
    val products: List<ProductReviewResponse> = emptyList()
)

data class ProductReviewResponse(
    val id: String,
    val name: String,
    val price: Double?,
    val status: String,
    @SerializedName("confidence_score")
    val confidenceScore: Double,
    val description: String?,
    @SerializedName("video_clip_url")
    val videoClipUrl: String?,
    @SerializedName("thumbnail_url")
    val thumbnailUrl: String?,
    @SerializedName("timestamp_start")
    val timestampStart: Double?,
    @SerializedName("timestamp_end")
    val timestampEnd: Double?,
    @SerializedName("review_status")
    val reviewStatus: String?,
    @SerializedName("product_url")
    val productUrl: String?,
    @SerializedName("created_at")
    val createdAt: String? = null,
    val reviewer: String? = null,
    @SerializedName("review_date")
    val reviewDate: String? = null
)

data class ProductEditRequest(
    @SerializedName("youtube_url")
    val youtubeUrl: String,
    @SerializedName("store_url")
    val storeUrl: String,
    val products: List<ProductUpdate>
)

data class ProductUpdate(
    val id: String,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val status: String? = null
)

// YouTube OAuth response models

data class YouTubeAuthUrlResponseFromService(
    @SerializedName("auth_url")
    val authUrl: String,
    val message: String
)

data class YouTubeTokenResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("store_url")
    val storeUrl: String
)

data class YouTubeTokenRevokeResponse(
    val success: Boolean,
    val message: String
)

data class YouTubeTokenRefreshResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("expires_at")
    val expiresAt: String? = null
)

// YouTube Videos response models

data class YouTubeVideosResponse(
    val videos: List<YouTubeVideo>,
    @SerializedName("next_page_token")
    val nextPageToken: String? = null,
    @SerializedName("prev_page_token")
    val prevPageToken: String? = null,
    @SerializedName("total_results")
    val totalResults: Int,
    @SerializedName("results_per_page")
    val resultsPerPage: Int
)

data class YouTubeVideo(
    @SerializedName("video_id")
    val videoId: String,
    val title: String,
    val description: String = "",
    @SerializedName("published_at")
    val publishedAt: String,
    @SerializedName("channel_title")
    val channelTitle: String,
    val thumbnails: Map<String, VideoThumbnail>,
    @SerializedName("view_count")
    val viewCount: Int? = null,
    @SerializedName("like_count")
    val likeCount: Int? = null,
    val duration: String? = null,
    @SerializedName("has_captions")
    val hasCaptions: Boolean? = null,
    @SerializedName("formatted_duration")
    val formattedDuration: String? = null,
    @SerializedName("video_url")
    val videoUrl: String? = null,
    @SerializedName("embed_url")
    val embedUrl: String? = null
)

data class VideoThumbnail(
    val url: String,
    val width: Int,
    val height: Int
)
