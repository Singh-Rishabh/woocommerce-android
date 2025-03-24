package com.woocommerce.android.ui.woopos.home.items

import com.woocommerce.android.R
import com.woocommerce.android.ui.woopos.common.composeui.component.WooPosSearchInputState
import com.woocommerce.android.ui.woopos.home.ChildToParentEvent
import com.woocommerce.android.ui.woopos.home.ParentToChildrenEvent
import com.woocommerce.android.ui.woopos.home.WooPosChildrenToParentEventSender
import com.woocommerce.android.ui.woopos.home.WooPosParentToChildrenEventReceiver
import com.woocommerce.android.viewmodel.ResourceProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class WooPosItemsSearchHelper @Inject constructor(
    private val resourceProvider: ResourceProvider,
    private val childToParentEventSender: WooPosChildrenToParentEventSender,
    private val parentToChildrenEventReceiver: WooPosParentToChildrenEventReceiver
) {
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var viewStateFlow: MutableStateFlow<WooPosItemsViewState>

    fun initialize(
        coroutineScope: CoroutineScope,
        viewStateFlow: MutableStateFlow<WooPosItemsViewState>
    ) {
        this.coroutineScope = coroutineScope
        this.viewStateFlow = viewStateFlow

        listenEventsFromParent()
    }

    private fun listenEventsFromParent() {
        coroutineScope.launch {
            parentToChildrenEventReceiver.events.collect { event ->
                when (event) {
                    ParentToChildrenEvent.SearchEvent.Started -> {
                        val currentState = getCurrentContentState() ?: return@collect
                        val searchState = currentState.search as?
                            WooPosItemsViewState.Content.SearchState.Visible ?: return@collect
                        val searchStateValue = searchState.state as? WooPosSearchInputState.Open ?: return@collect
                        updateSearchState(
                            currentState.copy(
                                search = WooPosItemsViewState.Content.SearchState.Visible(
                                    state = searchStateValue.copy(
                                        isLoading = true,
                                    )
                                )
                            )
                        )
                    }

                    ParentToChildrenEvent.SearchEvent.Finished -> {
                        val currentState = getCurrentContentState() ?: return@collect
                        val searchState = currentState.search as?
                            WooPosItemsViewState.Content.SearchState.Visible ?: return@collect
                        val searchStateValue = searchState.state as? WooPosSearchInputState.Open ?: return@collect
                        updateSearchState(
                            currentState.copy(
                                search = WooPosItemsViewState.Content.SearchState.Visible(
                                    state = searchStateValue.copy(
                                        isLoading = false,
                                    )
                                )
                            )
                        )
                    }

                    is ParentToChildrenEvent.BackFromCheckoutToCartClicked,
                    is ParentToChildrenEvent.ItemClickedInProductSelector,
                    is ParentToChildrenEvent.OrderSuccessfullyPaid,
                    is ParentToChildrenEvent.CheckoutClicked,
                    is ParentToChildrenEvent.SearchEvent.ChangedQuery -> Unit
                }
            }
        }
    }


    fun onSearchAnimationCompleted() {
        val currentState = getCurrentContentState() ?: return
        val currentSearch = currentState.search as? WooPosItemsViewState.Content.SearchState.Visible
            ?: return

        val searchState = currentSearch.state as? WooPosSearchInputState.Open ?: return
        if (searchState.animationState == WooPosSearchInputState.Open.AnimationState.InProgress) {
            updateSearchState(
                currentState.copy(
                    search = currentSearch.copy(
                        state = searchState.copy(
                            animationState = WooPosSearchInputState.Open.AnimationState.Complete
                        )
                    )
                )
            )
        }
    }

    fun onSearchChanged(newQuery: String) {
        coroutineScope.launch {
            childToParentEventSender.sendToParent(
                ChildToParentEvent.SearchEvent.ChangedQuery(query = newQuery)
            )
        }

        val currentState = getCurrentContentState() ?: return

        if (newQuery.isEmpty()) {
            updateSearchState(
                currentState.copy(
                    search = WooPosItemsViewState.Content.SearchState.Visible(
                        state = WooPosSearchInputState.Open(
                            input = WooPosSearchInputState.Open.Input.Hint(
                                resourceProvider.getString(R.string.woopos_search_products)
                            ),
                            isLoading = false,
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

    private fun updateSearchState(newState: WooPosItemsViewState.Content) {
        viewStateFlow.value = newState
    }

    private fun getCurrentContentState(): WooPosItemsViewState.Content? {
        return viewStateFlow.value as? WooPosItemsViewState.Content
    }
}
