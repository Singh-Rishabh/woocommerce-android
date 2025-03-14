package com.woocommerce.android.ui.woopos.home.items.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.WooPosPreview
import com.woocommerce.android.ui.woopos.common.composeui.component.ShadowType
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosCard
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosText
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosCornerRadius
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosElevation
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosSpacing
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosTheme
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.WooPosTypography
import com.woocommerce.android.ui.woopos.common.composeui.designsystem.toAdaptivePadding
import com.woocommerce.android.ui.woopos.home.items.WooPosItem
import com.woocommerce.android.ui.woopos.home.items.WooPosItemCard

@Composable
fun WooPosItemsSearchScreen(
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<WooPosItemsSearchViewModel>()
    val state = viewModel.viewState.collectAsState().value
    WooPosItemsSearchScreen(
        modifier = modifier,
        state = state,
    )
}

@Composable
private fun WooPosItemsSearchScreen(
    modifier: Modifier = Modifier,
    state: WooPosItemsSearchViewState,
) {
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        when (state) {
            is WooPosItemsSearchViewState.EmptySearchQuery -> {
                WooPosItemsEmptySearchQueryState(state)
            }

            is WooPosItemsSearchViewState.Content -> {
                // TODO: Implement the content state
            }

            WooPosItemsSearchViewState.Empty -> {
            }
        }
    }
}

@Composable
private fun WooPosItemsEmptySearchQueryState(
    state: WooPosItemsSearchViewState.EmptySearchQuery
) {
    val scrollState = rememberScrollState()
    Column(
        Modifier
            .fillMaxHeight()
            .verticalScroll(scrollState)
            .padding(
                start = WooPosSpacing.None.value.toAdaptivePadding(),
                end = WooPosSpacing.None.value.toAdaptivePadding(),
                top = WooPosSpacing.Small.value.toAdaptivePadding(),
                bottom = WooPosSpacing.None.value.toAdaptivePadding(),
            )
    ) {
        if (state.popularItems.isNotEmpty()) {
            PopularItemsSection(state.popularItems)

            Spacer(modifier = Modifier.height(WooPosSpacing.Large.value))
        }

        if (state.recentSearches.isNotEmpty()) {
            RecentSearchesSection(state)
        }

        @Suppress("WooPosDesignSystemSpacingUsageRule")
        Spacer(modifier = Modifier.height(104.dp))
    }
}

@Composable
private fun PopularItemsSection(popularItems: List<WooPosItem>) {
    WooPosText(
        text = "Popular Items",
        style = WooPosTypography.BodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value))

    popularItems.forEach { popularItem ->
        val itemContentDescription = stringResource(
            id = R.string.woopos_product_item_content_description,
            popularItem.name,
            popularItem.price
        )

        WooPosItemCard(
            modifier = Modifier,
            itemContentDescription = itemContentDescription,
            onItemClicked = { },
            item = popularItem,
        )

        Spacer(modifier = Modifier.height(WooPosSpacing.Small.value))
    }
}

@Composable
private fun RecentSearchesSection(state: WooPosItemsSearchViewState.EmptySearchQuery) {
    WooPosText(
        text = "Recent Searches",
        style = WooPosTypography.BodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
    )

    Spacer(modifier = Modifier.height(WooPosSpacing.Medium.value))

    state.recentSearches.forEach { recentSearch ->
        WooPosCard(
            shape = RoundedCornerShape(WooPosCornerRadius.Medium.value),
            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            elevation = WooPosElevation.Medium,
            shadowType = ShadowType.Soft,
        ) {
            Row(
                modifier = Modifier
                    .clickable { }
                    .height(112.dp)
                    .fillMaxWidth()
                    .padding(WooPosSpacing.Medium.value.toAdaptivePadding()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WooPosText(
                    text = recentSearch,
                    style = WooPosTypography.BodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(WooPosSpacing.Small.value))
    }
}

@WooPosPreview
@Composable
fun WooPosItemsEmptySearchQueryStatePreview() {
    WooPosTheme {
        WooPosItemsSearchScreen(
            state = WooPosItemsSearchViewState.EmptySearchQuery(
                popularItems = listOf<WooPosItem>(
                    WooPosItem.SimpleProduct(
                        id = 1,
                        name = "Popular Item 1",
                        price = "10.0$",
                        imageUrl = "https://example.com/image1.jpg",
                    ),
                    WooPosItem.SimpleProduct(
                        id = 2,
                        name = "Popular Item 2",
                        price = "20.0$",
                        imageUrl = "https://example.com/image2.jpg",
                    ),
                    WooPosItem.Variation(
                        id = 3,
                        name = "Popular Item 3",
                        productId = 1,
                        price = "30.0$",
                        imageUrl = "https://example.com/image3.jpg",
                    ),
                ),
                recentSearches = listOf("T-shirt", "Jeans", "Shoes"),
            )
        )
    }
}
