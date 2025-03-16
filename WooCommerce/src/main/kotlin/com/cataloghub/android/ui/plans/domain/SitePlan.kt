package com.cataloghub.android.ui.plans.domain

import com.cataloghub.android.R
import com.cataloghub.android.viewmodel.ResourceProvider
import java.time.ZonedDateTime

data class SitePlan(
    val name: String,
    val expirationDate: ZonedDateTime,
    val type: Type,
) {
    fun generateFormattedPlanName(resourceProvider: ResourceProvider): String {
        return if (type == Type.FREE_TRIAL) {
            resourceProvider.getString(R.string.subscription_free_trial)
        } else {
            name.removePrefix(WP_PREFIX)
                .removePrefix(WOO_EXPRESS_PREFIX)
                .trimIndent()
        }
    }

    enum class Type {
        FREE_TRIAL, OTHER
    }

    companion object {
        val EMPTY = SitePlan("", ZonedDateTime.now(), Type.OTHER)
        const val WP_PREFIX = "WordPress.com"
        const val WOO_EXPRESS_PREFIX = "Woo Express:"
    }
}
