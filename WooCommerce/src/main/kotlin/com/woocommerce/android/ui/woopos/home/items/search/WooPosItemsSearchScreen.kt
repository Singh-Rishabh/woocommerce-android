package com.woocommerce.android.ui.woopos.home.items.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosTheme

@Composable
fun WooPosItemsSearchScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<WooPosItemsSearchViewModel>()
    val viewState = viewModel.viewState
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "WooPosItemsSearchScreen",
            modifier = Modifier,
        )
    }
}

@WooPosPreview
@Composable
fun WooPosItemsSearchScreenPreview() {
    WooPosTheme {
        WooPosItemsSearchScreen()
    }
}
