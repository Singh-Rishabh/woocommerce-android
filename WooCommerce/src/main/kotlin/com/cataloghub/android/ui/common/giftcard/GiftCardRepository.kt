package com.cataloghub.android.ui.common.giftcard

import com.cataloghub.android.model.GiftCardSummary
import com.cataloghub.android.model.toAppModel
import com.cataloghub.android.network.giftcard.GiftCardRestClient
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.util.CoroutineDispatchers
import kotlinx.coroutines.withContext
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.BaseRequest
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooError
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooErrorType
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult
import javax.inject.Inject

class GiftCardRepository @Inject constructor(
    private val selectedSite: SelectedSite,
    private val giftCardRestClient: GiftCardRestClient,
    private val dispatchers: CoroutineDispatchers
) {
    suspend fun fetchGiftCardSummaryByOrderId(
        orderId: Long,
        site: SiteModel = selectedSite.get()
    ): WooResult<List<GiftCardSummary>> {
        return withContext(dispatchers.io) {
            val response = giftCardRestClient.fetchGiftCardSummaryByOrderId(site, orderId)
            when {
                response.isError -> {
                    WooResult(response.error)
                }
                response.result != null -> {
                    val giftCards = response.result!!.map { dto -> dto.toAppModel() }
                    WooResult(giftCards)
                }
                else -> WooResult(WooError(WooErrorType.GENERIC_ERROR, BaseRequest.GenericErrorType.UNKNOWN))
            }
        }
    }
}
