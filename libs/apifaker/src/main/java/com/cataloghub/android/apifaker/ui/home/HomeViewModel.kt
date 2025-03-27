package com.cataloghub.android.apifaker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.apifaker.ApiFakerConfig
import com.cataloghub.android.apifaker.db.EndpointDao
import com.cataloghub.android.apifaker.models.Request
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class HomeViewModel @Inject constructor(
    private val endpointDao: EndpointDao,
    private val config: ApiFakerConfig
) : ViewModel() {
    @Suppress("MagicNumber")
    val endpoints = endpointDao.observeEndpoints()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val isEnabled = config.enabled

    fun onMockingToggleChanged(enabled: Boolean) {
        viewModelScope.launch {
            config.setStatus(enabled)
        }
    }

    fun onRemoveRequest(request: Request) {
        viewModelScope.launch {
            endpointDao.deleteRequest(request)
        }
    }
}
