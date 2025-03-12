package com.woocommerce.android.ui.woopos.common.composeui.component

import android.annotation.SuppressLint
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosSearchInputState.Open.Input
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosCornerRadius
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosSpacing
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosTheme
import kotlinx.coroutines.delay

private val BUTTON_SIZE = 48.dp

@Composable
fun WooPosSearchInput(
    modifier: Modifier = Modifier,
    state: WooPosSearchInputState = WooPosSearchInputState.Closed,
    onEvent: (WooPosSearchUIEvent) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = WooPosSpacing.XSmall.value)
            .height(BUTTON_SIZE + WooPosSpacing.XSmall.value * 2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        when (state) {
            is WooPosSearchInputState.Open -> {
                AnimatedSearchInput(
                    state = state,
                    onEvent = onEvent,
                )
            }

            WooPosSearchInputState.Closed -> {
                SearchButton(onEvent = onEvent)
            }
        }
    }
}

@Composable
fun SearchButton(onEvent: (WooPosSearchUIEvent) -> Unit) {
    OutlinedIconButton(
        onClick = { onEvent(WooPosSearchUIEvent.Search("")) },
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.size(BUTTON_SIZE),
        shape = CircleShape,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = stringResource(
                R.string.woopos_search_products
            )
        )
    }
}

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
private fun AnimatedSearchInput(
    state: WooPosSearchInputState.Open,
    onEvent: (WooPosSearchUIEvent) -> Unit,
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        val maxWidthPx = maxWidth
        val focusRequester = remember { FocusRequester() }
        var isExpanded by remember { mutableStateOf(false) }
        var isClosing by remember { mutableStateOf(false) }

        val transition = updateTransition(
            targetState = if (isClosing) false else isExpanded,
            label = "searchTransition"
        )

        val animationTime = 400L

        val width by transition.animateDp(
            label = "width",
            transitionSpec = {
                tween(
                    durationMillis = animationTime.toInt(),
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
                )
            }
        ) { expanded ->
            if (expanded) maxWidthPx else BUTTON_SIZE
        }

        val cornerRadius by transition.animateDp(
            label = "cornerRadius",
            transitionSpec = {
                tween(
                    durationMillis = animationTime.toInt(),
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f)
                )
            }
        ) { expanded ->
            if (expanded) WooPosCornerRadius.Medium.value else BUTTON_SIZE / 2
        }

        val height by transition.animateDp(
            label = "height",
            transitionSpec = {
                tween(
                    durationMillis = animationTime.toInt(),
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.2f, 1.0f)
                )
            }
        ) { expanded ->
            if (expanded) 56.dp else BUTTON_SIZE
        }

        val iconAlpha by transition.animateFloat(
            label = "iconAlpha",
            transitionSpec = {
                tween(
                    durationMillis = (animationTime * 0.7).toInt(),
                    easing = FastOutSlowInEasing,
                    delayMillis = if (isClosing) 0 else (animationTime * 0.3).toInt()
                )
            }
        ) { expanded ->
            if (expanded) 1f else 0f
        }

        val searchIconAlpha by transition.animateFloat(
            label = "searchIconAlpha",
            transitionSpec = {
                tween(
                    durationMillis = (animationTime * 0.5).toInt(),
                    easing = FastOutSlowInEasing
                )
            }
        ) { expanded ->
            if (expanded) 0f else 1f
        }

        val backgroundColor by transition.animateColor(
            label = "backgroundColor",
            transitionSpec = {
                tween(
                    durationMillis = animationTime.toInt(),
                    easing = FastOutSlowInEasing
                )
            }
        ) { expanded ->
            if (expanded) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surface.copy(alpha = 0f)
        }

        val (hint, query) = when (state.input) {
            is Input.Query -> "" to state.input.text
            is Input.Hint -> state.input.text to ""
        }

        LaunchedEffect(isClosing) {
            if (isClosing) {
                delay(animationTime)
                onEvent(WooPosSearchUIEvent.Close)
            }
        }

        OutlinedTextField(
            value = query,
            onValueChange = { onEvent(WooPosSearchUIEvent.Search(it)) },
            modifier = Modifier
                .width(width)
                .height(height)
                .focusRequester(focusRequester),
            placeholder = {
                Text(
                    text = hint,
                    modifier = Modifier.alpha(iconAlpha)
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            shape = RoundedCornerShape(cornerRadius),
            keyboardActions = KeyboardActions(
                onSearch = { onEvent(WooPosSearchUIEvent.Search(query)) }
            ),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.onBackground,
                unfocusedContainerColor = backgroundColor,
                focusedContainerColor = backgroundColor
            ),
            leadingIcon = {
                IconButton(
                    onClick = {
                        isClosing = true
                    },
                    modifier = Modifier.alpha(iconAlpha)
                ) {
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
                    state.isLoading -> {
                        WooPosCircularLoadingIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .alpha(iconAlpha)
                        )
                    }

                    query.isNotEmpty() -> {
                        IconButton(
                            onClick = { onEvent(WooPosSearchUIEvent.Clear) },
                            modifier = Modifier.alpha(iconAlpha)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(
                                    R.string.woopos_search_back_content_description
                                ),
                            )
                        }
                    }

                    else -> {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier
                                .size(24.dp)
                                .alpha(searchIconAlpha)
                        )
                    }
                }
            }
        )

        LaunchedEffect(Unit) {
            isExpanded = true
            delay(animationTime.toLong())
            focusRequester.requestFocus()
        }
    }
}

sealed class WooPosSearchInputState {
    data class Open(val input: Input, val isLoading: Boolean) : WooPosSearchInputState() {
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
    WooPosTheme {
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
}

@WooPosPreview
@Composable
fun WooPosSearchInputClosedPreview() {
    WooPosTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(WooPosSpacing.Small.value)
        ) {
            WooPosSearchInput(
                state = WooPosSearchInputState.Closed,
                onEvent = {}
            )
        }
    }
}
