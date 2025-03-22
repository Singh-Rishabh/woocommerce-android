package com.cataloghub.android.ui.ai.review

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cataloghub.android.R
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.ai.AIRepository
import com.cataloghub.android.ui.ai.ProductEditRequest
import com.cataloghub.android.ui.ai.ProductReviewResponse
import com.cataloghub.android.ui.ai.ProductUpdate
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIReviewViewModel @Inject constructor(
    private val repository: AIRepository,
    private val selectedSite: SelectedSite
) : ViewModel() {

    private val _viewState = MutableLiveData(ViewState())
    val viewState: LiveData<ViewState> = _viewState

    private val _event = MultiLiveEvent<Event>()
    val event: LiveData<Event> = _event

    private var currentYoutubeUrl: String? = null
    private val pendingChanges = mutableMapOf<String, ProductUpdate>()

    init {
        loadProducts()
    }

    private fun loadProducts() {
        viewModelScope.launch {
            _viewState.value = ViewState(isLoading = true)

            try {
                val storeUrl = selectedSite.get().url
                val youtubeUrl = currentYoutubeUrl ?: ""
                val products = repository.getProducts(youtubeUrl, storeUrl)
                _viewState.value = ViewState(
                    products = products,
                    isLoading = false,
                    hasChanges = pendingChanges.isNotEmpty()
                )
            } catch (e: Exception) {
                _event.value = Event.ShowError(R.string.ai_error_processing)
                _viewState.value = ViewState(isLoading = false)
            }
        }
    }

    fun approveProduct(product: ProductReviewResponse) {
        pendingChanges[product.id] = ProductUpdate(
            id = product.id,
            name = null,
            description = null,
            price = null,
            status = "approved"
        )
        updateViewState()
    }

    fun rejectProduct(product: ProductReviewResponse) {
        pendingChanges[product.id] = ProductUpdate(
            id = product.id,
            name = null,
            description = null,
            price = null,
            status = "rejected"
        )
        updateViewState()
    }

    fun editProduct(product: ProductReviewResponse) {
        // TODO: Show edit dialog and update product details
        pendingChanges[product.id] = ProductUpdate(
            id = product.id,
            name = product.name,
            description = product.description,
            price = product.price,
            status = "approved"
        )
        updateViewState()
    }

    fun submitChanges() {
        if (pendingChanges.isEmpty()) return

        viewModelScope.launch {
            _viewState.value = _viewState.value?.copy(isLoading = true)

            try {
                val request = ProductEditRequest(
                    youtubeUrl = currentYoutubeUrl ?: "",
                    storeUrl = selectedSite.get().url,
                    products = pendingChanges.values.toList()
                )

                repository.editProducts(request)
                pendingChanges.clear()
                _event.value = Event.ShowSuccess(R.string.ai_success_processing)
                loadProducts()
            } catch (e: Exception) {
                _event.value = Event.ShowError(R.string.ai_error_processing)
                _viewState.value = _viewState.value?.copy(isLoading = false)
            }
        }
    }

    private fun updateViewState() {
        _viewState.value = _viewState.value?.copy(
            hasChanges = pendingChanges.isNotEmpty()
        )
    }

    data class ViewState(
        val products: List<ProductReviewResponse> = emptyList(),
        val isLoading: Boolean = false,
        val hasChanges: Boolean = false
    )

    sealed class Event : MultiLiveEvent.Event(false) {
        data class ShowError(@androidx.annotation.StringRes val message: Int) : Event()
        data class ShowSuccess(@androidx.annotation.StringRes val message: Int) : Event()
    }
}

annotation class StringRes
