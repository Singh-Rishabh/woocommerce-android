package com.woocommerce.android.ui

sealed class AppMode {
    object PointOfSale : AppMode()
    object StoreManagement : AppMode()
}
