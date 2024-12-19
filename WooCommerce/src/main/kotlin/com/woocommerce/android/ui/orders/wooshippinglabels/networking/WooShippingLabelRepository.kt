package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import org.wordpress.android.fluxc.model.SiteModel
import javax.inject.Inject

class WooShippingLabelRepository @Inject constructor(
    private val restClient: WooShippingLabelRestClient,
    private val mapper: WooShippingNetworkingMapper
) {
    suspend fun fetchShippingLabelPrinting(
        site: SiteModel,
        labelIds: List<Long>,
        paperSize: String
    ) = restClient.fetchShippingLabelPrinting(
        site = site,
        labelIds = labelIds,
        paperSize = paperSize
    ).asWooResult()

    suspend fun fetchAccountSettings(
        site: SiteModel,
    ) = restClient.fetchAccountSettings(
        site = site,
    ).asWooResult { mapper(it.storeOptions) }

    suspend fun fetchPurchasedShippingLabels(
        site: SiteModel,
        orderId: Long,
    ) = restClient.fetchPurchasedShippingLabels(
        site = site,
        orderId = orderId,
    ).asWooResult { it.shippingLabels?.map { label -> mapper(label) } }
}
