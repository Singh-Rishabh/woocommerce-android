package com.woocommerce.android.ui.woopos.home.items.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class WooPosItemsSearchViewModel() : ViewModel() {
    private val _viewState =
        MutableStateFlow<WooPosItemsSearchViewState>(WooPosItemsSearchViewState.Empty)
    val viewState: StateFlow<WooPosItemsSearchViewState> = _viewState
        .stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = _viewState.value,
        )
}
