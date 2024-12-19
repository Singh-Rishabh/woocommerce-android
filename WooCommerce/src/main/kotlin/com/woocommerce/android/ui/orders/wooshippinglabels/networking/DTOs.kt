package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AccountSettingsDTO(
    val storeOptions: StoreOptionsDTO
)

data class StoreOptionsDTO(
    @SerializedName("currency_symbol") val currencySymbol: String? = null,
    @SerializedName("dimension_unit") val dimensionUnit: String? = null,
    @SerializedName("weight_unit") val weightUnit: String? = null,
    @SerializedName("origin_country") val originCountry: String? = null
)

data class GetShippingLabelResponse(
    @SerializedName("success") val success: Boolean? = null,
    @SerializedName("labels") val shippingLabels: List<ShippingLabelDTO>? = null
)

data class ShippingLabelDTO(
    @SerializedName("label_id") val labelId: Long? = null,
    @SerializedName("tracking") val tracking: String? = null,
    @SerializedName("refundable_amount") val refundableAmount: BigDecimal? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("created") val created: Long? = null,
    @SerializedName("carrier_id") val carrierId: String? = null,
    @SerializedName("service_name") val serviceName: String? = null,
    @SerializedName("commercial_invoice_url") val commercialInvoiceUrl: String? = null,
    @SerializedName("is_commercial_invoice_submitted_electronically")
    val isCommercialInvoiceSubmittedElectronically: Boolean? = null,
    @SerializedName("package_name") val packageName: String? = null,
    @SerializedName("is_letter") val isLetter: Boolean? = null,
    @SerializedName("product_names") val productNames: List<String>? = null,
    @SerializedName("product_ids") val productIds: List<Long>? = null,
    @SerializedName("receipt_item_id") val receiptItemId: Long? = null,
    @SerializedName("created_date") val createdDate: Long? = null,
    @SerializedName("main_receipt_id") val mainReceiptId: Long? = null,
    @SerializedName("rate") val rate: BigDecimal? = null,
    @SerializedName("currency") val currency: String? = null,
    @SerializedName("expiry_date") val expiryDate: Long? = null,
)
