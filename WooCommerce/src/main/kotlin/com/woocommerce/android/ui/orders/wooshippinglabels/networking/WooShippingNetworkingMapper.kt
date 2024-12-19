package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelModel
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus
import com.woocommerce.android.ui.orders.wooshippinglabels.models.StoreOptionsModel
import java.math.BigDecimal
import java.util.Date
import javax.inject.Inject

class WooShippingNetworkingMapper @Inject constructor() {
    operator fun invoke(storeOptionsDTO: StoreOptionsDTO): StoreOptionsModel {
        return StoreOptionsModel(
            currencySymbol = storeOptionsDTO.currencySymbol.orEmpty(),
            dimensionUnit = storeOptionsDTO.dimensionUnit.orEmpty(),
            weightUnit = storeOptionsDTO.weightUnit.orEmpty(),
            originCountry = storeOptionsDTO.originCountry.orEmpty()
        )
    }

    operator fun invoke(shippingLabelDTO: ShippingLabelDTO): ShippingLabelModel {
        return ShippingLabelModel(
            labelId = shippingLabelDTO.labelId ?: 0,
            tracking = shippingLabelDTO.tracking.orEmpty(),
            refundableAmount = shippingLabelDTO.refundableAmount ?: BigDecimal.ZERO,
            status = mapShippingLabelStatus(shippingLabelDTO.status),
            created = shippingLabelDTO.created?.let { Date(it) },
            carrierId = shippingLabelDTO.carrierId.orEmpty(),
            serviceName = shippingLabelDTO.serviceName.orEmpty(),
            commercialInvoiceUrl = shippingLabelDTO.commercialInvoiceUrl.orEmpty(),
            isCommercialInvoiceSubmittedElectronically =
            shippingLabelDTO.isCommercialInvoiceSubmittedElectronically ?: false,
            packageName = shippingLabelDTO.packageName.orEmpty(),
            isLetter = shippingLabelDTO.isLetter ?: false,
            productNames = shippingLabelDTO.productNames.orEmpty(),
            productIds = shippingLabelDTO.productIds.orEmpty(),
            receiptItemId = shippingLabelDTO.receiptItemId ?: 0,
            createdDate = shippingLabelDTO.createdDate?.let { Date(it) },
            mainReceiptId = shippingLabelDTO.mainReceiptId ?: 0,
            rate = shippingLabelDTO.rate ?: BigDecimal.ZERO,
            currency = shippingLabelDTO.currency.orEmpty(),
            expiryDate = shippingLabelDTO.expiryDate ?: 0
        )
    }

    private fun mapShippingLabelStatus(status: String?): ShippingLabelStatus {
        return when (status) {
            PURCHASE_IN_PROGRESS_KEY -> ShippingLabelStatus.PurchaseInProgress
            PURCHASED_KEY -> ShippingLabelStatus.Purchased
            PURCHASE_ERROR_KEY -> ShippingLabelStatus.PurchaseError
            ANONYMIZED_KEY -> ShippingLabelStatus.Anonymized
            else -> ShippingLabelStatus.Unknown
        }
    }

    companion object {
        private const val PURCHASE_IN_PROGRESS_KEY = "PURCHASE_IN_PROGRESS"
        private const val PURCHASED_KEY = "PURCHASED"
        private const val PURCHASE_ERROR_KEY = "PURCHASE_ERROR"
        private const val ANONYMIZED_KEY = "ANONYMIZED"
    }
}
