package com.cataloghub.android.ui.coupons.create

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.model.Coupon
import com.cataloghub.android.ui.coupons.edit.EditCouponViewModel
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.ScopedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CouponTypePickerViewModel @Inject constructor(savedStateHandle: SavedStateHandle) :
    ScopedViewModel(savedStateHandle) {

    fun onPercentageDiscountClicked() {
        triggerEvent(NavigateToCouponEdit(EditCouponViewModel.Mode.Create(Coupon.Type.Percent)))
    }

    fun onFixedCartDiscountClicked() {
        triggerEvent(NavigateToCouponEdit(EditCouponViewModel.Mode.Create(Coupon.Type.FixedCart)))
    }

    fun onFixedProductDiscountClicked() {
        triggerEvent(NavigateToCouponEdit(EditCouponViewModel.Mode.Create(Coupon.Type.FixedProduct)))
    }

    data class NavigateToCouponEdit(val mode: EditCouponViewModel.Mode.Create) : MultiLiveEvent.Event()
}
