package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PurchasedShippingData(
    val labelId: Long,
    val totalWeight: String,
    val totalPrice: String,
    val dimensionUnit: String,
    val weightUnit: String,
    val items: List<ShippableItem>
) : Parcelable

@Parcelize
data class ShippableItem(
    val itemId: Long,
    val productId: Long,
    val title: String,
    val dimensions: String,
    val weight: String,
    val formattedPrice: String,
    val quantity: Float,
    val dimensionUnit: String,
    val weightUnit: String,
    val imageUrl: String? = null
) : Parcelable
