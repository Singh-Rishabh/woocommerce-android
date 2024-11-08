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
    fun `initial state should be ItemListScreen`() = testBlocking {
        assertEquals(LeftPaneNavigator.LeftPaneScreen.ItemListScreen, leftPaneNavigator.leftPaneScreen.first())
    }
}
