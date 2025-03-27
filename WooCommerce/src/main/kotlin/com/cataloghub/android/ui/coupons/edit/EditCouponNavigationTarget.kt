package com.cataloghub.android.ui.coupons.edit

import com.cataloghub.android.model.Coupon.CouponRestrictions
import com.cataloghub.android.ui.products.ProductRestriction
import com.cataloghub.android.ui.products.selector.ProductSelectorViewModel
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event

sealed class EditCouponNavigationTarget : Event() {
    data class OpenDescriptionEditor(val currentDescription: String?) : EditCouponNavigationTarget()
    data class OpenCouponRestrictions(
        val restrictions: CouponRestrictions,
        val currencyCode: String,
        val showLimitUsageToXItems: Boolean
    ) : EditCouponNavigationTarget()

    data class EditIncludedProducts(
        val selectedItems: List<ProductSelectorViewModel.SelectedItem>,
        val restrictions: List<ProductRestriction>
    ) : EditCouponNavigationTarget()

    data class EditIncludedProductCategories(val categoryIds: List<Long>) :
        EditCouponNavigationTarget()

    data class EditExcludedProducts(val excludedItems: List<ProductSelectorViewModel.SelectedItem>) :
        EditCouponNavigationTarget()

    data class EditExcludedProductCategories(val excludedCategoryIds: List<Long>) :
        EditCouponNavigationTarget()
}
