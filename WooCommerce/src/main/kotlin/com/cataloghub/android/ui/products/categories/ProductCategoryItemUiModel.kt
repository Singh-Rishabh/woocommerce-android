package com.cataloghub.android.ui.products.categories

import com.cataloghub.android.R
import com.cataloghub.android.model.ProductCategory

data class ProductCategoryItemUiModel(
    val category: ProductCategory,
    var margin: Int = R.dimen.major_125,
    var isSelected: Boolean = false
)
