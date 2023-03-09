package com.woocommerce.android.apifaker.ui

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.woocommerce.android.apifaker.ui.Screen.EndpointDetails
import com.woocommerce.android.apifaker.ui.home.HomeScreen

@Composable
fun ApiFakerNavHost(
    onExit: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route()
    ) {
        composable(Screen.Home.route()) {
            HomeScreen(viewModel = hiltViewModel(), navController = navController, onExit = onExit)
        }
        composable(
            EndpointDetails.baseRoute,
            arguments = listOf(navArgument("endpointId") {
                type = NavType.LongType
                defaultValue = -1
            })
        ) {
            TODO()
        }
    }
}
