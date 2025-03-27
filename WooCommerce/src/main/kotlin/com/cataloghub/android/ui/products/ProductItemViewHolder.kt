package com.cataloghub.android.ui.products

import android.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.cataloghub.android.R
import com.cataloghub.android.databinding.ProductListItemBinding
import com.cataloghub.android.model.Product
import com.cataloghub.android.util.CurrencyFormatter

class ProductItemViewHolder(val viewBinding: ProductListItemBinding) :
    RecyclerView.ViewHolder(viewBinding.root) {
    private val context = viewBinding.root.context

    fun bind(
        product: Product,
        currencyFormatter: CurrencyFormatter,
        isActivated: Boolean = false,
        isProductHighlighted: Boolean = false,
        isUploadingMedia: Boolean = false,
        isLastItem: Boolean,
    ) {
        viewBinding.root.isActivated = isActivated

        if (isProductHighlighted) {
            viewBinding.root.setBackgroundColor(
                viewBinding.root.context.getColor(R.color.color_item_selected)
            )
        } else {
            viewBinding.root.setBackgroundColor(Color.TRANSPARENT)
        }

        viewBinding.productItemView.binding.divider.isVisible = !isLastItem

        viewBinding.productItemView.bind(
            product = product,
            currencyFormatter = currencyFormatter,
            isActivated = isActivated,
            isUploadingMedia = isUploadingMedia
        )

        ViewCompat.setTransitionName(
            viewBinding.root,
            context.getString(R.string.order_card_transition_name, product.remoteId.toString()),
        )
    }

    fun setOnDeleteClickListener(
        product: Product,
        onItemDeleted: (product: Product) -> Unit
    ) {
        with(viewBinding.productItemView.binding.productBtnDelete) {
            isVisible = true
            setOnClickListener { onItemDeleted.invoke(product) }
        }
    }
}
