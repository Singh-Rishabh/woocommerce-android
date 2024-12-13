package com.woocommerce.android.ui.orders.wooshippinglabels.printing

import com.woocommerce.android.media.FileUtils
import com.woocommerce.android.util.Base64Decoder
import com.woocommerce.android.util.CoroutineDispatchers
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.withContext

class PrintShippingLabel @Inject constructor(
    private val dispatchers: CoroutineDispatchers,
    private val fileUtils: FileUtils,
    private val base64Decoder: Base64Decoder
) {
    suspend operator fun invoke(
        storageDirectory: File,
        shippingLabelData: String
    ) {
        withContext(dispatchers.io) {

        }
    }
}
