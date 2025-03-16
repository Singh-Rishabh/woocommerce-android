package com.cataloghub.android.model

import com.cataloghub.android.extensions.convertedFrom

data class SessionStat(
    val ordersCount: Int,
    val visitorsCount: Int
) {
    val conversionRate: String
        get() = ordersCount convertedFrom visitorsCount

    companion object {
        val EMPTY = SessionStat(
            ordersCount = 0,
            visitorsCount = 0
        )
    }
}
