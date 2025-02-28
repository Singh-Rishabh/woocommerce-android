package com.woocommerce.android.ui.orders.wooshippinglabels.address.destination

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.networking.WooShippingLabelRepository
import com.woocommerce.android.util.CoroutineDispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class VerifyDestinationAddress @Inject constructor(
    private val shippingRepository: WooShippingLabelRepository,
    private val selectedSite: SelectedSite,
    private val coroutineDispatchers: CoroutineDispatchers
) {
    suspend operator fun invoke(
        orderId: Long
    ): Result<Boolean> {
        return withContext(coroutineDispatchers.io) {
            selectedSite.getOrNull()?.let {
                val response = shippingRepository.verifyDestinationAddress(it, orderId)
                Result.success(response.model == true)
            } ?: Result.failure(Exception("No site selected"))
        }
    }
}
