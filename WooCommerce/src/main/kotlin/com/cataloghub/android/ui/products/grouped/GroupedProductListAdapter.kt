package com.cataloghub.android.ui.products.grouped

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import com.cataloghub.android.databinding.ProductListItemBinding
import com.cataloghub.android.model.Product
import com.cataloghub.android.ui.products.ProductItemDiffCallback
import com.cataloghub.android.ui.products.ProductItemViewHolder
import com.cataloghub.android.util.CurrencyFormatter

class GroupedProductListAdapter(
    private val onItemDeleted: (product: Product) -> Unit,
    private val currencyFormatter: CurrencyFormatter
) : ListAdapter<Product, ProductItemViewHolder>(ProductItemDiffCallback) {
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int) = getItem(position).remoteId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductItemViewHolder {
        return ProductItemViewHolder(
            ProductListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ProductItemViewHolder, position: Int) {
        val product = getItem(position)

        holder.bind(
            product,
            currencyFormatter,
            isLastItem = position == itemCount - 1
        )
        holder.setOnDeleteClickListener(product, onItemDeleted)
    }
}
