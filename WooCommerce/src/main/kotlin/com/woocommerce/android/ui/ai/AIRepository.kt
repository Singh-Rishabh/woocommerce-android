package com.woocommerce.android.ui.ai

import com.woocommerce.android.tools.SelectedSite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIRepository @Inject constructor(
    private val aiService: AIService,
    private val selectedSite: SelectedSite
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
    val youtubeUrl: String,
    val storeUrl: String,
    val autoApprove: Boolean
)

data class ProcessingResult(
    val success: Boolean,
    val collectionId: String,
    val collectionUrl: String,
    val totalProducts: Int,
    val status: String,
    val products: List<ProductReviewResponse>
)

data class ProductReviewResponse(
    val id: String,
    val name: String,
    val price: Double?,
    val status: String,
    val confidenceScore: Double,
    val description: String?,
    val videoClipUrl: String?,
    val thumbnailUrl: String?,
    val timestampStart: Double?,
    val timestampEnd: Double?,
    val reviewStatus: String?,
    val productUrl: String?
)

data class ProductEditRequest(
    val youtubeUrl: String,
    val storeUrl: String,
    val products: List<ProductUpdate>
)

data class ProductUpdate(
    val id: String,
    val name: String?,
    val description: String?,
    val price: Double?,
    val status: String?
) 