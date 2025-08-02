package com.cataloghub.android.ui.live

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.model.Product
import com.cataloghub.android.ui.products.list.ProductListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LiveStreamViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val productRepository: ProductListRepository
) : ViewModel() {
    private val sessionId: String = savedStateHandle.get<String>("sessionId") ?: ""
    val streamKey: String = savedStateHandle.get<String>("streamKey") ?: ""
    private val productIds: LongArray = savedStateHandle.get<LongArray>("productIds") ?: longArrayOf()

    private var rtmpStreamer: RtmpStreamer? = null

    private val _uiState = MutableStateFlow(LiveStreamUiState())
    val uiState: StateFlow<LiveStreamUiState> = _uiState

    private val _selectedProducts = MutableStateFlow<List<Product>>(emptyList())
    val selectedProducts: StateFlow<List<Product>> = _selectedProducts

    init {
        loadProducts()
    }

    private fun loadProducts() {
        if (productIds.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    val products = productIds.mapNotNull { id ->
                        productRepository.getProduct(id)
                    }
                    _selectedProducts.value = products
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(error = e.message)
                }
            }
        }
    }

    fun onStreamStarted(rtmpStreamer: RtmpStreamer) {
        this.rtmpStreamer = rtmpStreamer
        rtmpStreamer.startStream()
        _uiState.value = _uiState.value.copy(isStreaming = true)
    }

    fun onStreamStopped() {
        rtmpStreamer?.stopStream()
        _uiState.value = _uiState.value.copy(isStreaming = false)
    }

    fun onPreviewStarted() {
        rtmpStreamer?.startPreview()
    }

    fun onPreviewStopped() {
        rtmpStreamer?.stopPreview()
    }

    fun setRtmpStreamer(streamer: RtmpStreamer) {
        this.rtmpStreamer = streamer
    }

    data class LiveStreamUiState(
        val isStreaming: Boolean = false,
        val error: String? = null
    )
}