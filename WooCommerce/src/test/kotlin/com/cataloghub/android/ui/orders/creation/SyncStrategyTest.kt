package com.cataloghub.android.ui.orders.creation

import com.cataloghub.android.model.Order
import com.cataloghub.android.ui.orders.OrderTestUtils
import com.cataloghub.android.util.InlineClassesAnswer
import com.cataloghub.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.util.Date

@ExperimentalCoroutinesApi
abstract class SyncStrategyTest : BaseUnitTest() {
    protected val orderCreateEditRepository = mock<OrderCreateEditRepository> {
        onBlocking { createOrUpdateOrder(any(), any()) } doAnswer InlineClassesAnswer {
            val order = it.arguments.first() as Order
            Result.success(order.copy(total = order.total + BigDecimal.TEN))
        }
    }

    protected val order = Order.getEmptyOrder(Date(), Date()).copy(items = OrderTestUtils.generateTestOrderItems())
    protected val orderDraftChanges = MutableStateFlow(order)
    protected val retryTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    protected val createUpdateOrderUseCase = CreateUpdateOrder(
        dispatchers = coroutinesTestRule.testDispatchers,
        orderCreateEditRepository = orderCreateEditRepository
    )
}
