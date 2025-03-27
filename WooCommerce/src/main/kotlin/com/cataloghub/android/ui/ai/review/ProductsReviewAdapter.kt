package com.cataloghub.android.ui.ai.review

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cataloghub.android.databinding.ItemProductReviewBinding
import com.cataloghub.android.ui.ai.ProductReviewResponse
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
                textViewProductTitle.text = product.name
                textViewProductDescription.text = product.description ?: ""
                textViewProductPrice.text = product.price?.let { formatPrice(it) } ?: "N/A"
                // Confidence score display can be added somewhere if needed
                
                // Load product image
                Glide.with(imageViewProduct.context)
                    .load(product.thumbnailUrl)
                    .centerCrop()
                    .into(imageViewProduct)
                
                // Video details
                textViewVideoTitle.text = product.videoClipUrl ?: "YouTube Video"

                // Set button click listeners
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
