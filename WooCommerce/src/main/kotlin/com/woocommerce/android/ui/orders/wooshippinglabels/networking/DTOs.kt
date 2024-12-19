package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import com.google.gson.annotations.SerializedName

data class AccountSettingsDTO(
    val storeOptions: StoreOptionsDTO
)

data class StoreOptionsDTO(
    @SerializedName("currency_symbol") val currencySymbol: String? = null,
    @SerializedName("dimension_unit") val dimensionUnit: String? = null,
    @SerializedName("weight_unit") val weightUnit: String? = null,
    @SerializedName("origin_country") val originCountry: String? = null
)
