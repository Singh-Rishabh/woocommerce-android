package com.woocommerce.android.ui.woopos.home.items

import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LeftPaneNavigatorTest : BaseUnitTest() {
    private lateinit var leftPaneNavigator: LeftPaneNavigator

    @Before
    fun setUp() {
        leftPaneNavigator = LeftPaneNavigator()
    }

    @Test
    fun `given initial state, then state should be ItemListScreen`() = testBlocking {
        assertEquals(LeftPaneNavigator.LeftPaneScreen.ItemListScreen, leftPaneNavigator.leftPaneScreen.first())
    }

    @Test
    fun `given navigate to VariationsScreen, then updates leftPaneScreen with VariableProductData`() = testBlocking {
        val sampleVariableProduct = WooPosItemNavigationData.VariableProductData(
            id = 1L,
            name = "Variable Product",
            numOfVariations = 10,
            variationIds = emptyList()
        )

        val expectedScreen = LeftPaneNavigator.LeftPaneScreen.VariationsScreen(sampleVariableProduct)
        leftPaneNavigator.navigateToVariationsScreen(sampleVariableProduct)

        assertEquals(expectedScreen, leftPaneNavigator.leftPaneScreen.first())
    }

    @Test
    fun `given navigate back to ItemListScreen from variations screen, then sets leftPaneScreen to ItemListScreen`() = testBlocking {
        leftPaneNavigator.navigateToVariationsScreen(
            WooPosItemNavigationData.VariableProductData(
                id = 1L,
                name = "Variable Product",
                numOfVariations = 10,
                variationIds = emptyList()
            )
        )

        leftPaneNavigator.navigateBackToItemListScreen()

        assertEquals(LeftPaneNavigator.LeftPaneScreen.ItemListScreen, leftPaneNavigator.leftPaneScreen.first())
    }
}
