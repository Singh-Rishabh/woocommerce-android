package com.cataloghub.android.ui.categories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.model.UiState
import com.cataloghub.android.model.sortCategories
import com.cataloghub.android.ui.products.categories.ProductCategoriesRepository
import com.cataloghub.android.ui.products.categories.ProductCategoryItemUiModel
import com.cataloghub.android.util.WooLog
import com.cataloghub.android.viewmodel.ResourceProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class CategoriesListViewModel @Inject constructor(
    private val categoriesRepository: ProductCategoriesRepository,
    private val resourceProvider: ResourceProvider
) : ViewModel() {
    
    enum class SortBy {
        ALPHABETICAL_ASC, ALPHABETICAL_DESC, DATE_ASC, DATE_DESC
    }
    
    private var allCategories: List<ProductCategoryItemUiModel> = emptyList()
    private var currentSearchQuery: String = ""
    private var currentSortBy: SortBy = SortBy.ALPHABETICAL_ASC
    
    private val _categoriesViewState = MutableLiveData<UiState<List<ProductCategoryItemUiModel>>>()
    val categoriesViewState: LiveData<UiState<List<ProductCategoryItemUiModel>>> = _categoriesViewState
    
    private val _isBottomNavBarVisible = MutableLiveData<Boolean>(true)
    val isBottomNavBarVisible: LiveData<Boolean> = _isBottomNavBarVisible
    
    fun loadCategories(forceRefresh: Boolean = false) {
        if (_categoriesViewState.value is UiState.Loading) return
        
        _categoriesViewState.value = UiState.Loading()
        
        viewModelScope.launch {
            try {
                val result = categoriesRepository.fetchProductCategories(loadMore = false)
                
                if (result.isSuccess) {
                    val categories = result.getOrDefault(emptyList())
                    if (categories.isEmpty()) {
                        _categoriesViewState.value = UiState.Empty()
                    } else {
                        val categoryUiModels = categories.sortCategories(resourceProvider)
                        allCategories = categoryUiModels
                        sortCategories(currentSortBy)
                    }
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Unknown error"
                    _categoriesViewState.value = UiState.Error(errorMessage)
                    AnalyticsTracker.track(
                        AnalyticsEvent.CATEGORIES_LOAD_FAILED,
                        mapOf("error" to errorMessage)
                    )
                }
            } catch (e: Exception) {
                _categoriesViewState.value = UiState.Error(e.message ?: "Unknown error")
                AnalyticsTracker.track(
                    AnalyticsEvent.CATEGORIES_LOAD_FAILED,
                    mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }
    
    fun loadMoreCategories() {
        if (_categoriesViewState.value is UiState.Loading) return
        
        viewModelScope.launch {
            try {
                val result = categoriesRepository.fetchProductCategories(loadMore = true)
                
                if (result.isSuccess) {
                    val categories = result.getOrDefault(emptyList())
                    val categoryUiModels = categories.sortCategories(resourceProvider)
                    allCategories = categoryUiModels
                    
                    // Apply current search filter and sort
                    if (currentSearchQuery.isNotEmpty()) {
                        searchCategories(currentSearchQuery)
                    } else {
                        sortCategories(currentSortBy)
                    }
                }
            } catch (e: Exception) {
                // Just log the error for load more, don't update the UI state
                AnalyticsTracker.track(
                    AnalyticsEvent.CATEGORIES_LOAD_MORE_FAILED,
                    mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }
    
    fun searchCategories(query: String) {
        this.currentSearchQuery = query
        
        viewModelScope.launch {
            if (query.isEmpty()) {
                sortCategories(currentSortBy)
                return@launch
            }
            
            val filteredCategories = allCategories.filter {
                it.category.name.contains(query, ignoreCase = true) ||
                it.category.slug.contains(query, ignoreCase = true)
            }
            
            if (filteredCategories.isEmpty()) {
                _categoriesViewState.value = UiState.Empty()
            } else {
                _categoriesViewState.value = UiState.Content(filteredCategories)
            }
        }
    }
    
    fun sortCategories(sortBy: SortBy) {
        currentSortBy = sortBy
        
        if (allCategories.isEmpty()) {
            return
        }
        
        val sortedList = when (sortBy) {
            SortBy.ALPHABETICAL_ASC -> allCategories.sortedBy { it.category.name.lowercase() }
            SortBy.ALPHABETICAL_DESC -> allCategories.sortedByDescending { it.category.name.lowercase() }
            // Note: In a real implementation, you'd have access to the creation date
            // For this example, we're simulating it based on ID (assuming higher ID = newer)
            SortBy.DATE_DESC -> allCategories.sortedByDescending { it.category.remoteCategoryId }
            SortBy.DATE_ASC -> allCategories.sortedBy { it.category.remoteCategoryId }
        }
        
        _categoriesViewState.value = UiState.Content(sortedList)
    }
    
    fun toggleBottomNavVisibility(isVisible: Boolean) {
        _isBottomNavBarVisible.value = isVisible
    }
    
    fun onCleanup() {
        // Clear any references and cancel any ongoing operations
        viewModelScope.launch {
            try {
                // Clear local data
                allCategories = emptyList()
                currentSearchQuery = ""
                
                // Clean up repository resources
                categoriesRepository.onCleanup()
            } catch (e: Exception) {
                // Log but don't crash
                WooLog.e(WooLog.T.PRODUCTS, "Error cleaning up CategoriesListViewModel: ${e.message}", e)
                AnalyticsTracker.track(
                    AnalyticsEvent.CATEGORIES_CLEANUP_FAILED,
                    mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        onCleanup()
    }
} 