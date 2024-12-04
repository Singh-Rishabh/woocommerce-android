package com.woocommerce.android.ui.woopos.home.variations

import android.annotation.SuppressLint
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.asLiveData
import app.cash.turbine.test
import com.woocommerce.android.ui.products.ProductTestUtils
import com.woocommerce.android.ui.woopos.common.data.WooPosGetProductById
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.items.PaginationState
import com.woocommerce.android.ui.woopos.home.items.WooPosItemsViewModel
import com.woocommerce.android.ui.woopos.home.items.WooPosVariationsViewState
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsDataSource
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsUIEvents
import com.woocommerce.android.ui.woopos.home.items.variations.WooPosVariationsViewModel
import com.woocommerce.android.ui.woopos.util.WooPosCoroutineTestRule
import com.woocommerce.android.ui.woopos.util.format.WooPosFormatPrice
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
class WooPosVariationsViewModelTest {

    @Rule @JvmField
    val rule = InstantTaskExecutorRule()

    @Rule
    @JvmField
    val coroutinesTestRule = WooPosCoroutineTestRule()

    private val getProductById: WooPosGetProductById = mock()
    private val childrenToParentEventSender: WooPosChildrenToParentEventSender = mock()
    private val variationsDataSource: WooPosVariationsDataSource = mock()
    private val priceFormat: WooPosFormatPrice = mock {
        onBlocking { invoke(any()) }.thenReturn("$10.0")
    }
    private lateinit var wooPosVariationsViewModel: WooPosVariationsViewModel

    @Test
    fun `given view model init, then loading state is displayed`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(emptyFlow())

        wooPosVariationsViewModel = WooPosVariationsViewModel(
            childrenToParentEventSender,
            getProductById,
            variationsDataSource,
            priceFormat
        )
        wooPosVariationsViewModel.init(1L)

        assertThat(
            wooPosVariationsViewModel.viewState.value
        ).isEqualTo(
            WooPosVariationsViewState.Loading(withCart = true)
        )
    }

    @Test
    fun `given view model init, then API call is made to fetch product`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
            flow {
                emit(emptyList())
                emit(
                    listOf(
                        ProductTestUtils.generateProductVariation(1L, 2L),
                        ProductTestUtils.generateProductVariation(1L, 3L),
                        ProductTestUtils.generateProductVariation(1L, 4L),
                    )
                )
            }
        )

        whenever(variationsDataSource.fetchVariations(any(), any())).thenReturn(
            Result.success(Unit)
        )

        wooPosVariationsViewModel = WooPosVariationsViewModel(
            childrenToParentEventSender,
            getProductById,
            variationsDataSource,
            priceFormat
        )
        wooPosVariationsViewModel.init(1L)

        verify(getProductById, times(3)).invoke(1L)
    }

    @Test
    fun `given view model init, then API call is made to fetch variation`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(emptyFlow())

        wooPosVariationsViewModel = WooPosVariationsViewModel(
            childrenToParentEventSender,
            getProductById,
            variationsDataSource,
            priceFormat
        )
        wooPosVariationsViewModel.init(1L)

        verify(variationsDataSource).fetchVariations(eq(1L), any())
    }

    @Test
    fun `given view model init, then API call is made to fetch variation with forceRefresh set to true`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(emptyFlow())

        wooPosVariationsViewModel = WooPosVariationsViewModel(
            childrenToParentEventSender,
            getProductById,
            variationsDataSource,
            priceFormat
        )
        wooPosVariationsViewModel.init(1L)

        verify(variationsDataSource).fetchVariations(1L, forceRefresh = true)
    }

    @Test
    fun `given view model init, when variation fetched successfully, then view state is updated with variation content`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                flow {
                    emit(emptyList())
                    delay(100)
                    emit(
                        listOf(
                            ProductTestUtils.generateProductVariation(1L, 2L),
                            ProductTestUtils.generateProductVariation(1L, 3L),
                            ProductTestUtils.generateProductVariation(1L, 4L),
                        )
                    )
                }
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )

            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            advanceUntilIdle()

            wooPosVariationsViewModel.viewState.test {
                // THEN
                val value = awaitItem() as WooPosVariationsViewState.Content
                assertThat(value).isInstanceOf(WooPosVariationsViewState.Content::class.java)
            }
        }

    @Test
    fun `given view model init, when variation fetched successfully, then filter out variations with price null`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                flow {
                    emit(emptyList())
                    delay(100)
                    emit(
                        listOf(
                            ProductTestUtils.generateProductVariation(1L, 2L, amount = ""),
                            ProductTestUtils.generateProductVariation(1L, 3L),
                            ProductTestUtils.generateProductVariation(1L, 4L),
                        )
                    )
                }
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )

            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            advanceUntilIdle()

            wooPosVariationsViewModel.viewState.test {
                // THEN
                val value = awaitItem() as WooPosVariationsViewState.Content
                assertThat(value.items.size).isEqualTo(2)
            }
        }

    @Test
    fun `given view model init, when variation fetched successfully, then view state is updated with proper variation content`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                flow {
                    emit(emptyList())
                    delay(100)
                    emit(
                        listOf(
                            ProductTestUtils.generateProductVariation(1L, 2L),
                            ProductTestUtils.generateProductVariation(1L, 3L),
                            ProductTestUtils.generateProductVariation(1L, 4L),
                        )
                    )
                }
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )

            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            advanceUntilIdle()

            wooPosVariationsViewModel.viewState.test {
                // THEN
                val value = awaitItem() as WooPosVariationsViewState.Content
                assertThat(value.items.size).isEqualTo(3)
                assertThat(value.items[0].id).isEqualTo(2)
                assertThat(value.items[1].id).isEqualTo(3)
                assertThat(value.items[2].id).isEqualTo(4)
                assertThat(value.paginationState).isEqualTo(PaginationState.None)
                assertFalse(value.reloadingProductsWithPullToRefresh)
            }
        }

    @Test
    fun `given variation fetch fails, when retry clicked, then fetch variations called`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                emptyFlow()
            )
            whenever(variationsDataSource.fetchVariations(any(), any())).thenReturn(
                Result.failure(Throwable())
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )
            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )

            wooPosVariationsViewModel.init(1L)
            wooPosVariationsViewModel.onUIEvent(
                WooPosVariationsUIEvents.VariationsLoadingErrorRetryButtonClicked(1L)
            )

            verify(variationsDataSource, times(2)).fetchVariations(1L)
        }

    @SuppressLint("CheckResult")
    @Test
    fun `given view state is content, when pull to refreshed, then view state contains Content state with pull to refresh set to true`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                flowOf(
                    listOf(
                        ProductTestUtils.generateProductVariation(1L, 2L),
                        ProductTestUtils.generateProductVariation(1L, 3L),
                        ProductTestUtils.generateProductVariation(1L, 4L),
                    )
                )
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )
            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            val states: MutableList<WooPosVariationsViewState> = mutableListOf()
            wooPosVariationsViewModel.viewState.asLiveData().observeForever {
                states.add(it)
            }

            wooPosVariationsViewModel.init(1L)
            wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.PullToRefreshTriggered(1L))

            assertThat(
                states.any { (it as? WooPosVariationsViewState.Content)?.reloadingProductsWithPullToRefresh == true }
            )
        }

    @Test
    fun `given view state is Loading, when pull to refreshed, then view state contains Loading state with pull to refresh set to true`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(
                emptyFlow()
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )

            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.PullToRefreshTriggered(1L))

            wooPosVariationsViewModel.viewState.test {
                // THEN
                val value = awaitItem() as WooPosVariationsViewState.Loading
                assertTrue(value.reloadingProductsWithPullToRefresh)
            }
        }

    @Test
    fun `given view state is Error, when fetch variations, then view state is updated with error state`() = runTest {
        whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
            flow {
                delay(100)
                emit(emptyList())
            }
        )
        whenever(getProductById.invoke(any())).thenReturn(
            ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
        )
        whenever(variationsDataSource.fetchVariations(any(), any())).thenReturn(
            Result.failure(Throwable())
        )

        wooPosVariationsViewModel = WooPosVariationsViewModel(
            childrenToParentEventSender,
            getProductById,
            variationsDataSource,
            priceFormat
        )
        wooPosVariationsViewModel.init(1L)
        advanceUntilIdle()

        wooPosVariationsViewModel.viewState.test {
            // THEN
            val value = awaitItem() as WooPosVariationsViewState.Error
            assertThat(value).isInstanceOf(WooPosVariationsViewState.Error::class.java)
        }
    }

    @Test
    fun `given view state is Error, when pull to refreshed, then view state is updated with proper variation content`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                flow {
                    delay(100)
                    emit(
                        listOf(
                            ProductTestUtils.generateProductVariation(1L, 2L),
                            ProductTestUtils.generateProductVariation(1L, 3L),
                            ProductTestUtils.generateProductVariation(1L, 4L),
                        )
                    )
                }
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )
            whenever(variationsDataSource.fetchVariations(any(), any())).thenReturn(
                Result.failure(Throwable())
            )
            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            val states: MutableList<WooPosVariationsViewState> = mutableListOf()
            wooPosVariationsViewModel.viewState.asLiveData().observeForever {
                states.add(it)
            }

            wooPosVariationsViewModel.init(1L)
            wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.PullToRefreshTriggered(1L))

            assertThat(
                states.any { (it as? WooPosVariationsViewState.Error)?.reloadingProductsWithPullToRefresh == true }
            )
        }

    @Test
    fun `when end of list reached, then load more called`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                flow {
                    emit(emptyList())
                    delay(100)
                    emit(
                        listOf(
                            ProductTestUtils.generateProductVariation(1L, 2L),
                            ProductTestUtils.generateProductVariation(1L, 3L),
                            ProductTestUtils.generateProductVariation(1L, 4L),
                        )
                    )
                }
            )

            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )
            whenever(variationsDataSource.fetchVariations(any(), any())).thenReturn(
                Result.success(Unit)
            )
            whenever(variationsDataSource.canLoadMore()).thenReturn(true)
            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            advanceUntilIdle()
            wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.EndOfItemsListReached(1L))

            verify(variationsDataSource).loadMore(1L)
        }

    @Test
    fun `given view state that is not Content, when load more is called, then return with doing nothing`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(
                emptyFlow()
            )
            whenever(variationsDataSource.loadMore(any())).thenReturn(
                Result.failure(Throwable())
            )

            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.EndOfItemsListReached(1L))

            verify(variationsDataSource, never()).loadMore(1L)
        }

    @Test
    fun `given no more items to load, when load more is called, then return with doing nothing`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(1L)).thenReturn(
                emptyFlow()
            )
            whenever(variationsDataSource.loadMore(any())).thenReturn(
                Result.failure(Throwable())
            )
            whenever(variationsDataSource.canLoadMore()).thenReturn(false)

            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.EndOfItemsListReached(1L))

            verify(variationsDataSource, never()).loadMore(1L)
        }

    @Test
    fun `given more items to load, when load more is success, then pagination state is updated to None `() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                flow {
                    emit(emptyList())
                    delay(100)
                    emit(
                        listOf(
                            ProductTestUtils.generateProductVariation(1L, 2L),
                            ProductTestUtils.generateProductVariation(1L, 3L),
                            ProductTestUtils.generateProductVariation(1L, 4L),
                        )
                    )
                }
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )
            whenever(variationsDataSource.loadMore(any())).thenReturn(
                Result.success(Unit)
            )
            whenever(variationsDataSource.canLoadMore()).thenReturn(true)

            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            advanceUntilIdle()
            wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.EndOfItemsListReached(1L))
            val states: MutableList<WooPosVariationsViewState> = mutableListOf()
            wooPosVariationsViewModel.viewState.asLiveData().observeForever {
                states.add(it)
            }

            assertThat((states[1] as WooPosVariationsViewState.Content).paginationState).isEqualTo(
                PaginationState.None
            )
        }

    @Test
    fun `given load more call fails, when load more is called, then pagination state is updated to Error`() =
        runTest {
            whenever(variationsDataSource.getVariationsFlow(any())).thenReturn(
                flow {
                    emit(emptyList())
                    delay(100)
                    emit(
                        listOf(
                            ProductTestUtils.generateProductVariation(1L, 2L),
                            ProductTestUtils.generateProductVariation(1L, 3L),
                            ProductTestUtils.generateProductVariation(1L, 4L),
                        )
                    )
                }
            )
            whenever(getProductById.invoke(any())).thenReturn(
                ProductTestUtils.generateProduct(1L, isVariable = true, productType = "variable")
            )
            whenever(variationsDataSource.loadMore(any())).thenReturn(
                Result.failure(Throwable())
            )
            whenever(variationsDataSource.canLoadMore()).thenReturn(true)

            wooPosVariationsViewModel = WooPosVariationsViewModel(
                childrenToParentEventSender,
                getProductById,
                variationsDataSource,
                priceFormat
            )
            wooPosVariationsViewModel.init(1L)
            advanceUntilIdle()
            wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.EndOfItemsListReached(1L))

            wooPosVariationsViewModel.viewState.test {
                val value = awaitItem() as WooPosVariationsViewState.Content
                assertThat(value.paginationState).isEqualTo(
                    PaginationState.Error
                )
            }
        }

    @Test
    fun `given OnItemClicked event, when onUIEvent is called, then onVariationClicked is triggered`() = runTest {
        // Arrange
        val productId = 1L
        val variationId = 2L
        wooPosVariationsViewModel = WooPosVariationsViewModel(
            childrenToParentEventSender,
            getProductById,
            variationsDataSource,
            priceFormat
        )

        // Act
        wooPosVariationsViewModel.onUIEvent(WooPosVariationsUIEvents.OnItemClicked(productId, variationId))

        // Assert
        verify(childrenToParentEventSender).sendToParent(
            ChildToParentEvent.ItemClickedInProductSelector(
                WooPosItemsViewModel.ItemClickedData.Variation(productId, variationId)
            )
        )
    }
}
