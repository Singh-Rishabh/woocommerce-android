package com.woocommerce.android.ui.orders.wooshippinglabels

import androidx.annotation.StringRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
internal fun ShippingRatesCard(
    shippingRates: Map<Carrier, List<ShippingRate>>,
    modifier: Modifier = Modifier
) {
    var selectedSortOption by remember { mutableStateOf(ShippingSortOption.CHEAPEST) }
    Column(modifier = modifier) {
        ShippingRatesHeader(
            selectedSortOption = selectedSortOption,
            onSortOptionSelected = { selectedSortOption = it },
            modifier = Modifier.padding(start = dimensionResource(R.dimen.major_100))
        )
        ShippingRates(shippingRates)
    }
}

@Composable
private fun ShippingRatesHeader(
    selectedSortOption: ShippingSortOption,
    onSortOptionSelected: (ShippingSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.shipping_label_shipping_service_title),
            color = MaterialTheme.colors.onSurface,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )

        SortingDropdownMenu(
            selectedSortOption = selectedSortOption,
            onSortOptionSelected = onSortOptionSelected,
            modifier = Modifier.padding(dimensionResource(R.dimen.major_100))
        )
    }
}

@Preview
@Composable
private fun ShippingRatesHeaderPreview() {
    WooThemeWithBackground {
        ShippingRatesHeader(
            selectedSortOption = ShippingSortOption.CHEAPEST,
            onSortOptionSelected = {},
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Composable
private fun SortingDropdownMenu(
    selectedSortOption: ShippingSortOption,
    onSortOptionSelected: (ShippingSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val options = ShippingSortOption.entries

    Box {
        Row(
            modifier = Modifier
                .clickable { expanded = true }
                .then(modifier),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(selectedSortOption.stringResource),
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.primary
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = stringResource(
                    R.string.sorted_by,
                    stringResource(selectedSortOption.stringResource)
                ),
                tint = MaterialTheme.colors.primary
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.sizeIn(minWidth = 150.dp)
        ) {
            options.forEach { option ->
                DropdownMenuItem(onClick = {
                    onSortOptionSelected(option)
                    expanded = false
                }) {
                    Text(
                        text = stringResource(option.stringResource),
                        style = MaterialTheme.typography.subtitle1
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ShippingRates(
    shippingRates: Map<Carrier, List<ShippingRate>>,
    modifier: Modifier = Modifier,
    tabModifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState { shippingRates.keys.size }
    val scope = rememberCoroutineScope()
    val carriers = shippingRates.keys.toList()

    ScrollableTabRow(
        selectedTabIndex = pagerState.currentPage,
        edgePadding = dimensionResource(R.dimen.major_100),
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.primary,
        modifier = tabModifier
    ) {
        shippingRates.keys.forEachIndexed { index, carrier ->
            val textColor = if (index == pagerState.currentPage) {
                MaterialTheme.colors.primary
            } else {
                colorResource(id = R.color.color_on_surface_medium)
            }
            LeadingIconTab(
                text = {
                    Text(
                        text = carrier.name,
                        color = textColor,
                        style = MaterialTheme.typography.subtitle2
                    )
                },
                icon = {
                    carrier.logoRes?.let {
                        Icon(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            modifier = Modifier
                                .sizeIn(maxWidth = 24.dp)
                                .clip(RoundedCornerShape(5.dp)),
                            tint = Color.Unspecified
                        )
                    }
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                }
            )
        }
    }

    HorizontalPager(state = pagerState, modifier = modifier.fillMaxSize()) { page ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = carriers[page].name,
                fontSize = 32.sp
            )
        }
    }
}

enum class ShippingSortOption(@StringRes val stringResource: Int) {
    CHEAPEST(R.string.shipping_label_shipping_rates_sort_option_cheapest),
    FASTEST(R.string.shipping_label_shipping_rates_sort_option_fastest)
}

data class Carrier(
    val id: String,
    val name: String,
    val logoRes: Int? = null,
)

data class ShippingRate(
    val rate: String,
    val currency: String,
    val deliveryDays: Int,
    val insurance: String?,
    val tracking: Boolean,
    val freePickup: Boolean,
    val signatureRequired: String?,
    val adultSignatureRequired: String?,
)

fun generateShippingRates(): Map<Carrier, List<ShippingRate>> {
    val carriers = listOf(
        Carrier(
            id = "dhl",
            name = "DHL Express",
            logoRes = R.drawable.dhl_logo
        ),
        Carrier(
            id = "usps",
            name = "USPS",
            logoRes = R.drawable.usps_logo
        ),
        Carrier(
            id = "ups",
            name = "UPS",
            logoRes = R.drawable.ups_logo
        ),
        Carrier(
            id = "fedex",
            name = "Fed Ex",
            logoRes = R.drawable.fedex_logo
        ),
        Carrier(
            id = "canadapost",
            name = "Canada Post",
            logoRes = null
        )
    )

    return carriers.associateWith { generateRates(Random(0).nextInt(from = 3, until = 10)) }
}

fun generateRates(number: Int): List<ShippingRate> {
    return List(number) {
        ShippingRate(
            rate = "${(it + 1) * 2}",
            currency = "USD",
            deliveryDays = it,
            insurance = null,
            tracking = false,
            freePickup = false,
            adultSignatureRequired = if (it % 2 == 0) null else "$3.00",
            signatureRequired = "$2.00"
        )
    }
}
