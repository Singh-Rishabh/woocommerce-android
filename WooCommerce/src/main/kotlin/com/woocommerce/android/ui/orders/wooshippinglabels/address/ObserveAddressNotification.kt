package com.woocommerce.android.ui.orders.wooshippinglabels.address

import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingAddresses
import com.woocommerce.android.viewmodel.ResourceProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.woocommerce.android.R
import javax.inject.Inject

class ObserveAddressNotification @Inject constructor(
    private val resourceProvider: ResourceProvider
) {
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    operator fun invoke(addresses: Flow<WooShippingAddresses>): Flow<AddressNotification?> {
        return addresses.map { addresses ->
            when {
                addresses.shipFrom.isVerified.not() -> {
                    AddressNotification(
                        isSuccess = false,
                        message = resourceProvider.getString(
                            R.string.woo_shipping_address_notification_origin_unverified
                        ),
                        isDestinationNotification = false
                    )
                }

                addresses.shipTo.address.hasInfo().not() -> {
                    AddressNotification(
                        isSuccess = false,
                        message = resourceProvider.getString(
                            R.string.woo_shipping_address_notification_destination_missing
                        ),
                        isDestinationNotification = true
                    )
                }

                addresses.shipTo.isVerified.not() -> {
                    AddressNotification(
                        isSuccess = false,
                        message = resourceProvider.getString(
                            R.string.woo_shipping_address_notification_destination_unverified
                        ),
                        isDestinationNotification = true
                    )
                }

                addresses.shipTo.isVerified -> {
                    AddressNotification(
                        isSuccess = true,
                        message = resourceProvider.getString(
                            R.string.woo_shipping_address_notification_destination_verified
                        ),
                        expireAfter = 2_000,
                        isDestinationNotification = true
                    )
                }

                else -> null
            }
        }
    }
}

data class AddressNotification(
    val isSuccess: Boolean,
    val message: String,
    val expireAfter: Long? = null,
    private val timestamp: Long = System.currentTimeMillis(),
    val isDestinationNotification: Boolean = false,
) {
    fun isExpired(): Boolean = expireAfter?.let {
        timestamp + it < System.currentTimeMillis()
    } == true
}
