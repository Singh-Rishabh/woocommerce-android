package com.woocommerce.android.ui.ai

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AIViewModel @Inject constructor(
    private val repository: AIRepository
) : ViewModel() {
    // AI is enabled by default for all sites
} 