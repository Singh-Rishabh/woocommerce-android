package com.cataloghub.android.ui.orders.filters.domain

import com.cataloghub.android.model.RequestResult
import com.cataloghub.android.ui.orders.filters.data.OrderFiltersRepository
import com.cataloghub.android.ui.orders.filters.data.OrderListFilterCategory
import com.cataloghub.android.ui.orders.filters.data.OrderStatusOption
import com.cataloghub.android.ui.orders.list.OrderListRepository
import javax.inject.Inject

class GetOrderStatusFilterOptions @Inject constructor(
    private val orderListRepository: OrderListRepository,
    private val orderFiltersRepository: OrderFiltersRepository
) {
    suspend operator fun invoke(): List<OrderStatusOption> {
        var orderStatus = orderListRepository.getCachedOrderStatusOptions()
        if (orderStatus.isEmpty()) {
            when (orderListRepository.fetchOrderStatusOptionsFromApi()) {
                RequestResult.SUCCESS -> orderStatus = orderListRepository.getCachedOrderStatusOptions()
                else -> {
                    /* do nothing */
                }
            }
        }
        return orderStatus.values
            .toList()
            .map {
                OrderStatusOption(
                    key = it.statusKey,
                    label = it.label,
                    statusCount = it.statusCount,
                    isSelected = checkIfSelected(it.statusKey)
                )
            }
    }

    private fun checkIfSelected(filterKey: String): Boolean =
        orderFiltersRepository
            .getCurrentFilterSelection(OrderListFilterCategory.ORDER_STATUS)
            .contains(filterKey)
}
