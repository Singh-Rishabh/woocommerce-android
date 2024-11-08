package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PageTab
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PageType.CARRIER
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PageType.CUSTOM
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PageType.SAVED
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms.WooShippingCarrierPackageScreen
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms.WooShippingCustomPackageCreationScreen
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.forms.WooShippingSavedPackageScreen

@Composable
fun WooShippingLabelPackageCreationScreen(
    viewModel: WooShippingLabelPackageCreationViewModel
) {
    val viewState = viewModel.viewState.observeAsState()
    WooShippingLabelPackageCreationScreen(
        tabs = viewState.value?.pageTabs.orEmpty(),
        createCustomPackageScreen = { WooShippingCustomPackageCreationScreen(viewModel) },
        createCarrierPackageScreen = { WooShippingCarrierPackageScreen() },
        createSavedPackageScreen = { WooShippingSavedPackageScreen(viewModel) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WooShippingLabelPackageCreationScreen(
    modifier: Modifier = Modifier,
    tabs: List<PageTab>,
    createCustomPackageScreen: @Composable () -> Unit,
    createCarrierPackageScreen: @Composable () -> Unit,
    createSavedPackageScreen: @Composable () -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val pagerState = rememberPagerState { tabs.size }
    LaunchedEffect(key1 = tabIndex) {
        pagerState.animateScrollToPage(tabIndex)
    }
    LaunchedEffect(key1 = pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            tabIndex = pagerState.currentPage
        }
    }

    Scaffold(
        topBar = {
            TabRow(
                selectedTabIndex = tabIndex,
                backgroundColor = MaterialTheme.colors.surface,
            ) {
                tabs.forEachIndexed { index, pageTab ->
                    Tab(
                        selectedContentColor = MaterialTheme.colors.onSurface,
                        text = { Text(text = pageTab.title) },
                        selected = tabIndex == index,
                        onClick = { tabIndex = index }
                    )
                }
            }
        },
        content = { paddingValues ->
            HorizontalPager(
                state = pagerState,
                modifier = modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(paddingValues)
                    .fillMaxSize()
            ) { currentPageIndex ->
                when (tabs[currentPageIndex].type) {
                    CUSTOM -> createCustomPackageScreen()
                    CARRIER -> createCarrierPackageScreen()
                    SAVED -> createSavedPackageScreen()
                }
            }
        }
    )
}

@Preview
@Composable
fun WooShippingLabelsPackageCreationScreenPreview() {
    WooThemeWithBackground {
        WooShippingLabelPackageCreationScreen(
            tabs = listOf(
                PageTab("Custom", CUSTOM),
                PageTab("Carrier", CARRIER),
                PageTab("Saved", SAVED)
            ),
            createCustomPackageScreen = {
                WooShippingCustomPackageCreationScreen(
                    packageType = "Envelope",
                    packageLength = "10",
                    packageWidth = "10",
                    packageHeight = "10",
                    isAddPackageEnabled = true,
                    onAddPackageClick = {},
                    onPackageTypeClick = {},
                    onLengthChange = {},
                    onWidthChange = {},
                    onHeightChange = {},
                    onSavePackageChanged = { }
                )
            },
            createSavedPackageScreen = {
                WooShippingSavedPackageScreen(
                    savedPackages = listOf(
                        PackageData(
                            type = PackageType.ENVELOPE,
                            name = "Small Flat Rate Box",
                            description = "USPS Priority Mail Flat Rate Boxes",
                            length = "10",
                            width = "10",
                            height = "10",
                            isSelected = true
                        ),
                        PackageData(
                            type = PackageType.BOX,
                            name = "Small Flat Rate Box",
                            description = "Custom package",
                            length = "20",
                            width = "20",
                            height = "20",
                            isSelected = false
                        ),
                        PackageData(
                            type = PackageType.BOX,
                            name = "Small Flat Rate Box",
                            description = "DHL Express",
                            length = "30",
                            width = "30",
                            height = "30",
                            isSelected = false
                        )
                    ),
                    isAddPackageEnabled = true,
                    onAddPackageClick = { },
                    onSavedPackageSelected = { _, _ -> }
                )
            },
            createCarrierPackageScreen = { }
        )
    }
}
