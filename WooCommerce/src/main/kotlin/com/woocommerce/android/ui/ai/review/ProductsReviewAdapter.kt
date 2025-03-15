package com.woocommerce.android.ui.ai.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.woocommerce.android.databinding.ItemProductReviewBinding
import com.woocommerce.android.ui.ai.ProductReviewResponse
import com.bumptech.glide.Glide
import java.text.NumberFormat
import java.util.Locale

class ProductsReviewAdapter(
    private val onApproveClick: (ProductReviewResponse) -> Unit,
    private val onRejectClick: (ProductReviewResponse) -> Unit,
    private val onEditClick: (ProductReviewResponse) -> Unit
) : ListAdapter<ProductReviewResponse, ProductsReviewAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductReviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductViewHolder(
        private val binding: ItemProductReviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductReviewResponse) {
            binding.apply {
                textName.text = product.name
                textPrice.text = product.price?.let { formatPrice(it) } ?: "N/A"
                textConfidence.text = "Confidence: ${(product.confidenceScore * 100).toInt()}%"

                Glide.with(imageProduct)
                    .load(product.thumbnailUrl)
                    .centerCrop()
                    .into(imageProduct)

                buttonApprove.setOnClickListener { onApproveClick(product) }
                buttonReject.setOnClickListener { onRejectClick(product) }
                buttonEdit.setOnClickListener { onEditClick(product) }
            }
        }

        private fun formatPrice(price: Double): String {
            return NumberFormat.getCurrencyInstance(Locale.US).format(price)
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<ProductReviewResponse>() {
        override fun areItemsTheSame(oldItem: ProductReviewResponse, newItem: ProductReviewResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductReviewResponse, newItem: ProductReviewResponse): Boolean {
            return oldItem == newItem
        }
    }
} 