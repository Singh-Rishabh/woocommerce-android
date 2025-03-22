package com.cataloghub.android.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class WebViewViewModel @Inject constructor() : ViewModel() {
    private val TAG = "WebViewViewModel"
    
    fun debugOAuthRequest(url: String) {
        Log.d(TAG, "Debug OAuth request: $url")
    }
} 