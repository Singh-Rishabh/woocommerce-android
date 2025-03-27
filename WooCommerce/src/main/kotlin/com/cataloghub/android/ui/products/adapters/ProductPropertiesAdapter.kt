package com.cataloghub.android.ui.products.adapters

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import com.cataloghub.android.ui.products.models.ProductProperty
import com.cataloghub.android.ui.products.models.ProductProperty.Button
import com.cataloghub.android.ui.products.models.ProductProperty.ComplexProperty
import com.cataloghub.android.ui.products.models.ProductProperty.Editable
import com.cataloghub.android.ui.products.models.ProductProperty.Link
import com.cataloghub.android.ui.products.models.ProductProperty.Property
import com.cataloghub.android.ui.products.models.ProductProperty.PropertyGroup
import com.cataloghub.android.ui.products.models.ProductProperty.RatingBar
import com.cataloghub.android.ui.products.models.ProductProperty.ReadMore
import com.cataloghub.android.ui.products.models.ProductProperty.Switch
import com.cataloghub.android.ui.products.models.ProductProperty.Type
import com.cataloghub.android.ui.products.models.ProductProperty.Type.BUTTON
import com.cataloghub.android.ui.products.models.ProductProperty.Type.COMPLEX_PROPERTY
import com.cataloghub.android.ui.products.models.ProductProperty.Type.DIVIDER
import com.cataloghub.android.ui.products.models.ProductProperty.Type.EDITABLE
import com.cataloghub.android.ui.products.models.ProductProperty.Type.LINK
import com.cataloghub.android.ui.products.models.ProductProperty.Type.PROPERTY
import com.cataloghub.android.ui.products.models.ProductProperty.Type.PROPERTY_GROUP
import com.cataloghub.android.ui.products.models.ProductProperty.Type.RATING_BAR
import com.cataloghub.android.ui.products.models.ProductProperty.Type.READ_MORE
import com.cataloghub.android.ui.products.models.ProductProperty.Type.SWITCH
import com.cataloghub.android.ui.products.models.ProductProperty.Type.WARNING
import com.cataloghub.android.ui.products.models.ProductProperty.Warning
import com.cataloghub.android.ui.products.viewholders.ButtonViewHolder
import com.cataloghub.android.ui.products.viewholders.ComplexPropertyViewHolder
import com.cataloghub.android.ui.products.viewholders.DividerViewHolder
import com.cataloghub.android.ui.products.viewholders.EditableViewHolder
import com.cataloghub.android.ui.products.viewholders.LinkViewHolder
import com.cataloghub.android.ui.products.viewholders.ProductPropertyViewHolder
import com.cataloghub.android.ui.products.viewholders.PropertyGroupViewHolder
import com.cataloghub.android.ui.products.viewholders.PropertyViewHolder
import com.cataloghub.android.ui.products.viewholders.RatingBarViewHolder
import com.cataloghub.android.ui.products.viewholders.ReadMoreViewHolder
import com.cataloghub.android.ui.products.viewholders.SwitchViewHolder
import com.cataloghub.android.ui.products.viewholders.WarningViewHolder
import com.cataloghub.android.widgets.WCWarningBanner

class ProductPropertiesAdapter : Adapter<ProductPropertyViewHolder>() {
    private var items = listOf<ProductProperty>()

    fun update(newItems: List<ProductProperty>) {
        val diffResult = DiffUtil.calculateDiff(
            ProductPropertiesDiffCallback(
                items,
                newItems
            )
        )
        items = newItems
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductPropertyViewHolder {
        return when (Type.values()[viewType]) {
            DIVIDER -> DividerViewHolder(parent)
            PROPERTY -> PropertyViewHolder(parent)
            COMPLEX_PROPERTY -> ComplexPropertyViewHolder(parent)
            RATING_BAR -> RatingBarViewHolder(parent)
            PROPERTY_GROUP -> PropertyGroupViewHolder(parent)
            EDITABLE -> EditableViewHolder(parent)
            LINK -> LinkViewHolder(parent)
            READ_MORE -> ReadMoreViewHolder(parent)
            SWITCH -> SwitchViewHolder(parent)
            WARNING -> WarningViewHolder(WCWarningBanner(parent.context))
            BUTTON -> ButtonViewHolder(parent)
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return items[position].type.ordinal
    }

    override fun onBindViewHolder(holder: ProductPropertyViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is PropertyViewHolder -> holder.bind(item as Property)
            is ComplexPropertyViewHolder -> holder.bind(item as ComplexProperty)
            is EditableViewHolder -> holder.bind(item as Editable)
            is PropertyGroupViewHolder -> holder.bind(item as PropertyGroup)
            is RatingBarViewHolder -> holder.bind(item as RatingBar)
            is LinkViewHolder -> holder.bind(item as Link)
            is ReadMoreViewHolder -> holder.bind(item as ReadMore)
            is SwitchViewHolder -> holder.bind(item as Switch)
            is WarningViewHolder -> holder.bind(item as Warning)
            is ButtonViewHolder -> holder.bind(item as Button)
        }
    }
}
