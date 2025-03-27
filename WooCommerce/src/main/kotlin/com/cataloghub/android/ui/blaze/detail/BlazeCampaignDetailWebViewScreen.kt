package com.cataloghub.android.ui.blaze.detail

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.cataloghub.android.R
import com.cataloghub.android.ui.common.webview.WebViewAuthenticator
import com.cataloghub.android.ui.compose.component.Toolbar
import com.cataloghub.android.ui.compose.component.web.WCWebView
import org.wordpress.android.fluxc.network.UserAgent

@Composable
fun BlazeCampaignDetailWebViewScreen(
    viewModel: BlazeCampaignDetailWebViewViewModel,
    authenticator: WebViewAuthenticator,
    userAgent: UserAgent,
    onUrlLoaded: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    Scaffold(
        topBar = {
            Toolbar(
                title = stringResource(id = R.string.blaze_campaign_details_title),
                onNavigationButtonClick = onDismiss
            )
        }
    ) { paddingValues ->
        WCWebView(
            url = viewModel.viewState.urlToLoad,
            userAgent = userAgent,
            authenticator = authenticator,
            onUrlLoaded = onUrlLoaded,
            modifier = Modifier.padding(paddingValues)
        )
    }
}
