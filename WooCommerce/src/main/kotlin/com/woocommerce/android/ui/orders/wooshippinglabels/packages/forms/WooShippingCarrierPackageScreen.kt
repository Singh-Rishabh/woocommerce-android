package com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms

import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LeadingIconTab
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ScrollableTabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.woocommerce.android.R
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.components.WooSavedPackageListItem
import kotlinx.coroutines.launch

@Composable
fun WooShippingCarrierPackageScreen(viewModel: WooShippingLabelPackageCreationViewModel) {
    val viewState by viewModel.viewState.observeAsState()
    WooShippingCarrierPackageScreen(
        carrierPackages = viewState?.carrierPackageSection?.carrierPackages ?: emptyMap(),
        isAddPackageEnabled = viewState?.carrierPackageSection?.hasSelection ?: false,
        onPackageSelected = viewModel::onCarrierPackageSelected,
        onAddPackageClick = viewModel::onAddCarrierPackageClick
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WooShippingCarrierPackageScreen(
    modifier: Modifier = Modifier,
    carrierPackages: Map<Carrier, List<CarrierPackageGroup>>,
    onPackageSelected: (PackageData, Boolean) -> Unit,
    isAddPackageEnabled: Boolean = false,
    onAddPackageClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState { carrierPackages.keys.size }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface)
    ) {
        CarrierTabRow(
            modifier = modifier,
            pagerState = pagerState,
            carriers = carrierPackages.keys.toList()
        )
        PackageListPager(
            modifier = modifier
                .weight(1f),
            pagerState = pagerState,
            carrierPackages = carrierPackages,
            onPackageSelected = onPackageSelected
        )
        Divider()
        Button(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            enabled = isAddPackageEnabled,
            onClick = onAddPackageClick
        ) {
            Text(stringResource(id = R.string.woo_shipping_labels_package_creation_add_package))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CarrierTabRow(
    modifier: Modifier,
    pagerState: PagerState,
    carriers: List<Carrier>
) {
    val scope = rememberCoroutineScope()
    ScrollableTabRow(
        modifier = modifier.fillMaxWidth(),
        selectedTabIndex = pagerState.currentPage,
        edgePadding = dimensionResource(R.dimen.major_100),
        backgroundColor = MaterialTheme.colors.surface,
        contentColor = MaterialTheme.colors.primary,
    ) {
        carriers.forEachIndexed { index, carrier ->
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
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PackageListPager(
    modifier: Modifier,
    pagerState: PagerState,
    carrierPackages: Map<Carrier, List<CarrierPackageGroup>>,
    onPackageSelected: (PackageData, Boolean) -> Unit
) {
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) { page ->
        val carrierForPageIndex = carrierPackages.keys.toList()[page]
        val carrierPackageGroups = carrierPackages[carrierForPageIndex] ?: emptyList()
        PackageList(
            packageGroups = carrierPackageGroups,
            onPackageSelected = onPackageSelected
        )
    }
}

@Composable
private fun PackageList(
    packageGroups: List<CarrierPackageGroup>,
    onPackageSelected: (PackageData, Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        packageGroups.forEach { group ->
            Spacer(modifier = Modifier.height(8.dp))
            PackageListSection(
                sectionHeader = group.groupName,
                packages = group.packages,
                onPackageSelected = onPackageSelected
            )
        }
    }
}

@Composable
private fun PackageListSection(
    sectionHeader: String,
    packages: List<PackageData>,
    onPackageSelected: (PackageData, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(start = 8.dp)
            .wrapContentHeight(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = sectionHeader,
            style = MaterialTheme.typography.body1,
            color = colorResource(id = R.color.color_on_surface_disabled)
        )
        Divider()
        packages.forEach { packageData ->
            WooSavedPackageListItem(
                modifier = Modifier.padding(start = 16.dp),
                packageData = packageData,
                onPackageSelected = onPackageSelected
            )
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

@Preview
@Composable
fun WooShippingCarrierPackageScreenPreview() {
    WooThemeWithBackground {
        WooShippingCarrierPackageScreen(
            carrierPackages = mapOf(
                Carrier(
                    id = "dhl",
                    name = "DHL Express",
                    logoRes = R.drawable.dhl_logo
                ) to listOf(
                    CarrierPackageGroup(
                        groupName = "Group 1",
                        packages = listOf(
                            PackageData(
                                type = PackageType.BOX,
                                name = "Package 1 - Carrier 1",
                                description = "Description 1",
                                length = "10",
                                width = "10",
                                height = "10",
                                isSelected = false
                            ),
                            PackageData(
                                type = PackageType.BOX,
                                name = "Package 2 - Carrier 1",
                                description = "Description 2",
                                length = "20",
                                width = "20",
                                height = "20",
                                isSelected = false
                            )
                        )
                    ),
                    CarrierPackageGroup(
                        groupName = "Group 2",
                        packages = listOf(
                            PackageData(
                                type = PackageType.BOX,
                                name = "Package 3 - Carrier 1",
                                description = "Description 3",
                                length = "30",
                                width = "30",
                                height = "30",
                                isSelected = false
                            ),
                            PackageData(
                                type = PackageType.BOX,
                                name = "Package 4 - Carrier 1",
                                description = "Description 4",
                                length = "40",
                                width = "40",
                                height = "40",
                                isSelected = false
                            )
                        )
                    )
                ),
                Carrier(
                    id = "usps",
                    name = "USPS",
                    logoRes = R.drawable.usps_logo
                ) to listOf(
                    CarrierPackageGroup(
                        groupName = "Group 2",
                        packages = listOf(
                            PackageData(
                                type = PackageType.BOX,
                                name = "Package 1 - Carrier 2",
                                description = "Description 1",
                                length = "10",
                                width = "10",
                                height = "10",
                                isSelected = false
                            ),
                            PackageData(
                                type = PackageType.BOX,
                                name = "Package 2 Carrier - 2",
                                description = "Description 2",
                                length = "20",
                                width = "20",
                                height = "20",
                                isSelected = false
                            )
                        )
                    )
                )
            ),
            onPackageSelected = { _, _ -> }
        )
    }
}
