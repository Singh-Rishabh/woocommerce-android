package com.woocommerce.android.ui.orders.wooshippinglabels.address.origin

import com.woocommerce.android.model.Address
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.networking.WooShippingLabelRepository
import javax.inject.Inject

class UpdateOriginAddress @Inject constructor(
    private val shippingRepository: WooShippingLabelRepository,
    private val selectedSite: SelectedSite
) {
    suspend operator fun invoke(address: Address, addressId: String?): Result<OriginShippingAddress> {
        return selectedSite.getOrNull()?.let {
            val response = shippingRepository.updateOriginAddress(it, address, addressId)
            val result = response.model
            when {
                response.isError || result == null -> {
                    val message =
                        response.error.message ?: if (result == null) "Empty response" else "Unknown error"
                    Result.failure(Exception(message))
                }
                else -> Result.success(result)
            }
        } ?: Result.failure(Exception("No site selected"))
    }
}
