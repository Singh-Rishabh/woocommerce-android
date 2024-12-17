package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import javax.inject.Inject
import org.wordpress.android.fluxc.model.SiteModel

class WooShippingLabelRepository @Inject constructor(
    private val restClient: WooShippingLabelRestClient
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
}
