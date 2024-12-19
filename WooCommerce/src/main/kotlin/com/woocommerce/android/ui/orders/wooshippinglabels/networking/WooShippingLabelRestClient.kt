package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.printing.ShippingLabelPrintingResponse
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooNetwork
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooPayload
import org.wordpress.android.fluxc.utils.toWooPayload
import javax.inject.Inject

class WooShippingLabelRestClient @Inject constructor(
    private val wooNetwork: WooNetwork
) {
    suspend fun fetchShippingLabelPrinting(
        site: SiteModel,
        labelIds: List<Long>,
        paperSize: String
    ): WooPayload<ShippingLabelPrintingResponse> {
        val url = "/wcshipping/v1/label/print"

        return wooNetwork.executeGetGsonRequest(
            site = site,
            path = url,
            params = mapOf(
                "label_id_csv" to labelIds.joinToString { "$it," },
                "paper_size" to paperSize
            ),
            clazz = ShippingLabelPrintingResponse::class.java,
        ).toWooPayload()
    }

    suspend fun fetchAccountSettings(
        site: SiteModel,
    ): WooPayload<AccountSettingsDTO> {
        val url = "/wcshipping/v1/account/settings/"

        val result = wooNetwork.executeGetGsonRequest(
            site = site,
            path = url,
            clazz = AccountSettingsDTO::class.java,
        )

        return result.toWooPayload()
    }
}
