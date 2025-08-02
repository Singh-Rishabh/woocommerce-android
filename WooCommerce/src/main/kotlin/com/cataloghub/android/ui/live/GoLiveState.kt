package com.cataloghub.android.ui.live

import com.cataloghub.android.model.Product

data class GoLiveScreenState(
    val title: String = "",
    val description: String = "",
    val products: List<Product> = emptyList(),
    val selectedProducts: List<Product> = emptyList(),
    val platforms: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CreateSessionRequest(
    val store_url: String,
    val products: List<ProductPayload>,
    val platforms: List<String>,
    val title: String,
    val description: String
)

data class ProductPayload(
    val id: String,
    val name: String
)

data class CreateSessionResponse(
    val session_id: String,
    val stream_key: String,
    val state: String
)
