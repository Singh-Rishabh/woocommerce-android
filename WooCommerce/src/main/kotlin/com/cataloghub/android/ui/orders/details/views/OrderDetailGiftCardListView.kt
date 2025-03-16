package com.cataloghub.android.ui.orders.details.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.card.MaterialCardView
import com.cataloghub.android.R
import com.cataloghub.android.databinding.OrderDetailGiftCardListBinding
import com.cataloghub.android.model.GiftCardSummary
import com.cataloghub.android.ui.orders.details.adapter.OrderDetailGiftCardListAdapter
import com.cataloghub.android.util.CurrencyFormatter
import com.cataloghub.android.widgets.AlignedDividerDecoration

class OrderDetailGiftCardListView @JvmOverloads constructor(
    ctx: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(ctx, attrs, defStyleAttr) {
    private val binding = OrderDetailGiftCardListBinding.inflate(LayoutInflater.from(ctx), this)
    fun updateGiftCardList(
        giftCards: List<GiftCardSummary>,
        currencyFormatter: CurrencyFormatter,
        currencyCode: String
    ) {
        val giftCardsAdapter =
            binding.giftCardsItems.adapter as? OrderDetailGiftCardListAdapter
                ?: OrderDetailGiftCardListAdapter(currencyFormatter, currencyCode).also {
                    binding.giftCardsItems.apply {
                        setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(context)
                        itemAnimator = DefaultItemAnimator()
                        adapter = it
                        if (itemDecorationCount == 0) {
                            addItemDecoration(
                                AlignedDividerDecoration(
                                    context,
                                    DividerItemDecoration.VERTICAL,
                                    R.id.gift_card_code
                                )
                            )
                        }
                    }
                }
        giftCardsAdapter.giftCardList = giftCards
    }
}
