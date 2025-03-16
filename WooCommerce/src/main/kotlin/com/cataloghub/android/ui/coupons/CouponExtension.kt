package com.cataloghub.android.ui.coupons

import com.cataloghub.android.model.Coupon
import com.cataloghub.android.util.CouponUtils
import java.util.Date

fun Coupon.toUiModel(couponUtils: CouponUtils, currencyCode: String?): CouponListItem {
    return CouponListItem(
        id = id,
        code = code,
        summary = couponUtils.generateSummary(this, currencyCode),
        isActive = dateExpires?.after(Date()) ?: true
    )
}
