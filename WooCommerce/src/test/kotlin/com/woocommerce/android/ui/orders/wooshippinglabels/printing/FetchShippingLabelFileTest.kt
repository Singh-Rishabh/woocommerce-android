package com.woocommerce.android.ui.orders.wooshippinglabels.printing

import android.content.Context
import android.os.Environment
import com.woocommerce.android.media.FileUtils
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.networking.WooShippingLabelRepository
import com.woocommerce.android.util.Base64Decoder
import com.woocommerce.android.util.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import org.wordpress.android.fluxc.model.SiteModel
import java.io.File
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult

@ExperimentalCoroutinesApi
class FetchShippingLabelFileTest {

    private lateinit var fetchShippingLabelFile: FetchShippingLabelFile
    private val appContext: Context = mock()
    private val selectedSite: SelectedSite = mock()
    private val dispatchers: CoroutineDispatchers = mock()
    private val fileUtils: FileUtils = mock()
    private val base64Decoder: Base64Decoder = mock()
    private val labelRepository: WooShippingLabelRepository = mock()
    private val storageDir: File = mock()
    private val siteModel: SiteModel = mock()
    private val file: File = mock()

    @Before
    fun setup() {
        whenever(appContext.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)).thenReturn(storageDir)
        whenever(selectedSite.getOrNull()).thenReturn(siteModel)
        whenever(dispatchers.io).thenReturn(kotlinx.coroutines.Dispatchers.Unconfined)
        fetchShippingLabelFile = FetchShippingLabelFile(appContext, selectedSite, dispatchers, fileUtils, base64Decoder, labelRepository)
    }

    @Test
    fun `invoke calls labelRepository and creates file`() = runBlockingTest {
        val labelIds = listOf(1L, 2L, 3L)
        val paperSize = "A4"
        val b64Content = "base64EncodedContent"
        val response = mock<WooResult<ShippingLabelPrintingResponse>> {
            on { isError } doReturn false
            on { model?.b64Content } doReturn b64Content
        }

        whenever(labelRepository.fetchShippingLabelPrinting(siteModel, labelIds, paperSize)).thenReturn(response)
        whenever(fileUtils.createTempTimeStampedFile(storageDir, FetchShippingLabelFile.PDF_PREFIX, FetchShippingLabelFile.PDF_EXTENSION)).thenReturn(file)
        whenever(base64Decoder.decode(b64Content, 0)).thenReturn(ByteArray(0))

        fetchShippingLabelFile(labelIds, paperSize)

        verify(labelRepository).fetchShippingLabelPrinting(siteModel, labelIds, paperSize)
        verify(fileUtils).createTempTimeStampedFile(storageDir, FetchShippingLabelFile.PDF_PREFIX, FetchShippingLabelFile.PDF_EXTENSION)
        verify(base64Decoder).decode(b64Content, 0)
        verify(fileUtils).writeContentToFile(file, ByteArray(0))
    }

    @Test
    fun `invoke returns null when site is null`() = runBlockingTest {
        whenever(selectedSite.getOrNull()).thenReturn(null)

        val result = fetchShippingLabelFile(listOf(1L, 2L, 3L))

        assert(result == null)
        verify(labelRepository, never()).fetchShippingLabelPrinting(any(), any(), any())
    }

    @Test
    fun `invoke returns null when response is error`() = runBlockingTest {
        val labelIds = listOf(1L, 2L, 3L)
        val paperSize = "A4"
        val response = mock<WooResult<ShippingLabelPrintingResponse>> {
            on { isError } doReturn true
        }

        whenever(labelRepository.fetchShippingLabelPrinting(siteModel, labelIds, paperSize)).thenReturn(response)

        val result = fetchShippingLabelFile(labelIds, paperSize)

        assert(result == null)
        verify(labelRepository).fetchShippingLabelPrinting(siteModel, labelIds, paperSize)
        verify(fileUtils, never()).createTempTimeStampedFile(any(), any(), any())
    }

    @Test
    fun `invoke returns null when b64Content is empty`() = runBlockingTest {
        val labelIds = listOf(1L, 2L, 3L)
        val paperSize = "A4"
        val response = mock<WooResult<ShippingLabelPrintingResponse>> {
            on { isError } doReturn false
            on { model?.b64Content } doReturn ""
        }

        whenever(labelRepository.fetchShippingLabelPrinting(siteModel, labelIds, paperSize)).thenReturn(response)

        val result = fetchShippingLabelFile(labelIds, paperSize)

        assert(result == null)
        verify(labelRepository).fetchShippingLabelPrinting(siteModel, labelIds, paperSize)
        verify(fileUtils, never()).createTempTimeStampedFile(any(), any(), any())
    }
}
