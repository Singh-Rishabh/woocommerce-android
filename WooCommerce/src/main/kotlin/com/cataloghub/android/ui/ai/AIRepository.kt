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

    suspend fun processVideo(
        youtubeUrl: String,
        storeUrl: String,
        autoApprove: Boolean
    ): ProcessingResult = withContext(Dispatchers.IO) {
        aiService.processVideo(
            ProcessVideoRequest(
                youtubeUrl = youtubeUrl,
                storeUrl = storeUrl,
                autoApprove = autoApprove
            )
        )
    }

    suspend fun getProducts(youtubeUrl: String): List<ProductReviewResponse> = withContext(Dispatchers.IO) {
        aiService.getProducts(
            youtubeUrl = youtubeUrl,
            storeUrl = selectedSite.get().url
        )
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
            AINetworkLogger.logResponse(TAG, "Received YouTube auth URL: $authUrl")
            return authUrl
        } catch (e: Exception) {
            Log.e(TAG, "Error getting YouTube auth URL: ${e.message}", e)
            Log.e(TAG, "Error details: ${e.stackTraceToString()}")
            AINetworkLogger.logError(TAG, e)
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

    suspend fun getYouTubeVideoDetails(
        storeUrl: String,
        videoId: String
    ): YouTubeVideo = withContext(Dispatchers.IO) {
        aiService.getYouTubeVideoDetails(
            storeUrl = storeUrl,
            videoId = videoId
        )
    }

    suspend fun processYouTubeVideo(
        videoId: String,
        storeUrl: String,
        autoApprove: Boolean = false
    ): ProcessingResult = withContext(Dispatchers.IO) {
        aiService.processYouTubeVideo(
            YouTubeProcessVideoRequest(
                videoId = videoId,
                storeUrl = storeUrl,
                autoApprove = autoApprove
            )
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
            val response = aiService.completeYouTubeAuth(CompleteYouTubeAuthRequest(authCode))
            val success = response.isSuccess
            Log.d(TAG, "YouTube auth completion ${if (success) "successful" else "failed"}")
            AINetworkLogger.logResponse(TAG, "YouTube auth completion ${if (success) "successful" else "failed"}")
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error completing YouTube auth: ${e.message}", e)
            AINetworkLogger.logError(TAG, e)
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

    suspend fun getYouTubeVideoDetails(videoId: String): YouTubeVideo = withContext(Dispatchers.IO) {
        val response = aiService.getYouTubeVideoDetails(videoId)
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
        val response = aiService.generateProductFromVideo(GenerateProductRequest(videoId))
        AIProduct(
            id = response.id,
            title = response.title,
            description = response.description,
            price = response.price,
            imageUrl = response.imageUrl,
            videoId = response.videoId,
            videoTitle = response.videoTitle,
            videoThumbnailUrl = response.videoThumbnailUrl,
            createdAt = Date(response.createdAt),
            status = AIProductStatus.valueOf(response.status),
            categories = response.categories,
            tags = response.tags
        )
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
            AINetworkLogger.logResponse(TAG, "YouTube connection status: ${response.isConnected}")
            return response.isConnected
        } catch (e: Exception) {
            Log.e(TAG, "Error checking YouTube connection: ${e.message}", e)
            AINetworkLogger.logError(TAG, e)
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
            AINetworkLogger.logResponse(TAG, "YouTube disconnect ${if (success) "successful" else "failed"}")
            return success
        } catch (e: Exception) {
            Log.e(TAG, "Error disconnecting YouTube: ${e.message}", e)
            AINetworkLogger.logError(TAG, e)
            throw e
        }
    }
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
