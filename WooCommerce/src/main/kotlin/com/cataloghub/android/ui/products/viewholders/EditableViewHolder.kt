package com.cataloghub.android.ui.products.viewholders

import android.view.ViewGroup
import com.cataloghub.android.R
import com.cataloghub.android.ui.products.models.ProductProperty.Editable
import com.cataloghub.android.ui.products.propertyviews.WCProductPropertyEditableView

class EditableViewHolder(parent: ViewGroup) : ProductPropertyViewHolder(
    parent,
    R.layout.product_property_editable_view
) {
    fun bind(item: Editable) {
        val context = itemView.context
        val hint = context.getString(item.hint)
        val editableView = itemView as WCProductPropertyEditableView

        item.onTextChanged?.let { onTextChanged ->
            editableView.setOnTextChangedListener { text -> onTextChanged(text.toString()) }
        }

        editableView.show(hint, item.text, item.shouldFocus, item.isReadOnly)

        if (item.badgeText != null && item.badgeColor != null) {
            editableView.showBadge(item.badgeText, item.badgeColor)
        } else {
            editableView.hideBadge()
        }
    }
}
