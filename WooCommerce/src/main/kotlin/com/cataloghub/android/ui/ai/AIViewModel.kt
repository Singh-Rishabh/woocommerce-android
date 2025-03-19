package com.cataloghub.android.ui.ai

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.R
import com.cataloghub.android.model.AIProduct
import com.cataloghub.android.model.AIProductStatus
import com.cataloghub.android.util.WooLog
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val repository: AIRepository
) : ViewModel() {

    private val _isYouTubeConnected = MutableLiveData<Boolean>()
    val isYouTubeConnected: LiveData<Boolean> = _isYouTubeConnected

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<Int?>()
    val errorMessage: LiveData<Int?> = _errorMessage

    private val _authUrl = MutableLiveData<String?>()
    val authUrl: LiveData<String?> = _authUrl

    // Social Media Connection States
    private val _youtubeConnectionState = MutableLiveData<ConnectionState>()
    val youtubeConnectionState: LiveData<ConnectionState> = _youtubeConnectionState
    
    private val _facebookConnectionState = MutableLiveData<ConnectionState>()
    val facebookConnectionState: LiveData<ConnectionState> = _facebookConnectionState
    
    private val _instagramConnectionState = MutableLiveData<ConnectionState>()
    val instagramConnectionState: LiveData<ConnectionState> = _instagramConnectionState
    
    // YouTube Videos
    private val _youtubeVideos = MutableLiveData<List<YouTubeVideo>>()
    val youtubeVideos: LiveData<List<YouTubeVideo>> = _youtubeVideos
    
    private val _videoSortOrder = MutableLiveData(SortOrder.DATE_DESC)
    val videoSortOrder: LiveData<SortOrder> = _videoSortOrder
    
    private val _isLoadingVideos = MutableLiveData(false)
    val isLoadingVideos: LiveData<Boolean> = _isLoadingVideos
    
    // Products
    private val _pendingProducts = MutableLiveData<List<AIProduct>>()
    val pendingProducts: LiveData<List<AIProduct>> = _pendingProducts
    
    private val _approvedProducts = MutableLiveData<List<AIProduct>>()
    val approvedProducts: LiveData<List<AIProduct>> = _approvedProducts
    
    private val _rejectedProducts = MutableLiveData<List<AIProduct>>()
    val rejectedProducts: LiveData<List<AIProduct>> = _rejectedProducts

    private val _event = MultiLiveEvent<Event>()
    val event: LiveData<Event> = _event

    init {
        AINetworkLogger.logRequest("AIViewModel", "Initialized")
        WooLog.d(WooLog.T.AI, "AIViewModel initialized")

        // Check connection states
        checkYouTubeConnection()
        checkFacebookConnection()
        checkInstagramConnection()
    }

    fun checkYouTubeConnectionStatus(storeUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val status = repository.checkYouTubeTokenStatus(storeUrl)
                _isYouTubeConnected.value = status.hasToken
                AINetworkLogger.logResponse("YouTube Connection Status", "Connected: ${status.hasToken}")
                WooLog.d(WooLog.T.AI, "YouTube connection status: ${status.hasToken}")
            } catch (e: Exception) {
                AINetworkLogger.logError("YouTube Connection Status Error", e)
                WooLog.e(WooLog.T.AI, "Failed to check YouTube connection status", e)
                _errorMessage.value = R.string.ai_error_connection_check
                _isYouTubeConnected.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getYouTubeAuthUrl(storeUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.getYouTubeAuthUrl(storeUrl)
                _authUrl.value = response.authUrl
                AINetworkLogger.logResponse("YouTube Auth URL", "URL: ${response.authUrl}")
                WooLog.d(WooLog.T.AI, "Got YouTube auth URL: ${response.authUrl}")
            } catch (e: Exception) {
                AINetworkLogger.logError("YouTube Auth URL Error", e)
                WooLog.e(WooLog.T.AI, "Failed to get YouTube auth URL", e)
                _errorMessage.value = R.string.ai_error_auth_url
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveYouTubeToken(authCode: String, storeUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.saveYouTubeToken(authCode, storeUrl)
                if (response.success) {
                    _isYouTubeConnected.value = true
                    AINetworkLogger.logResponse("YouTube Token Saved", "Success: ${response.success}")
                    WooLog.d(WooLog.T.AI, "YouTube token saved successfully")
                } else {
                    _errorMessage.value = R.string.ai_error_token_save
                    AINetworkLogger.logError("YouTube Token Save Failed", Exception(response.message))
                    WooLog.e(WooLog.T.AI, "Failed to save YouTube token: ${response.message}")
                }
            } catch (e: Exception) {
                AINetworkLogger.logError("YouTube Token Save Error", e)
                WooLog.e(WooLog.T.AI, "Error saving YouTube token", e)
                _errorMessage.value = R.string.ai_error_token_save
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun revokeYouTubeToken(storeUrl: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.revokeYouTubeToken(storeUrl)
                _isYouTubeConnected.value = false
                AINetworkLogger.logResponse("YouTube Token Revoked", "Success")
                WooLog.d(WooLog.T.AI, "YouTube token revoked successfully")
            } catch (e: Exception) {
                AINetworkLogger.logError("YouTube Token Revoke Error", e)
                WooLog.e(WooLog.T.AI, "Error revoking YouTube token", e)
                _errorMessage.value = R.string.ai_error_token_revoke
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun errorMessageShown() {
        _errorMessage.value = null
    }

    fun authUrlOpened() {
        _authUrl.value = null
    }

    // Social Media Connection Methods
    fun connectYouTube() {
        viewModelScope.launch {
            _youtubeConnectionState.value = ConnectionState.CONNECTING
            try {
                val authUrl = repository.getYouTubeAuthUrl()
                _event.value = Event.NavigateToWebView(authUrl)
            } catch (e: Exception) {
                _youtubeConnectionState.value = ConnectionState.DISCONNECTED
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to connect to YouTube")
            }
        }
    }
    
    fun connectFacebook() {
        viewModelScope.launch {
            _facebookConnectionState.value = ConnectionState.CONNECTING
            try {
                val authUrl = repository.getFacebookAuthUrl()
                _event.value = Event.NavigateToWebView(authUrl)
            } catch (e: Exception) {
                _facebookConnectionState.value = ConnectionState.DISCONNECTED
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to connect to Facebook")
            }
        }
    }
    
    fun connectInstagram() {
        viewModelScope.launch {
            _instagramConnectionState.value = ConnectionState.CONNECTING
            try {
                val authUrl = repository.getInstagramAuthUrl()
                _event.value = Event.NavigateToWebView(authUrl)
            } catch (e: Exception) {
                _instagramConnectionState.value = ConnectionState.DISCONNECTED
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to connect to Instagram")
            }
        }
    }
    
    fun disconnectYouTube() {
        viewModelScope.launch {
            try {
                repository.disconnectYouTube()
                _youtubeConnectionState.value = ConnectionState.DISCONNECTED
                _event.value = Event.ShowSnackbar("YouTube disconnected successfully")
            } catch (e: Exception) {
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to disconnect YouTube")
            }
        }
    }
    
    fun disconnectFacebook() {
        viewModelScope.launch {
            try {
                repository.disconnectFacebook()
                _facebookConnectionState.value = ConnectionState.DISCONNECTED
                _event.value = Event.ShowSnackbar("Facebook disconnected successfully")
            } catch (e: Exception) {
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to disconnect Facebook")
            }
        }
    }
    
    fun disconnectInstagram() {
        viewModelScope.launch {
            try {
                repository.disconnectInstagram()
                _instagramConnectionState.value = ConnectionState.DISCONNECTED
                _event.value = Event.ShowSnackbar("Instagram disconnected successfully")
            } catch (e: Exception) {
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to disconnect Instagram")
            }
        }
    }
    
    private fun checkYouTubeConnection() {
        viewModelScope.launch {
            try {
                val isConnected = repository.isYouTubeConnected()
                _youtubeConnectionState.value = if (isConnected) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED
            } catch (e: Exception) {
                _youtubeConnectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }
    
    private fun checkFacebookConnection() {
        viewModelScope.launch {
            try {
                val isConnected = repository.isFacebookConnected()
                _facebookConnectionState.value = if (isConnected) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED
            } catch (e: Exception) {
                _facebookConnectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }
    
    private fun checkInstagramConnection() {
        viewModelScope.launch {
            try {
                val isConnected = repository.isInstagramConnected()
                _instagramConnectionState.value = if (isConnected) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED
            } catch (e: Exception) {
                _instagramConnectionState.value = ConnectionState.DISCONNECTED
            }
        }
    }
    
    // OAuth Callback Handling
    fun handleOAuthCallback(url: String) {
        viewModelScope.launch {
            try {
                when {
                    url.contains("youtube_callback") -> {
                        val code = extractCodeFromUrl(url)
                        repository.completeYouTubeAuth(code)
                        _youtubeConnectionState.value = ConnectionState.CONNECTED
                        _event.value = Event.ShowSnackbar("YouTube connected successfully")
                    }
                    url.contains("facebook_callback") -> {
                        val code = extractCodeFromUrl(url)
                        repository.completeFacebookAuth(code)
                        _facebookConnectionState.value = ConnectionState.CONNECTED
                        _event.value = Event.ShowSnackbar("Facebook connected successfully")
                    }
                    url.contains("instagram_callback") -> {
                        val code = extractCodeFromUrl(url)
                        repository.completeInstagramAuth(code)
                        _instagramConnectionState.value = ConnectionState.CONNECTED
                        _event.value = Event.ShowSnackbar("Instagram connected successfully")
                    }
                }
            } catch (e: Exception) {
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to complete authentication")
            }
        }
    }
    
    // Direct method for OAuth activity to use
    fun completeYouTubeAuth(code: String) {
        viewModelScope.launch {
            AINetworkLogger.logRequest("YouTube Auth", "Completing auth with code: $code")
            try {
                _youtubeConnectionState.value = ConnectionState.CONNECTING
                repository.completeYouTubeAuth(code)
                _youtubeConnectionState.value = ConnectionState.CONNECTED
                _isYouTubeConnected.value = true
                _event.value = Event.ShowSnackbar("YouTube connected successfully")
                AINetworkLogger.logResponse("YouTube Auth", "Connection successful")
            } catch (e: Exception) {
                AINetworkLogger.logError("YouTube Auth", e)
                _youtubeConnectionState.value = ConnectionState.DISCONNECTED
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to complete YouTube authentication")
            }
        }
    }
    
    private fun extractCodeFromUrl(url: String): String {
        return url.substringAfter("code=").substringBefore("&")
    }
    
    // YouTube Videos Methods
    fun loadYouTubeVideos() {
        viewModelScope.launch {
            _isLoadingVideos.value = true
            try {
                val videos = repository.getYouTubeVideos()
                _youtubeVideos.value = sortVideos(videos, _videoSortOrder.value ?: SortOrder.DATE_DESC)
            } catch (e: Exception) {
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to load YouTube videos")
            } finally {
                _isLoadingVideos.value = false
            }
        }
    }
    
    fun setSortOrder(sortOrder: SortOrder) {
        _videoSortOrder.value = sortOrder
        _youtubeVideos.value?.let {
            _youtubeVideos.value = sortVideos(it, sortOrder)
        }
    }
    
    private fun sortVideos(videos: List<YouTubeVideo>, sortOrder: SortOrder): List<YouTubeVideo> {
        return when (sortOrder) {
            SortOrder.DATE_DESC -> videos.sortedByDescending { it.publishedAt }
            SortOrder.DATE_ASC -> videos.sortedBy { it.publishedAt }
            SortOrder.VIEWS_DESC -> videos.sortedByDescending { it.viewCount }
            SortOrder.TITLE_ASC -> videos.sortedBy { it.title }
        }
    }
    
    // Product Methods
    fun loadProducts() {
        viewModelScope.launch {
            try {
                val products = repository.getProducts()
                _pendingProducts.value = products.filter { it.status == AIProductStatus.PENDING }
                _approvedProducts.value = products.filter { it.status == AIProductStatus.APPROVED }
                _rejectedProducts.value = products.filter { it.status == AIProductStatus.REJECTED }
            } catch (e: Exception) {
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to load products")
            }
        }
    }
    
    fun approveProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.updateProductStatus(productId, AIProductStatus.APPROVED)
                loadProducts() // Refresh the lists
                _event.value = Event.ShowSnackbar("Product approved successfully")
            } catch (e: Exception) {
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to approve product")
            }
        }
    }
    
    fun rejectProduct(productId: String) {
        viewModelScope.launch {
            try {
                repository.updateProductStatus(productId, AIProductStatus.REJECTED)
                loadProducts() // Refresh the lists
                _event.value = Event.ShowSnackbar("Product rejected successfully")
            } catch (e: Exception) {
                _event.value = Event.ShowSnackbar(e.message ?: "Failed to reject product")
            }
        }
    }
    
    // Event class for navigation and UI events
    sealed class Event : MultiLiveEvent.Event() {
        data class ShowSnackbar(val message: String) : Event()
        data class NavigateToWebView(val url: String) : Event()
        data class NavigateToVideoDetail(val videoId: String) : Event()
    }
    
    // Connection state enum
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED
    }
    
    // Sort order enum
    enum class SortOrder {
        DATE_DESC,
        DATE_ASC,
        VIEWS_DESC,
        TITLE_ASC
    }

    // Debug function to diagnose OAuth issues
    fun debugOAuthRequest(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Parse and log the URL components
                val uri = Uri.parse(url)
                val components = mutableMapOf<String, String?>()
                
                // Extract query parameters
                uri.queryParameterNames.forEach { paramName ->
                    components[paramName] = uri.getQueryParameter(paramName)
                }
                
                // Log all components
                Log.d("OAuth-Debug", "URL: $url")
                Log.d("OAuth-Debug", "Scheme: ${uri.scheme}")
                Log.d("OAuth-Debug", "Host: ${uri.host}")
                Log.d("OAuth-Debug", "Path: ${uri.path}")
                Log.d("OAuth-Debug", "Query parameters: $components")
                
                AINetworkLogger.logRequest("OAuth Debug", "URL Components: $components")
                
                // If it's an error response, log detailed info
                if (components.containsKey("error")) {
                    val error = components["error"] ?: "unknown"
                    val errorDesc = components["error_description"] ?: "No description"
                    
                    Log.e("OAuth-Debug", "OAuth Error: $error - $errorDesc")
                    AINetworkLogger.logError("OAuth Error", Exception("$error: $errorDesc"))
                    
                    _errorMessage.value = R.string.ai_error_auth_url
                }
            } catch (e: Exception) {
                Log.e("OAuth-Debug", "Error debugging OAuth URL", e)
                AINetworkLogger.logError("OAuth Debug", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
