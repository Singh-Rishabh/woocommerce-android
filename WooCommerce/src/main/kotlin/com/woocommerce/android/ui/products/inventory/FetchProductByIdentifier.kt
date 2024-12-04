package com.woocommerce.android.ui.products.inventory

import com.woocommerce.android.model.Product
import com.woocommerce.android.ui.orders.creation.CheckDigitRemoverFactory
import com.woocommerce.android.ui.orders.creation.GoogleBarcodeFormatMapper
import com.woocommerce.android.ui.products.list.ProductListRepository
import org.wordpress.android.fluxc.store.WCProductStore
import javax.inject.Inject

class FetchProductByIdentifier @Inject constructor(
    private val productRepository: ProductListRepository,
    private val checkDigitRemoverFactory: CheckDigitRemoverFactory,
) {
    suspend operator fun invoke(
        codeScannerResultCode: String,
        codeScannerResultFormat: GoogleBarcodeFormatMapper.BarcodeFormat
    ): Result<Product> {
        val product = searchProductBySku(
            codeScannerResultCode = codeScannerResultCode,
            codeScannerResultFormat = codeScannerResultFormat
        ) ?: searchProductByGlobalUniqueIdentifier(
            codeScannerResultCode = codeScannerResultCode,
            codeScannerResultFormat = codeScannerResultFormat
        )

        return if (product != null) {
            Result.success(product)
        } else {
            Result.failure(Exception("Product not found"))
        }
    }

    private suspend fun searchProductBySku(
        codeScannerResultCode: String,
        codeScannerResultFormat: GoogleBarcodeFormatMapper.BarcodeFormat
    ): Product? {
        return productRepository.searchProductList(
            searchQuery = codeScannerResultCode,
            skuSearchOptions = WCProductStore.SkuSearchOptions.ExactSearch
        )?.firstOrNull()
            ?: removeCheckDigitIfPossible(
                codeScannerResultCode = codeScannerResultCode,
                codeScannerResultFormat = codeScannerResultFormat
            )?.let {
                productRepository.searchProductList(
                    searchQuery = it,
                    skuSearchOptions = WCProductStore.SkuSearchOptions.ExactSearch
                )?.firstOrNull()
            }
    }

    private suspend fun searchProductByGlobalUniqueIdentifier(
        codeScannerResultCode: String,
        codeScannerResultFormat: GoogleBarcodeFormatMapper.BarcodeFormat
    ): Product? {
        val product = productRepository.searchProductListByGlobalUniqueId(
            globalUniqueId = codeScannerResultCode
        )?.firstOrNull() ?: removeCheckDigitIfPossible(
            codeScannerResultCode = codeScannerResultCode,
            codeScannerResultFormat = codeScannerResultFormat
        )?.let {
            productRepository.searchProductListByGlobalUniqueId(
                globalUniqueId = codeScannerResultCode
            )?.firstOrNull()
        }

        return product
    }

    private fun removeCheckDigitIfPossible(
        codeScannerResultCode: String,
        codeScannerResultFormat: GoogleBarcodeFormatMapper.BarcodeFormat
    ): String? {
        if (codeScannerResultFormat.isEAN() || codeScannerResultFormat.isUPC()) {
            return checkDigitRemoverFactory.getCheckDigitRemoverFor(codeScannerResultFormat)
                .getSKUWithoutCheckDigit(codeScannerResultCode)
        }

        return null
    }

    private fun GoogleBarcodeFormatMapper.BarcodeFormat.isUPC() =
        this == GoogleBarcodeFormatMapper.BarcodeFormat.FormatUPCA ||
            this == GoogleBarcodeFormatMapper.BarcodeFormat.FormatUPCE

    private fun GoogleBarcodeFormatMapper.BarcodeFormat.isEAN() =
        this == GoogleBarcodeFormatMapper.BarcodeFormat.FormatEAN13 ||
            this == GoogleBarcodeFormatMapper.BarcodeFormat.FormatEAN8
}
