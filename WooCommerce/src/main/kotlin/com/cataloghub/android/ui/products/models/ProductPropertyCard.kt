package com.cataloghub.android.ui.products.models

data class ProductPropertyCard(val type: Type, val caption: String = "", val properties: List<ProductProperty>) {
    enum class Type {
        PRIMARY,
        SECONDARY
    }
}
