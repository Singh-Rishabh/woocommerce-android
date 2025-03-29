package com.cataloghub.android.model

/**
 * Represents different states of UI components during data loading operations.
 */
sealed class UiState<out T> {
    /**
     * Indicates that data is being loaded.
     */
    class Loading<T> : UiState<T>()
    
    /**
     * Indicates that data is successfully loaded and is available.
     * @param data The loaded data
     */
    data class Content<T>(val data: T) : UiState<T>()
    
    /**
     * Indicates that there is no data available.
     */
    class Empty<T> : UiState<T>()
    
    /**
     * Indicates that an error occurred during data loading.
     * @param message The error message describing what went wrong
     */
    data class Error<T>(val message: String) : UiState<T>()
} 