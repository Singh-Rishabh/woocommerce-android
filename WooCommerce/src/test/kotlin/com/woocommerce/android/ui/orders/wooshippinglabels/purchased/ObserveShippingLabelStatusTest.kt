import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus
import com.woocommerce.android.ui.orders.wooshippinglabels.networking.WooShippingLabelRepository
import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.ObserveShippingLabelStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import org.mockito.kotlin.any
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult

@ExperimentalCoroutinesApi
class ObserveShippingLabelStatusTest {

    private lateinit var observeShippingLabelStatus: ObserveShippingLabelStatus
    private val selectedSite: SelectedSite = mock()
    private val labelRepository: WooShippingLabelRepository = mock()

    @Before
    fun setup() {
        observeShippingLabelStatus = ObserveShippingLabelStatus(selectedSite, labelRepository)
    }

    @Test
    fun `initial state is Unknown`() = runTest {
        whenever(labelRepository.fetchShippingLabelStatus(any(), any(), any()))
            .thenReturn(WooResult(ShippingLabelStatus.Unknown))

        val result = observeShippingLabelStatus(0L, 0L).toList()

        assertEquals(listOf(ShippingLabelStatus.Unknown), result)
    }

    @Test
    fun `state transitions from PurchaseInProgress to Purchased`() = runTest {
        whenever(labelRepository.fetchShippingLabelStatus(any(), any(), any()))
            .thenReturn(WooResult(ShippingLabelStatus.PurchaseInProgress))
            .thenReturn(WooResult(ShippingLabelStatus.Purchased))

        val result = observeShippingLabelStatus(0L, 0L).toList()

        assertEquals(
            listOf(
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.PurchaseInProgress,
                ShippingLabelStatus.Purchased
            ), result
        )
    }

    @Test
    fun `state remains Unknown if status is not updated`() = runTest {
        whenever(labelRepository.fetchShippingLabelStatus(any(), any(), any()))
            .thenReturn(WooResult(ShippingLabelStatus.Unknown))

        val result = observeShippingLabelStatus(0L, 0L).toList()

        assertEquals(
            listOf(
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown
            ), result
        )
    }

    @Test
    fun `flow emits null after maximum number of attempts`() = runTest {
        whenever(labelRepository.fetchShippingLabelStatus(any(), any(), any()))
            .thenReturn(WooResult(ShippingLabelStatus.Unknown))

        val result = observeShippingLabelStatus(0L, 0L).toList()

        assertEquals(
            listOf(
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                ShippingLabelStatus.Unknown,
                null
            ), result
        )
    }
}
