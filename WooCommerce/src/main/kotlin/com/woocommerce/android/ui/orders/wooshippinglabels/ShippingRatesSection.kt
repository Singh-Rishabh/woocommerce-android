package com.woocommerce.android.ui.orders.wooshippinglabels

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.component.SelectionCheck
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.util.StringUtils
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
internal fun ShippingRatesCard(
    selected: ShippingRate?,
    onSelectedChange: (ShippingRate) -> Unit = {},
    shippingRates: Map<Carrier, List<ShippingRate>>,
    signatureRequired: SignatureRequired?,
    onSelectedSignatureChange: (SignatureRequired?) -> Unit,
    signatureRequiredOptions: List<SignatureRequired>,
    modifier: Modifier = Modifier
) {
    var selectedSortOption by remember { mutableStateOf(ShippingSortOption.CHEAPEST) }
    Column(modifier = modifier) {
        ShippingRatesHeader(
            selectedSortOption = selectedSortOption,
            onSortOptionSelected = { selectedSortOption = it },
            modifier = Modifier.padding(start = dimensionResource(R.dimen.major_100))
        )
        ShippingRates(
            selected = selected,
            onSelectedChange = onSelectedChange,
            shippingRates = shippingRates,
            signatureRequired = signatureRequired,
            onSelectedSignatureChange = onSelectedSignatureChange,
            signatureRequiredOptions = signatureRequiredOptions
        )
    }
}

@Preview
@Composable
private fun ShippingRatesCardPreview() {
    val rates = generateShippingRates()
    val selected = rates.values.first().first()
    WooThemeWithBackground {
        ShippingRatesCard(
            selected = selected,
            shippingRates = generateShippingRates(),
            signatureRequired = null,
            onSelectedSignatureChange = {},
            signatureRequiredOptions = listOf(
                SignatureRequired("Signature Required", "$10.00"),
                SignatureRequired("Adult Signature Required", "$15.00")
            )
        )
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
    selected: ShippingRate?,
    onSelectedChange: (ShippingRate) -> Unit = {},
    shippingRates: Map<Carrier, List<ShippingRate>>,
    signatureRequired: SignatureRequired?,
    onSelectedSignatureChange: (SignatureRequired?) -> Unit,
    signatureRequiredOptions: List<SignatureRequired>,
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
                    carrier.logoRes?.let { CarrierLogo(it) }
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

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val carrier = carriers[page]
        val rates = shippingRates[carrier] ?: emptyList()
        Column {
            rates.forEach { rate ->
                ShippingRateItem(
                    carrier = carrier,
                    shippingRate = rate,
                    isSelected = selected == rate,
                    signatureRequired = signatureRequired,
                    onSelectedSignatureChange = onSelectedSignatureChange,
                    signatureRequiredOptions = signatureRequiredOptions,
                    modifier = Modifier.clickable { onSelectedChange(rate) }
                )
            }
        }
    }
}

@Composable
private fun CarrierLogo(
    @DrawableRes resId: Int,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = resId),
        contentDescription = null,
        modifier = modifier
            .size(24.dp)
            .clip(RoundedCornerShape(5.dp)),
        tint = Color.Unspecified
    )
}

@Composable
private fun ShippingRateItem(
    carrier: Carrier,
    shippingRate: ShippingRate,
    isSelected: Boolean,
    signatureRequired: SignatureRequired?,
    onSelectedSignatureChange: (SignatureRequired?) -> Unit,
    signatureRequiredOptions: List<SignatureRequired>,
    modifier: Modifier = Modifier
) {
    val borderWidth = if (isSelected) {
        2.dp
    } else {
        1.dp
    }
    val borderColor = if (isSelected) {
        MaterialTheme.colors.primary
    } else {
        colorResource(R.color.divider_color)
    }

    val backgroundColor = if (isSelected) {
        animateColorAsState(targetValue = Color(0xFFF2EDFF), label = "colorAnimation")
    } else {
        animateColorAsState(targetValue = MaterialTheme.colors.surface, label = "colorAnimation")
    }

    RoundedCornerBoxWithBorder(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp),
        innerModifier = modifier,
        backgroundColor = backgroundColor.value,
        borderWidth = borderWidth,
        borderColor = borderColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            carrier.logoRes?.let {
                CarrierLogo(resId = it)
            }
            Column(modifier = Modifier.animateContentSize()) {
                Row {
                    Text(
                        text = shippingRate.name,
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    )
                    Text(
                        text = shippingRate.rate,
                        style = MaterialTheme.typography.body1,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (isSelected) {
                    ShippingRateItemExpandedDescription(
                        shippingRate = shippingRate,
                        signatureRequired = signatureRequired,
                        onSelectedSignatureChange = onSelectedSignatureChange,
                        signatureRequiredOptions = signatureRequiredOptions,
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 16.dp)
                    )
                } else {
                    Text(
                        text = getShippingRateFormattedDescription(LocalContext.current, shippingRate),
                        style = MaterialTheme.typography.body2,
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp, end = 16.dp)
                    )
                }
            }
        }
    }
}

private fun getShippingRateFormattedDescription(
    context: Context,
    shippingRate: ShippingRate
): AnnotatedString {
    return buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(shippingRate.getEstimatedDays(context))
        }
        val options = shippingRate.getIncludedOptions(context)
        if (options.isNotEmpty()) {
            append(" • ")
            val include = context.getString(
                R.string.shipping_label_rate_included_options,
                options.joinToString().lowercase()
            )
            append(include)
        }
    }
}

@Composable
private fun ShippingRateItemExpandedDescription(
    shippingRate: ShippingRate,
    signatureRequired: SignatureRequired?,
    onSelectedSignatureChange: (SignatureRequired?) -> Unit,
    signatureRequiredOptions: List<SignatureRequired>,
    modifier: Modifier = Modifier
) {
    val options = shippingRate.getIncludedOptions(LocalContext.current)
    Column(modifier = modifier) {
        Text(
            text = shippingRate.getEstimatedDays(LocalContext.current),
            style = MaterialTheme.typography.body2,
            modifier = Modifier.padding(top = 8.dp, end = 16.dp, bottom = 8.dp),
            fontWeight = FontWeight.Bold
        )
        options.forEach {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = null,
                    tint = MaterialTheme.colors.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = it.capitalize(Locale.current),
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        SelectSignatureRequired(
            signatureRequired = signatureRequired,
            onSelectedSignatureChange = onSelectedSignatureChange,
            signatureRequiredOptions = signatureRequiredOptions,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun SelectSignatureRequired(
    signatureRequired: SignatureRequired?,
    onSelectedSignatureChange: (SignatureRequired?) -> Unit,
    signatureRequiredOptions: List<SignatureRequired>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        signatureRequiredOptions.forEach { option ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        val isSelected = signatureRequired == option
                        val value = if (isSelected) null else option
                        onSelectedSignatureChange(value)
                    }
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
            ) {
                SelectionCheck(
                    isSelected = signatureRequired == option,
                    onSelectionChange = null,
                    modifier = Modifier.padding(end = 4.dp)
                )
                Text(
                    text = option.name,
                    style = MaterialTheme.typography.body1,
                )
            }
        }
    }
}

fun ShippingRate.getEstimatedDays(context: Context): String {
    return StringUtils.getQuantityString(
        context = context,
        quantity = deliveryDays,
        default = R.string.shipping_label_shipping_carrier_rates_delivery_estimate_many,
        one = R.string.shipping_label_shipping_carrier_rates_delivery_estimate_one
    )
}

fun ShippingRate.getIncludedOptions(context: Context): List<String> {
    val options = mutableListOf<String>()
    if (tracking) {
        val tracking = context.getString(
            R.string.shipping_label_rate_included_options_tracking
        )
        options.add(tracking)
    }
    if (insurance != null) {
        val insurance = context.getString(
            R.string.shipping_label_rate_included_options_insurance,
            context.getString(R.string.shipping_label_rate_insurance_up_to, insurance)
        )
        options.add(insurance)
    }
    if (freePickup) {
        val freePickup = context.getString(
            R.string.shipping_label_rate_included_options_free_pickup
        )
        options.add(freePickup)
    }
    return options
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
    val name: String,
    val rate: String,
    val currency: String,
    val deliveryDays: Int,
    val insurance: String?,
    val tracking: Boolean,
    val freePickup: Boolean
)

data class SignatureRequired(
    val name: String,
    val amount: String,
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

    return carriers.associateWith {
        generateRates(
            it.name,
            Random(0).nextInt(from = 3, until = 10)
        )
    }
}

fun generateRates(carrier: String, number: Int): List<ShippingRate> {
    return List(number) {
        ShippingRate(
            name = "$carrier - Ground Advantage Express",
            rate = "$${(it + 1) * 2}.00",
            currency = "USD",
            deliveryDays = it,
            insurance = "$100.00",
            tracking = true,
            freePickup = true
        )
    }
}
