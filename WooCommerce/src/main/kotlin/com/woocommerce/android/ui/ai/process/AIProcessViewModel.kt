package com.woocommerce.android.ui.ai.process

import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.woocommerce.android.R
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.ai.AIRepository
import com.woocommerce.android.ui.ai.AINetworkLogger
import com.woocommerce.android.ui.ai.ProductReviewResponse
import com.woocommerce.android.ui.ai.ProductEditRequest
import com.woocommerce.android.ui.ai.ProductUpdate
import com.woocommerce.android.util.WooLog
import com.woocommerce.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIProcessViewModel @Inject constructor(
    private val selectedSite: SelectedSite,
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _viewState = MutableLiveData(ViewState())
    val viewState: LiveData<ViewState> = _viewState

    private val _event = MultiLiveEvent<Event>()
    val event: LiveData<Event> = _event

    private val _processingState = MutableLiveData<ProcessingState>()
    val processingState: LiveData<ProcessingState> = _processingState

    private val _products = MutableLiveData<List<ProductReviewResponse>>()
    val products: LiveData<List<ProductReviewResponse>> = _products

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        AINetworkLogger.logRequest("AIProcessViewModel", "Initialized")
        WooLog.d(WooLog.T.AI, "AIProcessViewModel initialized")
        _processingState.value = ProcessingState.IDLE
    }

    fun processVideo(youtubeUrl: String, autoApprove: Boolean = false) {
        AINetworkLogger.logRequest(
            "Process Video Request",
            "URL: $youtubeUrl, AutoApprove: $autoApprove, Store URL: ${selectedSite.get().url}"
        )
        WooLog.d(WooLog.T.AI, "Processing video: $youtubeUrl (autoApprove: $autoApprove), store URL: ${selectedSite.get().url}")

        if (!isValidYoutubeUrl(youtubeUrl)) {
            _event.value = Event.ShowError(R.string.ai_error_invalid_url)
            return
        }

        viewModelScope.launch {
            _viewState.value = ViewState(isLoading = true)

            try {
                _processingState.value = ProcessingState.PROCESSING
                AINetworkLogger.logRequest("Processing State", "PROCESSING")
                WooLog.d(WooLog.T.AI, "Processing state changed to PROCESSING")

                val result = aiRepository.processVideo(
                    youtubeUrl = youtubeUrl,
                    storeUrl = selectedSite.get().url,
                    autoApprove = autoApprove
                )

                val responseLog = """
                    Success: ${result.success}
                    Collection ID: ${result.collectionId}
                    Total Products: ${result.totalProducts}
                    Status: ${result.status}
                    Products Count: ${result.products.size}
                    """.trimIndent()

                AINetworkLogger.logResponse("Process Video Response", responseLog)
                WooLog.d(WooLog.T.AI, "Process Video Response: $responseLog")

                if (result.success) {
                    _products.value = result.products
                    _processingState.value = ProcessingState.COMPLETED
                    AINetworkLogger.logResponse("Processing State", "COMPLETED")
                    WooLog.d(WooLog.T.AI, "Processing completed successfully")
                    _event.value = Event.NavigateToReview
                } else {
                    _error.value = "Processing failed: ${result.status}"
                    _processingState.value = ProcessingState.ERROR
                    AINetworkLogger.logError("Processing failed", Exception(result.status))
                    WooLog.e(WooLog.T.AI, "Video processing failed: ${result.status}")
                }
            } catch (e: Exception) {
                AINetworkLogger.logError("Process Video Error", e)
                _error.value = e.message ?: "Unknown error occurred"
                _processingState.value = ProcessingState.ERROR
                WooLog.e(WooLog.T.AI, "Process Video Error", e)
            } finally {
                _viewState.value = ViewState(isLoading = false)
            }
        }
    }

    fun getProducts(youtubeUrl: String) {
        AINetworkLogger.logRequest("Get Products Request", "URL: $youtubeUrl")
        WooLog.d(WooLog.T.AI, "Getting products for URL: $youtubeUrl")

        viewModelScope.launch {
            try {
                _processingState.value = ProcessingState.LOADING
                AINetworkLogger.logRequest("Processing State", "LOADING")
                WooLog.d(WooLog.T.AI, "Processing state changed to LOADING")

                val products = aiRepository.getProducts(youtubeUrl)

                AINetworkLogger.logResponse(
                    "Get Products Response",
                    "Retrieved ${products.size} products"
                )
                WooLog.d(WooLog.T.AI, "Retrieved ${products.size} products")

                _products.value = products
                _processingState.value = ProcessingState.COMPLETED
                AINetworkLogger.logResponse("Processing State", "COMPLETED")
                WooLog.d(WooLog.T.AI, "Products retrieval completed")
            } catch (e: Exception) {
                AINetworkLogger.logError("Get Products Error", e)
                _error.value = e.message ?: "Failed to retrieve products"
                _processingState.value = ProcessingState.ERROR
                WooLog.e(WooLog.T.AI, "Get Products Error", e)
            }
        }
    }

    fun editProducts(youtubeUrl: String, updates: List<ProductUpdate>) {
        val requestLog = """
            URL: $youtubeUrl
            Updates Count: ${updates.size}
            Updates: ${updates.map { it.id to it.status }}
            """.trimIndent()

        AINetworkLogger.logRequest("Edit Products Request", requestLog)
        WooLog.d(WooLog.T.AI, "Edit Products Request: $requestLog")

        viewModelScope.launch {
            try {
                _processingState.value = ProcessingState.UPDATING
                AINetworkLogger.logRequest("Processing State", "UPDATING")
                WooLog.d(WooLog.T.AI, "Processing state changed to UPDATING")

                val request = ProductEditRequest(
                    youtubeUrl = youtubeUrl,
                    storeUrl = selectedSite.get().url,
                    products = updates
                )

                val updatedProducts = aiRepository.editProducts(request)

                AINetworkLogger.logResponse(
                    "Edit Products Response",
                    "Updated ${updatedProducts.size} products"
                )
                WooLog.d(WooLog.T.AI, "Updated ${updatedProducts.size} products")

                _products.value = updatedProducts
                _processingState.value = ProcessingState.COMPLETED
                AINetworkLogger.logResponse("Processing State", "COMPLETED")
                WooLog.d(WooLog.T.AI, "Products update completed")
            } catch (e: Exception) {
                AINetworkLogger.logError("Edit Products Error", e)
                _error.value = e.message ?: "Failed to update products"
                _processingState.value = ProcessingState.ERROR
                WooLog.e(WooLog.T.AI, "Edit Products Error", e)
            }
        }
    }

    fun resetError() {
        AINetworkLogger.logRequest("Reset Error", "Clearing error state")
        WooLog.d(WooLog.T.AI, "Error state reset")
        _error.value = null
    }

    private fun isValidYoutubeUrl(url: String): Boolean {
        return url.isNotBlank() && (
            url.startsWith("https://www.youtube.com/watch?v=") ||
            url.startsWith("https://youtu.be/")
        )
    }

    data class ViewState(
        val isLoading: Boolean = false
    )

    sealed class Event : MultiLiveEvent.Event(false) {
        data class ShowError(@StringRes val message: Int) : Event()
        object NavigateToReview : Event()
    }

    enum class ProcessingState {
        IDLE,
        PROCESSING,
        LOADING,
        UPDATING,
        COMPLETED,
        ERROR
    }
}
