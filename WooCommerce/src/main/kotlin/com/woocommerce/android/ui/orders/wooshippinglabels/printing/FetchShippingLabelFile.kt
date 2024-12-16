package com.woocommerce.android.ui.orders.wooshippinglabels.printing

import android.content.Context
import android.os.Environment
import com.woocommerce.android.extensions.isNotNullOrEmpty
import com.woocommerce.android.media.FileUtils
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.util.Base64Decoder
import com.woocommerce.android.util.CoroutineDispatchers
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.withContext

class FetchShippingLabelFile @Inject constructor(
    private val appContext: Context,
    private val selectedSite: SelectedSite,
    private val dispatchers: CoroutineDispatchers,
    private val fileUtils: FileUtils,
    private val base64Decoder: Base64Decoder,
    private val printingRestClient: WooShippingLabelPrintingRestClient
) {
    private val storageDir: File?
        get() = appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)

    suspend operator fun invoke(
        labelIds: List<Long>
    ): File? {
        val site = selectedSite.getOrNull() ?: return null

        val response = printingRestClient.fetchShippingLabelPrinting(
            site = site,
            labelIds = labelIds,
            paperSize = "A4"
        ).takeIf { it.isError.not() }
            ?.result?.b64Content
            ?.takeIf { it.isNotNullOrEmpty() }
            ?: return null


        return withContext(dispatchers.io) {
            storageDir?.let {
                createShippingLabelFile(it, response)
            }
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
