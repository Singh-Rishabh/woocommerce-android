package com.cataloghub.android.ui.ai

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

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
}
