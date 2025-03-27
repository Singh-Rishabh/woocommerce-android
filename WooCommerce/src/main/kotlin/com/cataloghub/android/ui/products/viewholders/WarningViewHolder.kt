package com.cataloghub.android.ui.products.viewholders

import android.view.ViewGroup.LayoutParams
import com.cataloghub.android.ui.products.models.ProductProperty.Warning
import com.cataloghub.android.widgets.WCWarningBanner

class WarningViewHolder(
    private val view: WCWarningBanner
) : ProductPropertyViewHolder(view) {
    init {
        view.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        view.isDividerVisible = false
    }
    fun bind(item: Warning) {
        view.message = item.content
    }
}
