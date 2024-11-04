package com.woocommerce.android.ui.woopos.home.products

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

sealed class WooPosItemsViewState(
    open val reloadingProductsWithPullToRefresh: Boolean,
) {
    data class Content(
        val items: List<WooPosItem>,
        val loadingMore: Boolean,
        val bannerState: BannerState,
        override val reloadingProductsWithPullToRefresh: Boolean = false,
    ) : WooPosItemsViewState(reloadingProductsWithPullToRefresh) {
        data class BannerState(
            val isBannerHiddenByUser: Boolean,
            @StringRes val title: Int,
            @StringRes val message: Int,
            @DrawableRes val icon: Int,
        )
    }

    data class Loading(
        override val reloadingProductsWithPullToRefresh: Boolean = false,
        val withCart: Boolean,
    ) :
        WooPosItemsViewState(reloadingProductsWithPullToRefresh)

    data class Error(override val reloadingProductsWithPullToRefresh: Boolean = false) :
        WooPosItemsViewState(reloadingProductsWithPullToRefresh)

    data class Empty(override val reloadingProductsWithPullToRefresh: Boolean = false) :
        WooPosItemsViewState(reloadingProductsWithPullToRefresh)
}
