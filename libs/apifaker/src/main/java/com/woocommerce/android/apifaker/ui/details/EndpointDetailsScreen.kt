package com.woocommerce.android.apifaker.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.woocommerce.android.apifaker.models.ApiType
import com.woocommerce.android.apifaker.models.MockedEndpoint
import com.woocommerce.android.apifaker.models.Request
import com.woocommerce.android.apifaker.models.Response
import com.woocommerce.android.apifaker.ui.DropDownMenu

@Composable
internal fun EndpointDetailsScreen(
    viewModel: EndpointDetailsViewModel,
    navController: NavController
) {
    EndpointDetailsScreen(
        state = viewModel.state,
        navController = navController,
        onEndpointTypeChanged = viewModel::onEndpointTypeChanged
    )
}

@Composable
private fun EndpointDetailsScreen(
    state: MockedEndpoint,
    navController: NavController,
    onEndpointTypeChanged: (ApiType) -> Unit = {},
    onPathChanged: (String) -> Unit = {},
    onBodyChanged: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Endpoint Definition") },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { /*TODO*/ }) {
                        Text(text = "Save")
                    }
                }
            )
        },
        backgroundColor = MaterialTheme.colors.surface
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            RequestDefinitionSection(
                request = state.request,
                onEndpointTypeChanged = onEndpointTypeChanged,
                onPathChanged = onPathChanged,
                onBodyChanged = onBodyChanged,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun RequestDefinitionSection(
    request: Request,
    onEndpointTypeChanged: (ApiType) -> Unit,
    onPathChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        DropDownMenu(
            label = "Type",
            currentValue = request.type,
            values = ApiType.defaultValues(),
            onValueChange = onEndpointTypeChanged,
            formatter = ApiType::label,
            modifier = Modifier.fillMaxWidth()
        )
        if (request.type is ApiType.Custom) {
            TextField(
                label = { Text(text = "Host (without scheme)") },
                value = request.type.host,
                onValueChange = { onEndpointTypeChanged(request.type.copy(host = it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private val ApiType.label
    get() = when (this) {
        ApiType.WPApi -> "WordPress REST API"
        ApiType.WPCom -> "WordPress.com REST API"
        is ApiType.Custom -> "Custom"
    }

@Composable
@Preview
private fun EndpointDetailsScreenPreview() {
    Surface(color = MaterialTheme.colors.background) {
        EndpointDetailsScreen(
            state = MockedEndpoint(
                request = Request(
                    type = ApiType.Custom("https://example.com"),
                    path = "/wc/v3/products",
                    body = "%"
                ),
                response = Response(
                    statusCode = 200,
                    body = ""
                )
            ),
            navController = rememberNavController()
        )
    }
}
