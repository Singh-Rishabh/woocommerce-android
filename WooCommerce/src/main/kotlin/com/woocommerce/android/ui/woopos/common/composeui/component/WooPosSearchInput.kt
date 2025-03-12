package com.woocommerce.android.ui.woopos.common.composeui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosSearchInputState.Open.Input
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosSpacing

@Composable
fun WooPosSearchInput(
    modifier: Modifier = Modifier,
    state: WooPosSearchInputState = WooPosSearchInputState.Closed,
    onEvent: (WooPosSearchUIEvent) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(WooPosSpacing.Small.value)
    ) {
        when (state) {
            is WooPosSearchInputState.Open -> {
                SearchInput(
                    state = state,
                    onEvent = onEvent,
                    modifier = modifier
                )
            }

            WooPosSearchInputState.Closed -> {
                Spacer(modifier.weight(1f))
                SearchButton(
                    onEvent = onEvent,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
fun SearchButton(
    onEvent: (WooPosSearchUIEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface
    ) {
        IconButton(
            onClick = { onEvent(WooPosSearchUIEvent.Search("")) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(
                    R.string.woopos_search_button_content_description
                ),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SearchInput(
    state: WooPosSearchInputState.Open,
    onEvent: (WooPosSearchUIEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }
    var isExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isExpanded = true
        focusRequester.requestFocus()
    }

    val transition = updateTransition(
        targetState = isExpanded,
        label = "searchTransition"
    )

    val widthFactor by transition.animateFloat(
        label = "widthFactor",
        transitionSpec = {
            tween(
                durationMillis = 300,
                easing = FastOutSlowInEasing
            )
        }
    ) { expanded ->
        if (expanded) 1f else 0f
    }

    val query = when (state.input) {
        is Input.Query -> state.input.text
        is Input.Hint -> ""
    }

    val hint = when (state.input) {
        is Input.Query -> ""
        is Input.Hint -> state.input.text
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(widthFactor)
                .height(56.dp)
                .clip(MaterialTheme.shapes.medium),
            color = MaterialTheme.colorScheme.surface
        ) {
            if (widthFactor > 0.5f) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { onEvent(WooPosSearchUIEvent.Search(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text(hint) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { onEvent(WooPosSearchUIEvent.Search(query)) }
                    ),
                    leadingIcon = {
                        IconButton(onClick = { onEvent(WooPosSearchUIEvent.Close) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(
                                    R.string.woopos_search_back_content_description
                                ),
                            )
                        }
                    },
                    trailingIcon = {
                        when {
                            state.loading -> {
                                WooPosCircularLoadingIndicator(
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            query.isNotEmpty() -> {
                                IconButton(onClick = { onEvent(WooPosSearchUIEvent.Clear) }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(
                                            R.string.woopos_search_back_content_description
                                        ),
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }

        if (widthFactor < 0.1f) {
            SearchButton(
                onEvent = onEvent,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

sealed class WooPosSearchInputState {
    data class Open(val input: Input, val loading: Boolean) : WooPosSearchInputState() {
        sealed class Input(val text: String) {
            data class Query(val query: String) : Input(query)
            data class Hint(val hint: String) : Input(hint)
        }
    }

    object Closed : WooPosSearchInputState()
}

sealed class WooPosSearchUIEvent {
    object Clear : WooPosSearchUIEvent()
    data class Search(val query: String) : WooPosSearchUIEvent()
    object Close : WooPosSearchUIEvent()
}

@WooPosPreview
@Composable
fun WooPosSearchInputOpenHintPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WooPosSpacing.Small.value)
    ) {
        WooPosSearchInput(
            state = WooPosSearchInputState.Open(Input.Hint("Search products..."), false),
            onEvent = {}
        )
    }
}

@WooPosPreview
@Composable
fun WooPosSearchInputOpenSearchPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WooPosSpacing.Small.value)
    ) {
        WooPosSearchInput(
            state = WooPosSearchInputState.Open(Input.Query("Search products..."), false),
            onEvent = {}
        )
    }
}

@WooPosPreview
@Composable
fun WooPosSearchInputClosedPreview() {
    WooPosSearchInput(
        state = WooPosSearchInputState.Closed,
        onEvent = {}
    )
}
