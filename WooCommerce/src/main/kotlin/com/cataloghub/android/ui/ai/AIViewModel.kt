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

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _authUrl = MutableLiveData<String?>(null)
    val authUrl: LiveData<String?> = _authUrl
    
    // Store URL for methods that need it
    private var currentStoreUrl: String = ""

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

    private val _event = MutableLiveData<Event>()
    val event: LiveData<Event> = _event

    private val _successMessage = MutableLiveData<String?>(null)
    val successMessage: LiveData<String?> = _successMessage

    private val TAG = "AIViewModel"

    init {
        AINetworkLogger.logRequest("AIViewModel", "Initialized")
        WooLog.d(WooLog.T.AI, "AIViewModel initialized")
    }
    
    // Set the current store URL
    fun setStoreUrl(storeUrl: String) {
        currentStoreUrl = storeUrl
        // Check connection states with the updated URL
        checkAllConnections(storeUrl)
    }
    
    private fun checkAllConnections(storeUrl: String) {
        checkYouTubeConnection(storeUrl)
        checkFacebookConnection()
        checkInstagramConnection()
    }

    /**
     * Get the YouTube authorization URL
     * Returns the URL as a string
     */
    suspend fun getYouTubeAuthUrl(storeUrl: String): String {
        Log.d(TAG, "Requesting YouTube auth URL for store: $storeUrl")
        currentStoreUrl = storeUrl // Store this for future use
        
        try {
            val url = repository.getYouTubeAuthUrl(storeUrl)
            Log.d(TAG, "Received YouTube auth URL: $url")
            return url
        } catch (e: Exception) {
            Log.e(TAG, "Error getting YouTube auth URL: ${e.message}", e)
            throw e
        }
    }

    /**
     * Check YouTube connection status using the token-status API
     */
    fun checkYouTubeConnectionStatus(storeUrl: String) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val tokenStatus = repository.checkYouTubeTokenStatus(storeUrl)
                // Consider a channel connected if we have a token AND it's valid for Android
                val isConnected = tokenStatus.hasToken && (tokenStatus.validForAndroid == true)
                _isYouTubeConnected.postValue(isConnected)
                Log.d(TAG, "YouTube connection status checked: hasToken=${tokenStatus.hasToken}, validForAndroid=${tokenStatus.validForAndroid}, isConnected=$isConnected")
            } catch (e: Exception) {
                Log.e(TAG, "Error checking YouTube connection: ${e.message}", e)
                _errorMessage.postValue(e.message ?: "Error checking YouTube connection")
                _isYouTubeConnected.postValue(false)
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    /**
     * Save YouTube token directly with the auth code from the callback
     * This uses the save-token API endpoint
     */
    fun saveYouTubeToken(authCode: String, storeUrl: String) {
        Log.d(TAG, "Saving YouTube token with auth code for store: $storeUrl")
        currentStoreUrl = storeUrl // Store for future use
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val response = repository.saveYouTubeToken(authCode, storeUrl)
                Log.d(TAG, "YouTube token save response: $response")
                
                if (response.success) {
                    _isYouTubeConnected.postValue(true)
                    _successMessage.postValue("YouTube connected successfully!")
                    _event.postValue(Event.ShowSnackbar("YouTube connected successfully!"))
                } else {
                    Log.e(TAG, "Failed to save YouTube token: ${response.message}")
                    _errorMessage.postValue(response.message)
                    _isYouTubeConnected.postValue(false)
                    _event.postValue(Event.ShowSnackbar("Failed to connect YouTube: ${response.message}"))
                }
                
                // Refresh connection status
                checkYouTubeConnectionStatus(storeUrl)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving YouTube token: ${e.message}", e)
                _errorMessage.postValue(e.message ?: "Error connecting to YouTube")
                _isYouTubeConnected.postValue(false)
                _event.postValue(Event.ShowSnackbar("Error connecting to YouTube: ${e.message}"))
            } finally {
                _isLoading.postValue(false)
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
                _errorMessage.value = "Error revoking YouTube token"
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
    /**
     * Start the YouTube connection flow
     * This will get the auth URL and post it to the LiveData
     */
    fun connectYouTube() {
        _isLoading.value = true
        
        if (currentStoreUrl.isNullOrEmpty()) {
            _errorMessage.postValue("No store URL provided")
            _isLoading.postValue(false)
            return
        }
        
        Log.d(TAG, "Starting YouTube connection with store URL: $currentStoreUrl")
        
        viewModelScope.launch {
            try {
                // Get the auth URL and append the store URL as state parameter if not already included
                var url = getYouTubeAuthUrl(currentStoreUrl!!)
                
                // Check if URL already has state parameter
                if (!url.contains("state=")) {
                    // Add state parameter with store URL
                    val separator = if (url.contains("?")) "&" else "?"
                    url = "${url}${separator}state=${Uri.encode(currentStoreUrl)}"
                    Log.d(TAG, "Added state parameter to auth URL: $url")
                }
                
                if (url.isNotEmpty()) {
                    _authUrl.postValue(url)
                    Log.d(TAG, "Posted auth URL to LiveData: $url")
                } else {
                    _errorMessage.postValue("Failed to get authorization URL")
                    _isLoading.postValue(false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in connectYouTube: ${e.message}", e)
                _errorMessage.postValue(e.message ?: "Error connecting to YouTube")
                _isLoading.postValue(false)
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
        if (currentStoreUrl.isEmpty()) {
            _event.value = Event.ShowSnackbar("Store URL not set")
            return
        }
        
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = repository.disconnectYouTube(currentStoreUrl)
                if (result) {
                    _isYouTubeConnected.postValue(false)
                    _event.postValue(Event.ShowSnackbar("YouTube disconnected successfully"))
                } else {
                    _event.postValue(Event.ShowSnackbar("Failed to disconnect YouTube"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error disconnecting YouTube: ${e.message}", e)
                _errorMessage.postValue(e.message ?: "Error disconnecting YouTube")
            } finally {
                _isLoading.postValue(false)
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
    
    private fun checkYouTubeConnection(storeUrl: String) {
        viewModelScope.launch {
            try {
                val isConnected = repository.isYouTubeConnected(storeUrl)
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
    fun handleOAuthCallback(callbackUrl: String) {
        Log.d(TAG, "Handling OAuth callback: $callbackUrl")
        
        try {
            // Extract the authorization code from the callback URL
            val uri = android.net.Uri.parse(callbackUrl)
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            
            if (code != null) {
                Log.d(TAG, "Authorization code extracted: $code")
                completeYouTubeAuth(code)
            } else if (error != null) {
                Log.e(TAG, "OAuth error: $error")
                _errorMessage.value = "YouTube authorization failed: $error"
            } else {
                Log.e(TAG, "No code or error in callback URL")
                _errorMessage.value = "YouTube authorization failed"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling OAuth callback: ${e.message}", e)
            _errorMessage.value = "Error processing YouTube authorization"
        }
    }
    
    // Direct method for OAuth activity to use
    fun completeYouTubeAuth(authCode: String) {
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val result = repository.completeYouTubeAuth(authCode)
                if (result) {
                    _isYouTubeConnected.postValue(true)
                    _event.postValue(Event.ShowSnackbar("YouTube connected successfully"))
                    Log.d(TAG, "YouTube connection successful")
                } else {
                    _isYouTubeConnected.postValue(false)
                    _event.postValue(Event.ShowSnackbar("YouTube connection failed"))
                    Log.e(TAG, "YouTube connection failed")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error completing YouTube auth: ${e.message}", e)
                _errorMessage.postValue(e.message ?: "Error connecting to YouTube")
                _isYouTubeConnected.postValue(false)
            } finally {
                _isLoading.postValue(false)
            }
        }
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
                    
                    _errorMessage.value = "YouTube authorization failed: $error"
                }
            } catch (e: Exception) {
                Log.e("OAuth-Debug", "Error debugging OAuth URL", e)
                AINetworkLogger.logError("OAuth Debug", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Set loading state
     */
    fun setLoading(isLoading: Boolean) {
        _isLoading.postValue(isLoading)
    }

    /**
     * Get the current store URL
     */
    fun getCurrentStoreUrl(): String {
        return currentStoreUrl
    }

    /**
     * Handle YouTube auth callback with authorization code
     */
    fun handleYouTubeAuthCallback(authCode: String) {
        Log.d(TAG, "Handling YouTube auth callback with code")
        
        if (currentStoreUrl.isNullOrEmpty()) {
            Log.e(TAG, "Store URL is empty, attempting to get from selectedSite")
            _errorMessage.postValue("Error: No store URL available")
            return
        }
        
        Log.d(TAG, "Using store URL: $currentStoreUrl")
        saveYouTubeToken(authCode, currentStoreUrl)
    }

    /**
     * Handle Facebook OAuth callback
     */
    fun handleFacebookAuthCallback(authCode: String) {
        // Implement Facebook auth handling
        Log.d(TAG, "Handling Facebook auth callback - not implemented yet")
    }

    /**
     * Handle Instagram OAuth callback
     */
    fun handleInstagramAuthCallback(authCode: String) {
        // Implement Instagram auth handling
        Log.d(TAG, "Handling Instagram auth callback - not implemented yet")
    }
}
