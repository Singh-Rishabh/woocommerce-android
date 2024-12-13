package com.woocommerce.android.ui.orders.wooshippinglabels.printing

import android.content.Context
import android.os.Environment
import com.woocommerce.android.media.FileUtils
import com.woocommerce.android.util.Base64Decoder
import com.woocommerce.android.util.CoroutineDispatchers
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.withContext

class PrintShippingLabel @Inject constructor(
    private val appContext: Context,
    private val dispatchers: CoroutineDispatchers,
    private val fileUtils: FileUtils,
    private val base64Decoder: Base64Decoder
) {
    private val storageDir: File?
        get() = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

    suspend operator fun invoke(
        shippingLabelData: String
    ) {
        withContext(dispatchers.io) {
            storageDir?.let { createShippingLabelFile(it, shippingLabelData) }
        }
    }

    private fun createShippingLabelFile(
        storageDirectory: File,
        shippingLabelData: String
    ) = fileUtils.createTempTimeStampedFile(
        storageDir = storageDirectory,
        prefix = "PDF",
        fileExtension = "pdf"
    )?.let {
        fileUtils.writeContentToFile(
            file = it,
            content = base64Decoder.decode(shippingLabelData, 0)
        )
    }
}
