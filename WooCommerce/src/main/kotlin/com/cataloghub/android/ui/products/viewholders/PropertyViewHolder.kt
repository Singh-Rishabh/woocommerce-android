package com.cataloghub.android.ui.products.viewholders

import android.view.ViewGroup
import android.widget.LinearLayout
import com.cataloghub.android.R
import com.cataloghub.android.ui.products.models.ProductProperty.Property
import com.cataloghub.android.ui.products.propertyviews.WCProductPropertyView

class PropertyViewHolder(parent: ViewGroup) : ProductPropertyViewHolder(parent, R.layout.product_property_view) {
    fun bind(item: Property) {
        val context = itemView.context
        val propertyView = itemView as WCProductPropertyView
        propertyView.show(
            orientation = LinearLayout.HORIZONTAL,
            caption = context.getString(item.title),
            detail = item.value,
            showTitle = true,
            isDividerVisible = item.isDividerVisible
        )
    }
}
