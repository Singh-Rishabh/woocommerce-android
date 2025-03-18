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
                val video = repository.getYouTubeVideoDetails(videoId)
                _videoDetails.value = video
                youtubeUrl = "https://www.youtube.com/watch?v=${video.id}"
                
                AINetworkLogger.logResponse(
                    "YouTube Video Details",
                    "ID: ${video.id}, Title: ${video.title}"
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
                val products = repository.getProducts()
                val videoProducts = products.filter { it.videoId == videoId }
                
                if (videoProducts.isNotEmpty()) {
                    _productsExist.value = true
                    categorizeProducts(videoProducts)
                    
                    AINetworkLogger.logResponse(
                        "Existing Products",
                        "Found ${videoProducts.size} products for video $videoId"
                    )
                    WooLog.d(WooLog.T.AI, "Found ${videoProducts.size} existing products for video $videoId")
                } else {
                    _productsExist.value = false
                    AINetworkLogger.logResponse("Existing Products", "No products found for video $videoId")
                    WooLog.d(WooLog.T.AI, "No existing products found for video $videoId")
                }
            } catch (e: Exception) {
                AINetworkLogger.logError("Check Existing Products Error", e)
                WooLog.e(WooLog.T.AI, "Failed to check existing products", e)
                _productsExist.value = false
            }
        }
    }
    
    fun processVideo(storeUrl: String, videoId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            
            try {
                val product = repository.generateProductFromVideo(videoId)
                val products = listOf(product)
                categorizeProducts(products)
                _processingComplete.value = true
                
                AINetworkLogger.logResponse(
                    "Process Video Success",
                    "Processed product for video $videoId"
                )
                WooLog.d(
                    WooLog.T.AI,
                    "Successfully processed video $videoId, generated product"
                )
            } catch (e: Exception) {
                AINetworkLogger.logError("Process Video Error", e)
                WooLog.e(WooLog.T.AI, "Error processing video", e)
                _errorMessage.value = R.string.ai_error_processing
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
} 