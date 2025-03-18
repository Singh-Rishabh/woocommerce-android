package com.cataloghub.android.ui.ai

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.Path

interface AIService {
    @POST("process-video/")
    suspend fun processVideo(@Body request: ProcessVideoRequest): ProcessingResult

    @GET("api/v1/collections/by-youtube-url")
    suspend fun getProducts(
        @Query("youtube_url") youtubeUrl: String,
        @Query("store_url") storeUrl: String
    ): List<ProductReviewResponse>

    @POST("api/v1/collections/edit-product")
    suspend fun editProducts(@Body request: ProductEditRequest): List<ProductReviewResponse>
    
    // YouTube OAuth endpoints
    
    @GET("api/v1/youtube-public/auth-url")
    suspend fun getYouTubeAuthUrl(
        @Query("store_url") storeUrl: String
    ): YouTubeAuthResponse
    
    @POST("api/v1/youtube-public/save-token")
    suspend fun saveYouTubeToken(
        @Query("auth_code") authCode: String,
        @Query("store_url") storeUrl: String
    ): YouTubeTokenResponse
    
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
    
    @POST("api/v1/youtube-videos/process")
    suspend fun processYouTubeVideo(
        @Body request: YouTubeProcessVideoRequest
    ): ProcessingResult

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
    suspend fun getYouTubeVideos(): YouTubeVideosResponse
    
    @GET("ai/youtube/videos/{videoId}")
    suspend fun getYouTubeVideoDetails(@Path("videoId") videoId: String): YouTubeVideoResponse
    
    // Product Endpoints
    @GET("ai/products")
    suspend fun getProducts(): ProductsResponse
    
    @POST("ai/products/{productId}/status")
    suspend fun updateProductStatus(
        @Path("productId") productId: String,
        @Body request: UpdateProductStatusRequest
    )
    
    @POST("ai/products/generate")
    suspend fun generateProductFromVideo(@Body request: GenerateProductRequest): ProductResponse
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

data class YouTubeVideosResponse(
    val videos: List<YouTubeVideoResponse>
)

data class YouTubeVideoResponse(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val publishedAt: Long,
    val viewCount: Long,
    val likeCount: Long,
    val commentCount: Long,
    val duration: String,
    val channelTitle: String
)

data class ProductsResponse(
    val products: List<ProductResponse>
)

data class ProductResponse(
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
