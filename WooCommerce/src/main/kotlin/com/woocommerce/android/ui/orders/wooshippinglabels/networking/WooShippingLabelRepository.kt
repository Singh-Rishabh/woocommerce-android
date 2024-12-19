package com.woocommerce.android.ui.orders.wooshippinglabels.networking

import com.woocommerce.android.model.Address
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.datasource.WooShippingRateModel
import com.woocommerce.android.ui.orders.wooshippinglabels.rates.networking.DestinationAddressDTO
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult
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

    suspend fun fetchShippingLabelStatus(
        site: SiteModel,
        orderId: Long,
        labelId: Long,
    ) = restClient.fetchShippingLabelStatus(
        site = site,
        orderId = orderId,
        labelId = labelId,
    ).asWooResult { response ->
        response.shippingLabel?.let {
            mapper(it).status
        } ?: ShippingLabelStatus.Unknown
    }

    suspend fun purchaseShippingLabel(
        site: SiteModel,
        orderId: Long,
        shippableItems: List<Long>,
        selectedPackage: PackageData,
        shipTo: Address,
        shipFrom: OriginShippingAddress,
        selectedRate: WooShippingRateModel,
        weight: Float,
        lastOrderComplete: Boolean,
    ): WooResult<Map<*, *>> {
        val origin = OriginAddressPurchaseDTO(
            id = shipFrom.id,
            address = shipFrom.address1,
            address2 = shipFrom.address2,
            city = shipFrom.city,
            state = shipFrom.state,
            postcode = shipFrom.postcode,
            country = shipFrom.country,
            name = "${shipFrom.firstName} ${shipFrom.lastName}",
            company = shipFrom.company,
            phone = shipFrom.phone
        )
        val destination = DestinationAddressDTO(
            address = shipTo.address1,
            city = shipTo.city,
            state = shipTo.state.codeOrRaw,
            postcode = shipTo.postcode,
            country = shipTo.country.code,
            name = "${shipTo.firstName} ${shipTo.lastName}"
        )
        val packageDTO = PackagePurchaseDTO(
            id = selectedPackage.id,
            boxId = "default_package",
            length = selectedPackage.length.toFloat(),
            width = selectedPackage.width.toFloat(),
            height = selectedPackage.height.toFloat(),
            weight = weight,
            isLetter = selectedPackage.isLetter,
            shipmentId = selectedRate.shipmentId,
            products = shippableItems,
            rateId = selectedRate.rateId,
            serviceId = selectedRate.serviceId,
            carrierId = selectedRate.carrierId,
            serviceName = selectedRate.serviceName
        )
        val rateDTO = RateDTO(
            rateId = selectedRate.rateId,
            serviceId = selectedRate.serviceId,
            carrierId = selectedRate.carrierId,
            title = selectedRate.serviceName,
            rate = selectedRate.price,
            deliveryDays = selectedRate.deliveryDays,
            shipmentId = selectedRate.shipmentId,
            deliveryDate = null,
            deliveryDateGuaranteed = false,
            freePickup = selectedRate.hasFreePickup,
            insurance = selectedRate.insurance,
            isSelected = true,
            tracking = selectedRate.isTrackingEnabled,
            listRate = selectedRate.price,
            retailRate = selectedRate.discount
        )
        return restClient.purchaseShippingLabel(
            site = site,
            orderId = orderId,
            origin = origin,
            destination = destination,
            selectedPackage = packageDTO,
            selectedRate = rateDTO,
            markOrderComplete = lastOrderComplete
        ).asWooResult()
    }
}
