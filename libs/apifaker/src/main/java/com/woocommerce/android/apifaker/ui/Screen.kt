package com.woocommerce.android.apifaker.ui

internal sealed class Screen(val baseRoute: String) {
    object Home : Screen("home") {
        fun route() = baseRoute
    }

    object EndpointDetails : Screen("/endpoint-details") {
        fun route(endpointId: Long) = "$baseRoute/$endpointId"

        fun routeForCreation() = baseRoute
    }
}
