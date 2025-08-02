package com.cataloghub.android.ui.live

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.model.Product
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.ai.AIRepository
import com.cataloghub.android.ui.products.list.ProductListRepository
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoLiveViewModel @Inject constructor(
    private val productRepository: ProductListRepository,
    private val liveSessionRepository: LiveSessionRepository,
    private val selectedSite: SelectedSite,
    private val aiRepository: AIRepository,
    savedStateHandle: SavedStateHandle
) : ScopedViewModel(savedStateHandle) {
    private val _uiState = MutableStateFlow(GoLiveScreenState())
    val uiState: StateFlow<GoLiveScreenState> = _uiState

    private val _isYouTubeConnected = MutableStateFlow(false)
    val isYouTubeConnected: StateFlow<Boolean> = _isYouTubeConnected

    fun fetchProducts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val products = productRepository.getProductList()
                _uiState.value = _uiState.value.copy(products = products, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun checkYouTubeConnectionStatus() {
        viewModelScope.launch {
            try {
                val storeUrl = selectedSite.get()?.url
                if (storeUrl == null) {
                    _isYouTubeConnected.value = false
                    return@launch
                }
                _isYouTubeConnected.value = aiRepository.isYouTubeConnected(storeUrl)
            } catch (e: Exception) {
                _isYouTubeConnected.value = false
            }
        }
    }

    fun onTitleChanged(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun onDescriptionChanged(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun onProductSelected(product: Product) {
        val selectedProducts = _uiState.value.selectedProducts.toMutableList()
        if (selectedProducts.contains(product)) {
            selectedProducts.remove(product)
        } else {
            selectedProducts.add(product)
        }
        _uiState.value = _uiState.value.copy(selectedProducts = selectedProducts)
    }

    fun onProductMoved(from: Int, to: Int) {
        val selectedProducts = _uiState.value.selectedProducts.toMutableList()
        val movedProduct = selectedProducts.removeAt(from)
        selectedProducts.add(to, movedProduct)
        _uiState.value = _uiState.value.copy(selectedProducts = selectedProducts)
    }

    fun onPlatformSelected(platform: String) {
        val platforms = _uiState.value.platforms.toMutableList()
        if (platforms.contains(platform)) {
            platforms.remove(platform)
        } else {
            platforms.add(platform)
        }
        _uiState.value = _uiState.value.copy(platforms = platforms)
    }

    fun createLiveSession() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Validation
            if (_uiState.value.title.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please enter a title for your live stream",
                    isLoading = false
                )
                return@launch
            }
            
            if (_uiState.value.selectedProducts.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please select at least one product to showcase",
                    isLoading = false
                )
                return@launch
            }
            
            if (_uiState.value.platforms.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    error = "Please select at least one platform",
                    isLoading = false
                )
                return@launch
            }
            
            try {
                val storeUrl = selectedSite.get()?.url
                if (storeUrl == null) {
                    _uiState.value = _uiState.value.copy(error = "Store URL not found", isLoading = false)
                    return@launch
                }

                val request = CreateSessionRequest(
                    store_url = storeUrl,
                    products = _uiState.value.selectedProducts.map { ProductPayload(it.remoteId.toString(), it.name) },
                    platforms = _uiState.value.platforms,
                    title = _uiState.value.title,
                    description = _uiState.value.description
                )
                val response = liveSessionRepository.createLiveSession(request)
                
                // Clear any errors and navigate to live stream
                _uiState.value = _uiState.value.copy(error = null, isLoading = false)
                triggerEvent(
                    GoLiveNavigationEvent.NavigateToLiveStream(
                        sessionId = response.session_id,
                        streamKey = response.stream_key,
                        productIds = _uiState.value.selectedProducts.map { it.remoteId }.toLongArray()
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to create live session: ${e.message ?: "Unknown error"}",
                    isLoading = false
                )
            }
        }
    }

    sealed class GoLiveNavigationEvent : MultiLiveEvent.Event() {
        data class NavigateToLiveStream(
            val sessionId: String,
            val streamKey: String,
            val productIds: LongArray
        ) : GoLiveNavigationEvent() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as NavigateToLiveStream

                if (sessionId != other.sessionId) return false
                if (streamKey != other.streamKey) return false
                if (!productIds.contentEquals(other.productIds)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = sessionId.hashCode()
                result = 31 * result + streamKey.hashCode()
                result = 31 * result + productIds.contentHashCode()
                return result
            }
        }
    }
}
