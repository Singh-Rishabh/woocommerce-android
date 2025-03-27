package com.cataloghub.android.ui.orders

import android.content.Context
import androidx.core.content.ContextCompat
import com.cataloghub.android.R
import com.cataloghub.android.model.Subscription
import com.cataloghub.android.widgets.tags.ITag
import com.cataloghub.android.widgets.tags.TagConfig

class SubscriptionStatusTag(private val status: Subscription.Status) : ITag(status.value.trim()) {
    override fun getTagConfiguration(context: Context): TagConfig {
        return TagConfig(context).apply {
            when (status) {
                Subscription.Status.Active -> {
                    tagText = context.getString(R.string.subscription_status_active)
                    bgColor = ContextCompat.getColor(context, R.color.tag_bg_processing)
                }
                Subscription.Status.Cancelled -> tagText = context.getString(R.string.subscription_status_cancelled)
                Subscription.Status.Expired -> {
                    tagText = context.getString(R.string.subscription_status_expired)
                    bgColor = ContextCompat.getColor(context, R.color.tag_bg_failed)
                }
                Subscription.Status.OnHold -> {
                    tagText = context.getString(R.string.subscription_status_on_hold)
                    bgColor = ContextCompat.getColor(context, R.color.tag_bg_on_hold)
                }
                Subscription.Status.PendingCancellation ->
                    tagText = context.getString(R.string.subscription_status_pending_cancellation)
                is Subscription.Status.Custom -> tagText = status.value
            }
        }
    }
}
