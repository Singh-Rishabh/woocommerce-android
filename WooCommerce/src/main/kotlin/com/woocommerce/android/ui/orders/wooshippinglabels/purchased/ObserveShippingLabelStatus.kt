package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus.PurchaseInProgress
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus.Unknown
import com.woocommerce.android.ui.orders.wooshippinglabels.networking.WooShippingLabelRepository
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ObserveShippingLabelStatus @Inject constructor(
    private val selectedSite: SelectedSite,
    private val labelRepository: WooShippingLabelRepository
) {
    suspend operator fun invoke(
        orderId: Long,
        labelId: Long
    ): Flow<ShippingLabelStatus> {
        return flow {

        }
    }
}
