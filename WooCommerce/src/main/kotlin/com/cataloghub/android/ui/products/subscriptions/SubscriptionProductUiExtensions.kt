package com.cataloghub.android.ui.products.subscriptions

import com.cataloghub.android.R
import com.cataloghub.android.model.SubscriptionDetails
import com.cataloghub.android.model.SubscriptionPeriod
import com.cataloghub.android.viewmodel.ResourceProvider

fun SubscriptionDetails.expirationDisplayValue(resProvider: ResourceProvider): String {
    return if (length != null && length > 0) {
        val periodString = period.getPeriodString(resProvider, length)
        resProvider.getString(R.string.subscription_period, length.toString(), periodString)
    } else {
        resProvider.getString(R.string.subscription_never_expire)
    }
}

fun SubscriptionDetails.expirationDisplayOptions(resources: ResourceProvider): Map<String, Int> {
    val options = mutableMapOf(
        resources.getString(R.string.subscription_never_expire) to 0
    )
    for (index in period.getRangeForPeriod() step periodInterval) {
        if (index >= periodInterval) {
            val periodString = period.getPeriodString(resources, index)
            options[resources.getString(R.string.subscription_period, index, periodString)] = index
        }
    }
    return options
}

fun SubscriptionDetails.trialDisplayValue(resources: ResourceProvider): String {
    return if (trialPeriod != null && trialLength != null && trialLength > 0) {
        val periodString = trialPeriod.getPeriodString(resources, trialLength)
        resources.getString(R.string.subscription_period, trialLength.toString(), periodString)
    } else {
        resources.getString(R.string.subscription_no_trial)
    }
}

fun SubscriptionDetails.resetSubscriptionLengthIfThePeriodOrIntervalChanged(
    newPeriod: SubscriptionPeriod?,
    newInterval: Int?,
    newLength: Int?
) = when {
    newPeriod != null && newPeriod != period -> null
    newInterval != null && newInterval != periodInterval -> null
    else -> newLength ?: length
}
