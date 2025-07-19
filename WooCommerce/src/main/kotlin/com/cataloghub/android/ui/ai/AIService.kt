package com.cataloghub.android.ui.ai

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path
import com.google.gson.annotations.SerializedName

interface AIService {
    /**
     * Process a YouTube video to extract products
     * OpenAPI path: /api/v1/youtube-videos/process
     */
    @POST("api/v1/youtube-videos/process")
    suspend fun processVideo(@Body request: YouTubeProcessVideoRequest): ProcessingResult

    @GET("api/v1/collections/by-youtube-url")
    suspend fun getProducts(
        @Query("youtube_url") youtubeUrl: String,
        @Query("store_url") storeUrl: String
    ): List<ProductReviewResponse>

    /**
     * Get products by YouTube URL (No Auth)
     * OpenAPI path: /api/v1/youtube-videos/by-youtube-url
     */
    @GET("api/v1/youtube-videos/by-youtube-url")
    suspend fun getProductsByYoutubeUrl(
        @Query("youtube_url") youtubeUrl: String,
        @Query("store_url") storeUrl: String,
        @Query("status") status: String? = null
    ): List<ProductReviewResponse>

    /**
     * Edit product details (No Auth)
     * OpenAPI path: /api/v1/youtube-videos/edit-product
     */
    @POST("api/v1/youtube-videos/edit-product")
    suspend fun editProducts(@Body request: ProductEditRequest): List<ProductReviewResponse>

    // Previous version - keeping for reference but marking as deprecated
    /**
     * @deprecated Use the No Auth version instead
     */
    @POST("api/v1/collections/edit-product")
    suspend fun editProductsWithAuth(@Body request: ProductEditRequest): List<ProductReviewResponse>

    // YouTube OAuth endpoints - Update to match OpenAPI spec exactly

    /**
     * Get YouTube OAuth authorization URL (No Auth)
     * OpenAPI path: /api/v1/youtube-public/auth-url
     */
    @GET("api/v1/youtube-public/auth-url")
    suspend fun getYouTubeAuthUrl(
        @Query("store_url") storeUrl: String,
        @Query("platform") platform: String = "android"
    ): YouTubeAuthUrlResponse

    /**
     * Save YouTube OAuth token (No Auth)
     * OpenAPI path: /api/v1/youtube-public/save-token
     */
    @POST("api/v1/youtube-public/save-token")
    suspend fun saveYouTubeToken(
        @Query("auth_code") authCode: String,
        @Query("store_url") storeUrl: String,
        @Query("platform") platform: String = "android"
    ): YouTubeTokenResponse

    /**
     * Check YouTube OAuth token status (No Auth)
     * OpenAPI path: /api/v1/youtube-public/token-status
     */
    @GET("api/v1/youtube-public/token-status")
    suspend fun checkYouTubeTokenStatus(
        @Query("store_url") storeUrl: String
    ): YouTubeTokenStatusResponse

    @DELETE("api/v1/youtube-public/revoke-token")
    suspend fun revokeYouTubeToken(
        @Query("store_url") storeUrl: String
    ): YouTubeTokenRevokeResponse

    @POST("api/v1/youtube-public/refresh-token")
    suspend fun refreshYouTubeToken(
        @Query("store_url") storeUrl: String
    ): YouTubeTokenRefreshResponse

    // YouTube Videos endpoints

    @GET("api/v1/youtube-videos/list")
    suspend fun listYouTubeVideos(
        @Query("store_url") storeUrl: String,
        @Query("page_token") pageToken: String? = null,
        @Query("max_results") maxResults: Int = 10,
        @Query("sort_by") sortBy: String = "date",
        @Query("order") order: String = "desc"
    ): YouTubeVideosResponse

    @GET("api/v1/youtube-videos/search")
    suspend fun searchYouTubeVideos(
        @Query("store_url") storeUrl: String,
        @Query("query") query: String,
        @Query("page_token") pageToken: String? = null,
        @Query("max_results") maxResults: Int = 10
    ): YouTubeVideosResponse

    @GET("api/v1/youtube-videos/video-details")
    suspend fun getYouTubeVideoDetails(
        @Query("store_url") storeUrl: String,
        @Query("video_id") videoId: String
    ): YouTubeVideo

    // Social Media Connection Endpoints
    @GET("ai/youtube/auth-url")
    suspend fun getYouTubeAuthUrl(): AuthUrlResponse

    @GET("ai/facebook/auth-url")
    suspend fun getFacebookAuthUrl(): AuthUrlResponse

    @GET("ai/instagram/auth-url")
    suspend fun getInstagramAuthUrl(): AuthUrlResponse

    @POST("ai/youtube/complete-auth")
    suspend fun completeYouTubeAuth(@Body request: CompleteAuthRequest)

    @POST("ai/facebook/complete-auth")
    suspend fun completeFacebookAuth(@Body request: CompleteAuthRequest)

    @POST("ai/instagram/complete-auth")
    suspend fun completeInstagramAuth(@Body request: CompleteAuthRequest)

    @POST("ai/youtube/disconnect")
    suspend fun disconnectYouTube()

    @POST("ai/facebook/disconnect")
    suspend fun disconnectFacebook()

    @POST("ai/instagram/disconnect")
    suspend fun disconnectInstagram()

    @GET("ai/youtube/connection-status")
    suspend fun getYouTubeConnectionStatus(): ConnectionStatusResponse

    @GET("ai/facebook/connection-status")
    suspend fun getFacebookConnectionStatus(): ConnectionStatusResponse

    @GET("ai/instagram/connection-status")
    suspend fun getInstagramConnectionStatus(): ConnectionStatusResponse

    // YouTube Videos Endpoints
    @GET("ai/youtube/videos")
    suspend fun getYouTubeVideos(): APIYouTubeVideosResponse

    @GET("ai/youtube/videos/{videoId}")
    suspend fun getYouTubeVideoDetails(@Path("videoId") videoId: String): APIYouTubeVideoResponse

    // Product Endpoints
    @GET("ai/products")
    suspend fun getProducts(): APIProductsResponse

    @POST("ai/products/{productId}/status")
    suspend fun updateProductStatus(
        @Path("productId") productId: String,
        @Body request: UpdateProductStatusRequest
    )

    /**
     * Check YouTube connection status
     */
    @GET("ai/youtube/connection")
    suspend fun checkYouTubeConnection(@Query("store_url") storeUrl: String): YouTubeConnectionResponse

    /**
     * Complete YouTube authorization with auth code
     */
    @POST("ai/youtube/complete-auth")
    suspend fun completeYouTubeAuth(@Body request: CompleteYouTubeAuthRequest, @Query("store_url") storeUrl: String): YouTubeAuthResponse

    /**
     * Disconnect YouTube
     */
    @POST("ai/youtube/disconnect")
    suspend fun disconnectYouTube(@Query("store_url") storeUrl: String): YouTubeDisconnectResponse

    /**
     * Process an Azure blob video/audio file to extract products and create them in WooCommerce.
     * OpenAPI path: /azure-videos/process
     */
    @POST("azure-videos/process")
    suspend fun processAzureVideo(@Body request: ProcessAzureRequest): ProcessingResult

    /**
     * Generate a SAS upload URL for Azure Blob Storage
     * POST /azure-videos/generate-upload-url
     */
    @POST("azure-videos/generate-upload-url")
    suspend fun generateAzureUploadUrl(@Body request: GenerateUploadUrlRequest): GenerateUploadUrlResponse
}

// Add these data classes for the new API endpoints
data class AuthUrlResponse(
    val url: String
)

data class CompleteAuthRequest(
    val code: String
)

data class ConnectionStatusResponse(
    val connected: Boolean
)

data class APIYouTubeVideosResponse(
    val videos: List<APIYouTubeVideoResponse>,
    @SerializedName("next_page_token")
    val nextPageToken: String? = null,
    @SerializedName("prev_page_token")
    val prevPageToken: String? = null,
    @SerializedName("total_results")
    val totalResults: Int = 0,
    @SerializedName("results_per_page")
    val resultsPerPage: Int = 0
)

data class APIYouTubeVideoResponse(
    val id: String,
    val videoId: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val publishedAt: Long,
    val viewCount: Long?,
    val likeCount: Long?,
    val commentCount: Long?,
    val duration: String?,
    val channelTitle: String
)

data class APIProductsResponse(
    val products: List<APIProductResponse>
)

data class APIProductResponse(
    val id: String,
    val title: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val videoId: String,
    val videoTitle: String,
    val videoThumbnailUrl: String,
    val createdAt: Long,
    val status: String,
    val categories: List<String>,
    val tags: List<String>
)

data class UpdateProductStatusRequest(
    val status: String
)

data class GenerateProductRequest(
    val videoId: String
)

/**
 * Models for YouTube connection responses
 */
data class YouTubeConnectionResponse(
    val isConnected: Boolean,
    val message: String
)

data class YouTubeAuthUrlResponse(
    @SerializedName("auth_url")
    val authUrl: String?,
    val message: String?,
    val success: Boolean = false
) {
    override fun toString(): String {
        return "YouTubeAuthUrlResponse(authUrl=${if (authUrl?.length ?: 0 > 10) authUrl?.substring(0, 10) + "..." else authUrl}, " +
               "message=$message, success=$success)"
    }
}

data class CompleteYouTubeAuthRequest(
    val authCode: String
)

data class YouTubeAuthResponse(
    val isSuccess: Boolean,
    val message: String
)

data class YouTubeDisconnectResponse(
    val isSuccess: Boolean,
    val message: String
)

data class YouTubeTokenStatusResponse(
    @SerializedName("has_token")
    val hasToken: Boolean,
    val message: String?,
    @SerializedName("expires_at")
    val expiresAt: String?,
    val scopes: List<String>?,
    @SerializedName("valid_for_android")
    val validForAndroid: Boolean?
)

data class ProcessAzureRequest(
    @SerializedName("azure_url")
    val azureUrl: String,
    @SerializedName("store_url")
    val storeUrl: String,
    @SerializedName("auto_approve")
    val autoApprove: Boolean = false,
    @SerializedName("collection_name")
    val collectionName: String
)

// Top-level data classes for Azure upload URL generation

data class GenerateUploadUrlRequest(
    @SerializedName("filename") val filename: String,
    @SerializedName("container") val container: String
)

data class GenerateUploadUrlResponse(
    @SerializedName("upload_url") val uploadUrl: String,
    @SerializedName("blob_name") val blobName: String
)
