package com.cataloghub.android.ui.ai

import com.google.gson.annotations.SerializedName
import com.cataloghub.android.tools.SelectedSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val aiService: AIService,
    val selectedSite: SelectedSite
) {
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
}

data class ProcessVideoRequest(
    @SerializedName("youtube_url")
    val youtubeUrl: String,
    @SerializedName("store_url")
    val storeUrl: String,
    @SerializedName("auto_approve")
    val autoApprove: Boolean = false
)

data class ProcessingResult(
    val success: Boolean,
    @SerializedName("collection_id")
    val collectionId: String,
    @SerializedName("collection_url")
    val collectionUrl: String,
    @SerializedName("total_products")
    val totalProducts: Int,
    val status: String,
    val products: List<ProductReviewResponse>
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
