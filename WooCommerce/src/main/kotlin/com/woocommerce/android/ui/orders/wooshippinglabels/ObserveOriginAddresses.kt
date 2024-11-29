package com.woocommerce.android.ui.orders.wooshippinglabels

import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class ObserveOriginAddresses @Inject constructor() {
    suspend operator fun invoke(): Flow<List<OriginShippingAddress>> {
        delay(200)
        val addresses = listOf(
            OriginShippingAddress(
                firstName = "first name",
                lastName = "last name",
                company = "Company",
                phone = "",
                address1 = "A huge address that should be truncated",
                address2 = "",
                city = "San Francisco",
                postcode = "",
                email = "email",
                country = "USA",
                state = "California",
                id = "id_1",
                isDefault = false,
                isVerified = true
            ),
            OriginShippingAddress(
                firstName = "first name",
                lastName = "last name",
                company = "Company",
                phone = "",
                address1 = "Another huge address that should be truncated",
                address2 = "",
                city = "Oakland",
                postcode = "",
                email = "email",
                country = "USA",
                state = "California",
                id = "id_1",
                isDefault = false,
                isVerified = true
            ),
            OriginShippingAddress(
                firstName = "first name",
                lastName = "last name",
                company = "Company",
                phone = "",
                address1 = "Small address",
                address2 = "",
                city = "Palo Alto",
                postcode = "",
                email = "email",
                country = "USA",
                state = "California",
                id = "id_1",
                isDefault = true,
                isVerified = true
            )
        )
        return flowOf(addresses)
    }
}
