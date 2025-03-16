package com.cataloghub.android.model

import com.cataloghub.android.FeedbackPrefs
import com.cataloghub.android.extensions.greaterThan
import com.cataloghub.android.extensions.pastTimeDeltaFromNowInDays
import com.cataloghub.android.model.FeatureFeedbackSettings.FeedbackState.UNANSWERED
import java.util.Calendar
import java.util.Date

data class FeatureFeedbackSettings(
    val feature: Feature,
    val feedbackState: FeedbackState = UNANSWERED,
    val settingChangeDate: Long = Calendar.getInstance().time.time,
) {
    val key
        get() = feature.toString()

    fun registerItself(feedbackPrefs: FeedbackPrefs) = feedbackPrefs.setFeatureFeedbackSettings(this)

    enum class FeedbackState {
        GIVEN,
        DISMISSED,
        UNANSWERED
    }

    enum class Feature {
        PRODUCT_ADDONS,
        ANALYTICS_HUB,
        ORDER_SHIPPING_LINES
    }

    fun isFeedbackMoreThanDaysAgo(days: Int) = Date(settingChangeDate).pastTimeDeltaFromNowInDays greaterThan days
}
