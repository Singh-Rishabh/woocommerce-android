package com.woocommerce.android.ui.woopos.home.items.search

import app.cash.turbine.test
import com.woocommerce.android.ui.products.ProductTestUtils
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.ParentToChildrenEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.WooPosParentToChildrenEventReceiver
import com.woocommerce.android.ui.woopos.home.items.WooPosItemSelectionViewState
import com.woocommerce.android.ui.woopos.home.items.WooPosItemSelectionViewState.Product
import com.woocommerce.android.ui.woopos.util.WooPosCoroutineTestRule
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal

@ExperimentalCoroutinesApi
class WooPosItemsSearchViewModelTestSelectionViewState {

    @Rule
    @JvmField
    val coroutinesTestRule = WooPosCoroutineTestRule()

    private val mockEmptyStateProvider: WooPosItemsSearchEmptyStateProvider = mock()
    private val mockPriceFormat: WooPosFormatPrice = mock()
    private val mockDataSource: WooPosSearchProductsMockedDataSource = mock()
    private val mockChildToParentEventSender: WooPosChildrenToParentEventSender = mock()
    private val mockParentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver = mock()

    @Test
    fun `given less than max popular items and recent searches, when view model created, then all items are shown`() =
        runTest {
            // GIVEN
            val popularItems = listOf(
                Product.Simple(id = 1, name = "Popular Item 1", price = "$10.0", imageUrl = null),
                Product.Simple(id = 2, name = "Popular Item 2", price = "$15.0", imageUrl = null)
            )
            val recentSearches = listOf(
                "Recent Search 1"
            )

            whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(popularItems)
            whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(recentSearches)
            whenever(mockParentToChildrenEventReceiver.events).thenReturn(emptyFlow())

            // WHEN
            val viewModel = createViewModel()

            // THEN
            viewModel.viewState.test {
                val value = awaitItem()
                assertThat(value).isInstanceOf(WooPosItemsSearchViewState.EmptySearchQuery::class.java)

                val emptySearchQuery = value as WooPosItemsSearchViewState.EmptySearchQuery
                assertThat(emptySearchQuery.popularItems).hasSize(2)
                assertThat(emptySearchQuery.recentSearches).hasSize(1)
            }
        }

    @Test
    fun `given empty popular items and recent searches, when view model created, then empty lists are shown`() =
        runTest {
            // GIVEN
            whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(emptyList())
            whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(emptyList())
            whenever(mockParentToChildrenEventReceiver.events).thenReturn(emptyFlow())

            // WHEN
            val viewModel = createViewModel()

            // THEN
            viewModel.viewState.test {
                val value = awaitItem()
                assertThat(value).isInstanceOf(WooPosItemsSearchViewState.EmptySearchQuery::class.java)

                val emptySearchQuery = value as WooPosItemsSearchViewState.EmptySearchQuery
                assertThat(emptySearchQuery.popularItems).isEmpty()
                assertThat(emptySearchQuery.recentSearches).isEmpty()
            }
        }

    @Test
    fun `given view model initialization, when view model created, then initial state is EmptySearchQuery`() = runTest {
        // GIVEN
        whenever(mockEmptyStateProvider.getPopularItems()).thenAnswer {
            emptyList<WooPosItemSelectionViewState>()
        }
        whenever(mockEmptyStateProvider.getLastSearches()).thenAnswer {
            emptyList<String>()
        }
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(emptyFlow())

        // WHEN
        val viewModel = createViewModel()

        // THEN
        viewModel.viewState.test {
            val value = awaitItem()
            assertThat(value).isInstanceOf(WooPosItemsSearchViewState.EmptySearchQuery::class.java)
        }
    }

    @Test
    fun `given more than max items count, when view model created, then only max items are shown`() = runTest {
        // GIVEN
        val popularItems = listOf(
            Product.Simple(id = 1, name = "Popular Item 1", price = "$10.0", imageUrl = null),
            Product.Simple(id = 2, name = "Popular Item 2", price = "$15.0", imageUrl = null),
            Product.Simple(id = 3, name = "Popular Item 3", price = "$20.0", imageUrl = null),
            Product.Simple(id = 4, name = "Popular Item 4", price = "$25.0", imageUrl = null)
        )
        val recentSearches = listOf(
            "Recent Search 1",
            "Recent Search 2",
            "Recent Search 3",
            "Recent Search 4"
        )
        whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(popularItems)
        whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(recentSearches)
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(emptyFlow())

        // WHEN
        val viewModel = createViewModel()

        // THEN
        viewModel.viewState.test {
            val value = awaitItem()
            assertThat(value).isInstanceOf(WooPosItemsSearchViewState.EmptySearchQuery::class.java)
            val emptySearchQuery = value as WooPosItemsSearchViewState.EmptySearchQuery
            assertThat(emptySearchQuery.popularItems).hasSize(3)
            assertThat(emptySearchQuery.popularItems.map { it.id }).containsExactly(1, 2, 3)
            assertThat(emptySearchQuery.recentSearches).hasSize(3)
            assertThat(emptySearchQuery.recentSearches).containsExactly(
                "Recent Search 1",
                "Recent Search 2",
                "Recent Search 3"
            )
        }
    }

    @Test
    fun `given search query and search results, when view model created, then view state updated accordingly`() = runTest {
        // GIVEN
        val query = "test query"
        val products = listOf(
            ProductTestUtils.generateProduct(
                productId = 1,
                productName = "Test Product",
                amount = "10.0",
                productType = "simple"
            )
        )

        whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(emptyList())
        whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(emptyList())
        whenever(mockDataSource.searchProducts(query)).thenReturn(
            flowOf(
                WooPosSearchProductsMockedDataSource.ProductsResult.Remote(
                    Result.success(products)
                )
            )
        )
        whenever(mockPriceFormat(BigDecimal("10.0"))).thenReturn("$10.0")
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(
            flowOf(ParentToChildrenEvent.SearchEvent.ChangedQuery(query))
        )

        // WHEN
        val viewModel = createViewModel()

        // THEN
        viewModel.viewState.test {
            val initialState = awaitItem()
            assertThat(initialState).isInstanceOf(WooPosItemsSearchViewState.EmptySearchQuery::class.java)

            val contentState = awaitItem()
            assertThat(contentState).isInstanceOf(WooPosItemsSearchViewState.Content::class.java)

            val content = contentState as WooPosItemsSearchViewState.Content
            assertThat(content.items).hasSize(1)
            assertThat((content.items[0] as WooPosItem.Product.Simple).name).isEqualTo("Test Product")
        }

        verify(mockChildToParentEventSender).sendToParent(ChildToParentEvent.SearchEvent.Started)
        verify(mockChildToParentEventSender).sendToParent(ChildToParentEvent.SearchEvent.Finished)
    }

    @Test
    fun `given empty search query, when search query updated, then view state is empty search query`() = runTest {
        // GIVEN
        val emptyQuery = ""

        whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(emptyList())
        whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(emptyList())
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(
            flowOf(ParentToChildrenEvent.SearchEvent.ChangedQuery(emptyQuery))
        )

        // WHEN
        val viewModel = createViewModel()

        // THEN
        viewModel.viewState.test {
            val value = awaitItem()
            assertThat(value).isInstanceOf(WooPosItemsSearchViewState.EmptySearchQuery::class.java)
        }
    }

    @Test
    fun `given valid search query, when search query changed, then view state updated accordingly`() = runTest {
        // GIVEN
        val query = "test query"
        val products = listOf(
            ProductTestUtils.generateProduct(
                productId = 1,
                productName = "Test Product",
                amount = "10.0",
                productType = "simple"
            )
        )

        whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(emptyList())
        whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(emptyList())
        whenever(mockDataSource.searchProducts(query)).thenReturn(
            flowOf(
                WooPosSearchProductsMockedDataSource.ProductsResult.Remote(
                    Result.success(products)
                )
            )
        )
        whenever(mockPriceFormat(BigDecimal("10.0"))).thenReturn("$10.0")
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(
            flowOf(ParentToChildrenEvent.SearchEvent.ChangedQuery(query))
        )

        // WHEN
        val viewModel = createViewModel()
        advanceTimeBy(600)

        // THEN
        viewModel.viewState.test {
            skipItems(0)

            val content = awaitItem() as WooPosItemsSearchViewState.Content
            assertThat(content.items).hasSize(1)
            assertThat((content.items[0] as WooPosItem.Product.Simple).name).isEqualTo("Test Product")
        }

        verify(mockChildToParentEventSender).sendToParent(ChildToParentEvent.SearchEvent.Started)
        verify(mockChildToParentEventSender).sendToParent(ChildToParentEvent.SearchEvent.Finished)
    }

    @Test
    fun `given empty search query, when search query changed to empty, then view state is empty search query`() = runTest {
        // GIVEN
        val emptyQuery = ""

        whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(emptyList())
        whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(emptyList())
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(
            flowOf(ParentToChildrenEvent.SearchEvent.ChangedQuery(emptyQuery))
        )

        // WHEN
        val viewModel = createViewModel()

        // THEN
        viewModel.viewState.test {
            val value = awaitItem()
            assertThat(value).isInstanceOf(WooPosItemsSearchViewState.EmptySearchQuery::class.java)
        }
    }

    @Test
    fun `given search with no results, when search performed, then view state is empty`() = runTest {
        // GIVEN
        val query = "no results"

        whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(emptyList())
        whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(emptyList())
        whenever(mockDataSource.searchProducts(query)).thenReturn(
            flowOf(
                WooPosSearchProductsMockedDataSource.ProductsResult.Remote(
                    Result.success(emptyList())
                )
            )
        )
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(
            flowOf(ParentToChildrenEvent.SearchEvent.ChangedQuery(query))
        )

        // WHEN
        val viewModel = createViewModel()
        advanceTimeBy(600)

        // THEN
        viewModel.viewState.test {
            assertThat(awaitItem()).isEqualTo(WooPosItemsSearchViewState.Empty)
        }
    }

    @Test
    fun `given search fails, when search performed, then view state is error`() = runTest {
        // GIVEN
        val query = "fail"

        whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(emptyList())
        whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(emptyList())
        whenever(mockDataSource.searchProducts(query)).thenReturn(
            flowOf(
                WooPosSearchProductsMockedDataSource.ProductsResult.Remote(
                    Result.failure(Exception("Search failed"))
                )
            )
        )
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(
            flowOf(ParentToChildrenEvent.SearchEvent.ChangedQuery(query))
        )

        // WHEN
        val viewModel = createViewModel()
        advanceTimeBy(600)

        // THEN
        viewModel.viewState.test {
            assertThat(awaitItem()).isEqualTo(WooPosItemsSearchViewState.Error)
        }

        verify(mockChildToParentEventSender).sendToParent(ChildToParentEvent.SearchEvent.Started)
        verify(mockChildToParentEventSender).sendToParent(ChildToParentEvent.SearchEvent.Finished)
    }

    @Test
    fun `given variable product in search results, when search performed, then mapped correctly to view state`() = runTest {
        // GIVEN
        val query = "variable"
        val products = listOf(
            ProductTestUtils.generateProduct(
                productId = 1,
                productName = "Variable Product",
                amount = "10.0",
                productType = "variation",
                isVariable = true,
                variationIds = "[101,102,103]"
            )
        )

        whenever(mockEmptyStateProvider.getPopularItems()).thenReturn(emptyList())
        whenever(mockEmptyStateProvider.getLastSearches()).thenReturn(emptyList())
        whenever(mockDataSource.searchProducts(query)).thenReturn(
            flowOf(
                WooPosSearchProductsMockedDataSource.ProductsResult.Remote(
                    Result.success(products)
                )
            )
        )
        whenever(mockPriceFormat(BigDecimal("10.0"))).thenReturn("$10.0")
        whenever(mockParentToChildrenEventReceiver.events).thenReturn(
            flowOf(ParentToChildrenEvent.SearchEvent.ChangedQuery(query))
        )

        // WHEN
        val viewModel = createViewModel()
        advanceTimeBy(600)

        // THEN
        viewModel.viewState.test {
            skipItems(0)

            val value = awaitItem() as WooPosItemsSearchViewState.Content
            assertThat(value.items[0]).isInstanceOf(WooPosItem.Product.Variable::class.java)

            val variableProduct = value.items[0] as WooPosItem.Product.Variable
            assertThat(variableProduct.name).isEqualTo("Variable Product")
            assertThat(variableProduct.numOfVariations).isEqualTo(3)
            assertThat(variableProduct.variationIds).containsExactly(101L, 102L, 103L)
        }
    }

    private fun createViewModel() = WooPosItemsSearchViewModel(
        emptyStateProvider = mockEmptyStateProvider,
        priceFormat = mockPriceFormat,
        dataSource = mockDataSource,
        childToParentEventSender = mockChildToParentEventSender,
        parentToChildrenEventReceiver = mockParentToChildrenEventReceiver
    )
}
