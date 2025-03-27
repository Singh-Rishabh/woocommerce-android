package com.cataloghub.android.ui.orders.details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.databinding.OrderDetailShippingLabelListBinding
import com.cataloghub.android.model.ShippingLabel
import com.cataloghub.android.tools.ProductImageMap
import com.cataloghub.android.ui.orders.OrderProductActionListener
import com.cataloghub.android.ui.orders.details.adapter.OrderDetailShippingLabelsAdapter
import com.cataloghub.android.ui.orders.details.adapter.OrderDetailShippingLabelsAdapter.OnShippingLabelClickListener
import java.math.BigDecimal

class OrderDetailShippingLabelsView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(ctx, attrs, defStyleAttr) {
    private val binding = OrderDetailShippingLabelListBinding.inflate(LayoutInflater.from(ctx), this, true)

    fun updateShippingLabels(
        shippingLabels: List<ShippingLabel>,
        productImageMap: ProductImageMap,
        formatCurrencyForDisplay: (BigDecimal) -> String,
        productClickListener: OrderProductActionListener,
        shippingLabelClickListener: OnShippingLabelClickListener
    ) {
        val viewAdapter = binding.shippingLabelList.adapter as? OrderDetailShippingLabelsAdapter
            ?: OrderDetailShippingLabelsAdapter(
                formatCurrencyForDisplay = formatCurrencyForDisplay,
                productImageMap = productImageMap,
                listener = shippingLabelClickListener,
                productClickListener = productClickListener
            )
        binding.shippingLabelList.apply {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator()
            adapter = viewAdapter
        }
        viewAdapter.shippingLabels = shippingLabels
    }
}
