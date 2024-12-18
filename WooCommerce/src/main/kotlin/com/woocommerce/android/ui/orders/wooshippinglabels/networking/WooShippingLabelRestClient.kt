package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.printing.ShippingLabelPrintingResponse
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.networking.DestinationAddressDTO
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

    suspend fun purchaseShippingLabel(
        orderId: Long,
        site: SiteModel,
        origin: OriginAddressPurchaseDTO,
        destination: DestinationAddressDTO,
        selectedPackage: PackagePurchaseDTO,
        selectedRate: RateDTO,
        hazmat: HazmatDTO = HazmatDTO(),
        markOrderComplete: Boolean
    ): WooPayload<Map<*,*>> {
        val url = "/wcshipping/v1/label/purchase/$orderId/"
        return wooNetwork.executePostGsonRequest(
            site = site,
            path = url,
            body = mapOf(
                "async" to true,
                "origin" to origin,
                "destination" to destination,
                "packages" to listOf(selectedPackage),
                "selected_rate" to mapOf(
                    selectedPackage.boxId to mapOf(
                        "rate" to selectedRate,
                        "parent" to null
                    )
                ),
                "hazmat" to mapOf(selectedPackage.boxId to hazmat),
                "customs" to emptyMap<String, String>(),
                "user_meta" to mapOf("last_order_completed" to markOrderComplete)
            ),
            clazz = Map::class.java,
        ).toWooPayload()
    }
}
