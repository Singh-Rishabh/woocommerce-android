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
    ): Flow<ShippingLabelStatus?> {
        return flow {
            var attempts = 0
            var latestStatus = Unknown
            emit(latestStatus)

            do {
                if (attempts >= CHECK_ATTEMPTS_BEFORE_GIVING_UP) {
                    emit(null)
                    break
                }

                latestStatus = labelRepository.fetchShippingLabelStatus(
                    site = selectedSite.get(),
                    orderId = orderId,
                    labelId = labelId
                ).takeIf { it.isError.not() }?.model ?: Unknown
                emit(latestStatus)
                attempts++
                delay(DELAY_BETWEEN_STATUS_CHECKS)
            } while (latestStatus == PurchaseInProgress || latestStatus == Unknown)
        }
    }

    companion object {
        private const val DELAY_BETWEEN_STATUS_CHECKS = 2000L
        private const val CHECK_ATTEMPTS_BEFORE_GIVING_UP = 10
    }
}
