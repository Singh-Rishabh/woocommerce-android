package com.cataloghub.android.ui.products.categories

interface OnProductCategoryClickListener {
    fun onProductCategoryChecked(productCategoryItemUiModel: ProductCategoryItemUiModel)
    fun onProductCategorySelected(productCategoryItemUiModel: ProductCategoryItemUiModel)
}
