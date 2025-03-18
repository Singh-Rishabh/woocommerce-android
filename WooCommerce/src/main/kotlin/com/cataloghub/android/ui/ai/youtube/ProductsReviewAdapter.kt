package com.cataloghub.android.ui.ai.youtube

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.cataloghub.android.R
import com.cataloghub.android.databinding.ItemProductReviewBinding
import com.cataloghub.android.model.AIProduct
import com.cataloghub.android.model.AIProductStatus

/**
 * Adapter for displaying products in the review screen.
 */
class ProductsReviewAdapter(
    private val onApproveClick: (AIProduct) -> Unit,
    private val onRejectClick: (AIProduct) -> Unit,
    private val onEditClick: (AIProduct) -> Unit
) : ListAdapter<AIProduct, ProductsReviewAdapter.ProductViewHolder>(ProductDiffCallback()) {

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

    inner class ProductViewHolder(private val binding: ItemProductReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: AIProduct) {
            binding.apply {
                textViewProductTitle.text = product.title
                textViewProductDescription.text = product.description
                textViewProductPrice.text = "$${product.price}"
                textViewVideoTitle.text = product.videoTitle

                // Load product image
                Glide.with(root.context)
                    .load(product.imageUrl)
                    .placeholder(R.drawable.ic_product_placeholder)
                    .error(R.drawable.ic_product_placeholder)
                    .into(imageViewProduct)

                // Load video thumbnail
                Glide.with(root.context)
                    .load(product.videoThumbnailUrl)
                    .placeholder(R.drawable.ic_video_placeholder)
                    .error(R.drawable.ic_video_placeholder)
                    .into(imageViewVideoThumbnail)

                // Set button visibility based on product status
                when (product.status) {
                    AIProductStatus.PENDING -> {
                        textViewStatus.setTextColor(ContextCompat.getColor(root.context, R.color.product_status_pending))
                        textViewStatus.text = root.context.getString(R.string.ai_product_status_pending)
                        buttonApprove.visibility = android.view.View.VISIBLE
                        buttonReject.visibility = android.view.View.VISIBLE
                        buttonEdit.visibility = android.view.View.VISIBLE
                    }
                    AIProductStatus.APPROVED -> {
                        textViewStatus.setTextColor(ContextCompat.getColor(root.context, R.color.product_status_approved))
                        textViewStatus.text = root.context.getString(R.string.ai_product_status_approved)
                        buttonApprove.visibility = android.view.View.GONE
                        buttonReject.visibility = android.view.View.GONE
                        buttonEdit.visibility = android.view.View.VISIBLE
                    }
                    AIProductStatus.REJECTED -> {
                        textViewStatus.setTextColor(ContextCompat.getColor(root.context, R.color.product_status_rejected))
                        textViewStatus.text = root.context.getString(R.string.ai_product_status_rejected)
                        buttonApprove.visibility = android.view.View.GONE
                        buttonReject.visibility = android.view.View.GONE
                        buttonEdit.visibility = android.view.View.VISIBLE
                    }
                }

                // Set click listeners
                buttonApprove.setOnClickListener { onApproveClick(product) }
                buttonReject.setOnClickListener { onRejectClick(product) }
                buttonEdit.setOnClickListener { onEditClick(product) }
            }
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<AIProduct>() {
        override fun areItemsTheSame(oldItem: AIProduct, newItem: AIProduct): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AIProduct, newItem: AIProduct): Boolean {
            return oldItem == newItem
        }
    }
} 