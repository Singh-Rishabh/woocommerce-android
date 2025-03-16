package com.cataloghub.android.ui.products.variations.attributes

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.cataloghub.android.databinding.AttributeItemBinding
import com.cataloghub.android.model.ProductAttribute
import com.cataloghub.android.ui.products.variations.attributes.AddAttributeAdapter.AddAttributeViewHolder

class AddAttributeAdapter(
    onItemClick: (attributeId: Long, attributeName: String) -> Unit
) : AttributeBaseAdapter<AddAttributeViewHolder>(onItemClick) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddAttributeViewHolder {
        return AddAttributeViewHolder(
            AttributeItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    inner class AddAttributeViewHolder(viewBinding: AttributeItemBinding) :
        AttributeBaseViewHolder(viewBinding) {
        override fun bind(attribute: ProductAttribute) {
            viewBinding.attributeName.text = attribute.name
            viewBinding.attributeTerms.isVisible = false
        }
    }
}
