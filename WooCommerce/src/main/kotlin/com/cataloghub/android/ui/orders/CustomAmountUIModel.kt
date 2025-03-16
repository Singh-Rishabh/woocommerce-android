package com.cataloghub.android.ui.orders

import android.os.Parcelable
import com.cataloghub.android.ui.payments.customamounts.CustomAmountsViewModel.CustomAmountType
import com.cataloghub.android.ui.payments.customamounts.CustomAmountsViewModel.TaxStatus
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class CustomAmountUIModel(
    val id: Long,
    val amount: BigDecimal,
    val currency: String? = null,
    val name: String,
    val taxStatus: TaxStatus = TaxStatus(),
    val type: CustomAmountType,
) : Parcelable {
    companion object {
        val EMPTY = CustomAmountUIModel(
            id = 0L,
            amount = BigDecimal.ZERO,
            name = "",
            type = CustomAmountType.FIXED_CUSTOM_AMOUNT
        )
    }
}
