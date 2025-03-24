package com.woocommerce.android.ui.woopos.home.items

import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosSearchInputState
import com.woocommerce.android.viewmodel.ResourceProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class WooPosItemsSearchHelper @Inject constructor(
    private val resourceProvider: ResourceProvider
) {
    private var coroutineScope: CoroutineScope? = null
    private var viewStateFlow: MutableStateFlow<WooPosItemsViewState>? = null
    private var searchJob: Job? = null

    fun initialize(
        coroutineScope: CoroutineScope,
        viewStateFlow: MutableStateFlow<WooPosItemsViewState>
    ) {
        this.coroutineScope = coroutineScope
        this.viewStateFlow = viewStateFlow
    }

    fun onSearchAnimationCompleted() {
        val currentState = getCurrentContentState() ?: return
        val currentSearch = currentState.search

        if (currentSearch is WooPosItemsViewState.Content.SearchState.Visible) {
            val searchState = currentSearch.state
            if (searchState is WooPosSearchInputState.Open &&
                searchState.animationState == WooPosSearchInputState.Open.AnimationState.InProgress
            ) {
                updateSearchState(
                    currentState.copy(
                        search = WooPosItemsViewState.Content.SearchState.Visible(
                            state = searchState.copy(
                                animationState = WooPosSearchInputState.Open.AnimationState.Complete
                            )
                        )
                    )
                )
            }
        }
    }

    fun onSearchChanged(newQuery: String) {
        val currentState = getCurrentContentState() ?: return
        searchJob?.cancel()

        if (newQuery.isEmpty()) {
            updateSearchState(
                currentState.copy(
                    search = WooPosItemsViewState.Content.SearchState.Visible(
                        state = WooPosSearchInputState.Open(
                            input = WooPosSearchInputState.Open.Input.Hint(
                                resourceProvider.getString(R.string.woopos_search_products)
                            ),
                            isLoading = false,
                            animationState = WooPosSearchInputState.Open.AnimationState.InProgress
                        )
                    )
                )
            )
            return
        }

        updateSearchState(
            currentState.copy(
                search = WooPosItemsViewState.Content.SearchState.Visible(
                    state = WooPosSearchInputState.Open(
                        input = WooPosSearchInputState.Open.Input.Query(newQuery),
                        isLoading = false,
                        animationState = WooPosSearchInputState.Open.AnimationState.Complete
                    )
                )
            )
        )

        val scope = coroutineScope ?: return

        @Suppress("MagicNumber")
        searchJob = scope.launch {
            try {
                delay(500)

                val updatedState = getCurrentContentState() ?: return@launch
                updateSearchState(
                    updatedState.copy(
                        search = WooPosItemsViewState.Content.SearchState.Visible(
                            state = WooPosSearchInputState.Open(
                                input = WooPosSearchInputState.Open.Input.Query(newQuery),
                                isLoading = true,
                                animationState = WooPosSearchInputState.Open.AnimationState.Complete
                            )
                        )
                    )
                )

                delay(2000)

                if (!isActive) return@launch

                val finalState = getCurrentContentState() ?: return@launch
                updateSearchState(
                    finalState.copy(
                        search = WooPosItemsViewState.Content.SearchState.Visible(
                            state = WooPosSearchInputState.Open(
                                input = WooPosSearchInputState.Open.Input.Query(newQuery),
                                isLoading = false,
                                animationState = WooPosSearchInputState.Open.AnimationState.Complete
                            )
                        )
                    )
                )
            } catch (_: CancellationException) {
                // Search was cancelled
            }
        }
    }

    fun onCloseSearchClicked() {
        val currentState = getCurrentContentState() ?: return
        updateSearchState(
            currentState.copy(
                search = WooPosItemsViewState.Content.SearchState.Visible(
                    state = WooPosSearchInputState.Closed
                )
            )
        )
    }

    fun onClearSearchClicked() {
        val currentState = getCurrentContentState() ?: return
        val currentSearch = currentState.search
        if (currentSearch is WooPosItemsViewState.Content.SearchState.Visible) {
            val currentInput = currentSearch.state
            if (currentInput is WooPosSearchInputState.Open &&
                currentInput.input is WooPosSearchInputState.Open.Input.Hint
            ) {
                return
            }
        }

        updateSearchState(
            currentState.copy(
                search = WooPosItemsViewState.Content.SearchState.Visible(
                    state = WooPosSearchInputState.Open(
                        input = WooPosSearchInputState.Open.Input.Hint(
                            resourceProvider.getString(R.string.woopos_search_products)
                        ),
                        isLoading = false,
                        animationState = WooPosSearchInputState.Open.AnimationState.Complete
                    )
                )
            )
        )
    }

    fun getInitialSearchState(isProductsSearchEnabled: Boolean): WooPosItemsViewState.Content.SearchState {
        return when (isProductsSearchEnabled) {
            true -> WooPosItemsViewState.Content.SearchState.Visible(
                state = WooPosSearchInputState.Closed
            )
            false -> WooPosItemsViewState.Content.SearchState.Hidden
        }
    }

    fun cancelSearch() {
        searchJob?.cancel()
        searchJob = null
    }

    private fun updateSearchState(newState: WooPosItemsViewState.Content) {
        viewStateFlow?.value = newState
    }

    private fun getCurrentContentState(): WooPosItemsViewState.Content? {
        return viewStateFlow?.value as? WooPosItemsViewState.Content
    }
}
