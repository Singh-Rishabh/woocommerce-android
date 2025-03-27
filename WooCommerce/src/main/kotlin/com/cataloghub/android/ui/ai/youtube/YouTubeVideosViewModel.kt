package com.cataloghub.android.ui.ai.youtube

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.R
import com.cataloghub.android.ui.ai.AINetworkLogger
import com.cataloghub.android.ui.ai.AIRepository
import com.cataloghub.android.ui.ai.YouTubeVideo
import com.cataloghub.android.util.WooLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class YouTubeVideosViewModel @Inject constructor(
    private val repository: AIRepository
) : ViewModel() {

    private val _videos = MutableLiveData<List<YouTubeVideo>>()
    val videos: LiveData<List<YouTubeVideo>> = _videos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<Int?>()
    val errorMessage: LiveData<Int?> = _errorMessage

    private var nextPageToken: String? = null
    private var prevPageToken: String? = null
    private var totalResults: Int = 0
    
    private var currentSortBy = "date"
    private var currentOrder = "desc"
    
    private var searchJob: Job? = null
    private var currentQuery: String? = null

    init {
        AINetworkLogger.logRequest("YouTubeVideosViewModel", "Initialized")
        WooLog.d(WooLog.T.AI, "YouTubeVideosViewModel initialized")
    }

    fun loadVideos(storeUrl: String, pageToken: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            currentQuery = null
            
            try {
                val response = repository.listYouTubeVideos(
                    storeUrl = storeUrl,
                    pageToken = pageToken,
                    sortBy = currentSortBy,
                    order = currentOrder
                )
                
                _videos.value = response.videos
                nextPageToken = response.nextPageToken
                prevPageToken = response.prevPageToken
                totalResults = response.totalResults
                
                AINetworkLogger.logResponse(
                    "YouTube Videos Loaded",
                    "Count: ${response.videos.size}, Total: ${response.totalResults}"
                )
                WooLog.d(
                    WooLog.T.AI,
                    "Loaded ${response.videos.size} YouTube videos (total: ${response.totalResults})"
                )
            } catch (e: Exception) {
                AINetworkLogger.logError("YouTube Videos Load Error", e)
                WooLog.e(WooLog.T.AI, "Failed to load YouTube videos", e)
                _errorMessage.value = R.string.ai_error_loading_videos
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchVideos(storeUrl: String, query: String) {
        // Cancel previous search job if it's still running
        searchJob?.cancel()
        
        // Start a new search job with a small delay to avoid too many API calls while typing
        searchJob = viewModelScope.launch {
            delay(300) // 300ms debounce
            
            _isLoading.value = true
            currentQuery = query
            
            try {
                val response = repository.searchYouTubeVideos(
                    storeUrl = storeUrl,
                    query = query
                )
                
                _videos.value = response.videos
                nextPageToken = response.nextPageToken
                prevPageToken = response.prevPageToken
                totalResults = response.totalResults
                
                AINetworkLogger.logResponse(
                    "YouTube Videos Search",
                    "Query: $query, Count: ${response.videos.size}, Total: ${response.totalResults}"
                )
                WooLog.d(
                    WooLog.T.AI,
                    "Searched YouTube videos with query '$query', found ${response.videos.size} results"
                )
            } catch (e: Exception) {
                AINetworkLogger.logError("YouTube Videos Search Error", e)
                WooLog.e(WooLog.T.AI, "Failed to search YouTube videos", e)
                _errorMessage.value = R.string.ai_error_searching_videos
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadNextPage(storeUrl: String) {
        if (nextPageToken != null) {
            if (currentQuery.isNullOrBlank()) {
                loadVideos(storeUrl, nextPageToken)
            } else {
                searchVideos(storeUrl, currentQuery!!)
            }
        }
    }

    fun loadPreviousPage(storeUrl: String) {
        if (prevPageToken != null) {
            if (currentQuery.isNullOrBlank()) {
                loadVideos(storeUrl, prevPageToken)
            } else {
                searchVideos(storeUrl, currentQuery!!)
            }
        }
    }
    
    fun setSortOrder(sortBy: String, order: String) {
        if (currentSortBy != sortBy || currentOrder != order) {
            currentSortBy = sortBy
            currentOrder = order
            
            // Reload videos with new sort order
            repository.selectedSite.get().url.let { storeUrl ->
                loadVideos(storeUrl)
            }
        }
    }

    fun errorMessageShown() {
        _errorMessage.value = null
    }
} 