package com.cataloghub.android.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cataloghub.android.databinding.ItemCategoryBinding
import com.cataloghub.android.ui.products.categories.ProductCategoryItemUiModel
import com.cataloghub.android.util.getDateTimeString

class CategoriesListAdapter(
    private val onCategoryClick: (categoryId: Long, categoryName: String) -> Unit
) : RecyclerView.Adapter<CategoriesListAdapter.CategoryViewHolder>() {

    private var categories: List<ProductCategoryItemUiModel> = emptyList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    fun setCategories(newCategories: List<ProductCategoryItemUiModel>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(private val binding: ItemCategoryBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val categoryId = categories[position].category.remoteCategoryId
                    val categoryName = categories[position].category.name
                    onCategoryClick(categoryId, categoryName)
                }
            }
        }

        fun bind(categoryUiModel: ProductCategoryItemUiModel) {
            with(binding) {
                val category = categoryUiModel.category
                categoryName.text = category.name
                
                // Set margin for nested categories
                val params = categoryName.layoutParams as ViewGroup.MarginLayoutParams
                params.marginStart = categoryUiModel.margin
                categoryName.layoutParams = params

                // Display the actual product count from the category model
                // val productCount = category.count
                categoryProductCount.visibility = android.view.View.GONE
                // categoryProductCount.text = if (productCount == 1) {
                //     "1 product" 
                // } else {
                //     "$productCount products"
                // }
                
                // Remove fake creation date display
                categoryCreationDate.visibility = android.view.View.GONE
            }
        }
    }
}