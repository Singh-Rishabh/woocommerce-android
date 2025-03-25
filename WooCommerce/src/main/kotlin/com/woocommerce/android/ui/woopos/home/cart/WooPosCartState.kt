package com.woocommerce.android.ui.woopos.home.cart

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WooPosCartState(
    val cartStatus: WooPosCartStatus = WooPosCartStatus.EDITABLE,
    val toolbar: Toolbar = Toolbar(),
    val body: Body = Body.Empty,
    val areItemsRemovable: Boolean = true,
    val isCheckoutButtonVisible: Boolean = true,
) : Parcelable {
    @Parcelize
    sealed class Body : Parcelable {
        abstract val amountOfItems: Int

        @Parcelize
        data object Empty : Body() {
            override val amountOfItems: Int
                get() = 0
        }

        @Parcelize
        data class WithItems(val itemsInCart: List<WooPosCartItemViewState>) : Body(), Parcelable {

            override val amountOfItems: Int
                get() = itemsInCart.size
        }
    }

    @Parcelize
    data class Toolbar(
        val backIconVisible: Boolean = false,
        val itemsCount: String? = null,
        val isClearAllButtonVisible: Boolean = false,
    ) : Parcelable
}

enum class WooPosCartStatus {
    EDITABLE, CHECKOUT, EMPTY,
}

sealed class WooPosCartItemViewState(open val name: String) : Parcelable {
    @Parcelize
    sealed class Product(
        open val id: Long,
        override val name: String,
        open val price: String,
        open val description: String?,
        open val imageUrl: String?,
    ) : WooPosCartItemViewState(name), Parcelable {
        @Parcelize
        data class Simple(
            override val id: Long,
            override val name: String,
            override val price: String,
            override val description: String?,
            override val imageUrl: String?,
        ) : Product(id, name, price, description, imageUrl), Parcelable

        @Parcelize
        data class Variation(
            override val id: Long,
            val parentProductId: Long,
            override val name: String,
            override val price: String,
            override val description: String?,
            override val imageUrl: String?,
        ) : Product(id, name, price, description, imageUrl), Parcelable
    }
}
