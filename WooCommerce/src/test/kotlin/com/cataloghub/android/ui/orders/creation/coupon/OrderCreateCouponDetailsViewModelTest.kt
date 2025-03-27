package com.cataloghub.android.ui.orders.creation.coupon

import com.cataloghub.android.extensions.isNotNullOrEmpty
import com.cataloghub.android.ui.orders.creation.OrderCreateEditViewModel
import com.cataloghub.android.ui.orders.creation.coupon.edit.OrderCreateCouponDetailsViewModel
import com.cataloghub.android.ui.orders.creation.coupon.edit.OrderCreateCouponEditFragmentArgs
import com.cataloghub.android.viewmodel.BaseUnitTest
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class OrderCreateCouponDetailsViewModelTest : BaseUnitTest() {

    @Test
    fun `given non empty coupon, when passed to coupon edition screen, then should show remove button`() {
        val navArgs = OrderCreateCouponEditFragmentArgs(
            OrderCreateEditViewModel.Mode.Edit(1L),
            "coupon_code"
        ).toSavedStateHandle()

        val sut = OrderCreateCouponDetailsViewModel(navArgs)

        sut.viewState.observeForever {
            assertTrue(it.isRemoveButtonVisible)
        }
    }

    @Test
    fun `given non empty coupon, when coupon removed, then should clear coupon`() {
        val initialCouponCode = "coupon_code"
        val navArgs = OrderCreateCouponEditFragmentArgs(
            OrderCreateEditViewModel.Mode.Edit(1L),
            initialCouponCode
        ).toSavedStateHandle()

        val sut = OrderCreateCouponDetailsViewModel(navArgs)

        sut.viewState.observeForever {
            assertTrue(it.couponCode.isNotNullOrEmpty())
            assertEquals(initialCouponCode, it.couponCode)
        }

        sut.onCouponRemoved()

        sut.event.observeForever {
            assertEquals(OrderCreateCouponDetailsViewModel.CouponEditResult.RemoveCoupon("coupon_code"), it)
        }
    }

    @Test
    fun `given non empty coupon, when remove button is clicked, then should remove coupon from order`() {
        val initialCouponCode = "coupon_code"
        val navArgs = OrderCreateCouponEditFragmentArgs(
            OrderCreateEditViewModel.Mode.Edit(1L),
            initialCouponCode
        ).toSavedStateHandle()

        val sut = OrderCreateCouponDetailsViewModel(navArgs)

        sut.viewState.observeForever {
            assertTrue(it.couponCode.isNotNullOrEmpty())
            assertEquals(initialCouponCode, it.couponCode)
        }

        sut.onCouponRemoved()

        sut.event.observeForever {
            assertEquals(OrderCreateCouponDetailsViewModel.CouponEditResult.RemoveCoupon("coupon_code"), it)
        }
    }
}
