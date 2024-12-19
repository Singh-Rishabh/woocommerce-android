package com.woocommerce.android.ui.orders.wooshippinglabels.purchased

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.OpenLearnMoreScreen
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.OpenShippingLabelFile
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.OpenUrl
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.StartRefundRequest
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.ViewState
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.printing.FetchShippingLabelFile
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.navArgs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File

@ExperimentalCoroutinesApi
class WooShippingLabelPurchasedViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: WooShippingLabelPurchasedViewModel
    private val fetchShippingLabelFile: FetchShippingLabelFile = mock()
    private val savedStateHandle: SavedStateHandle = mock()
    private val navArgs: WooShippingLabelPurchasedFragmentArgs = mock()
    private val file: File = mock()

    @Before
    fun setup() {
        whenever(savedStateHandle.navArgs<WooShippingLabelPurchasedFragmentArgs>()).thenReturn(navArgs)
        viewModel = WooShippingLabelPurchasedViewModel(savedStateHandle, fetchShippingLabelFile)
    }

    @Test
    fun `onPrintShippingLabelClicked triggers OpenShippingLabelFile event`() = runBlockingTest {
        var latestEvent: MultiLiveEvent.Event? = null
        viewModel.event.observeForever { latestEvent = it }
        whenever(fetchShippingLabelFile(listOf(navArgs.purchaseData.labelId), "label")).thenReturn(file)

        viewModel.onPrintShippingLabelClicked()

        verify(fetchShippingLabelFile).invoke(listOf(navArgs.purchaseData.labelId), "label")
        assertThat(latestEvent).isEqualTo(OpenShippingLabelFile(file))
    }

    @Test
    fun `onLabelPaperSizeOptionSelected updates viewState`() = runBlockingTest {
    }

    @Test
    fun `onTrackShipmentClicked triggers OpenUrl event`() = runBlockingTest {
        var latestEvent: MultiLiveEvent.Event? = null
        viewModel.event.observeForever { latestEvent = it }
        whenever(navArgs.purchaseData.trackingNumber).thenReturn("123456")
        whenever(navArgs.purchaseData.carrierId).thenReturn("usps")

        viewModel.onTrackShipmentClicked()

        assertThat(latestEvent).isEqualTo(OpenUrl("https://tools.usps.com/go/TrackConfirmAction_input?qtc_tLabels1=123456"))
    }

    @Test
    fun `onSchedulePickUpClicked triggers OpenUrl event`() = runBlockingTest {
        var latestEvent: MultiLiveEvent.Event? = null
        viewModel.event.observeForever { latestEvent = it }
        whenever(navArgs.purchaseData.carrierId).thenReturn("usps")

        viewModel.onSchedulePickUpClicked()

        assertThat(latestEvent).isEqualTo(OpenUrl("https://tools.usps.com/schedule-pickup-steps.htm"))
    }

    @Test
    fun `onRefundClicked triggers StartRefundRequest event`() = runBlockingTest {
        var latestEvent: MultiLiveEvent.Event? = null
        viewModel.event.observeForever { latestEvent = it }

        viewModel.onRefundClicked()

        assertThat(latestEvent).isEqualTo(StartRefundRequest)
    }

    @Test
    fun `onLearnMoreClicked triggers OpenLearnMoreScreen event`() = runBlockingTest {
        var latestEvent: MultiLiveEvent.Event? = null
        viewModel.event.observeForever { latestEvent = it }

        viewModel.onLearnMoreClicked()

        assertThat(latestEvent).isEqualTo(OpenLearnMoreScreen)
    }
}
