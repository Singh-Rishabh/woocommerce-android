import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus.PurchaseInProgress
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus.Purchased
import com.woocommerce.android.ui.orders.wooshippinglabels.models.ShippingLabelStatus.Unknown
import com.woocommerce.android.ui.orders.wooshippinglabels.networking.WooShippingLabelRepository
import com.woocommerce.android.ui.orders.wooshippinglabels.purchased.ObserveShippingLabelStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult
import kotlin.test.assertEquals
import kotlinx.coroutines.test.advanceUntilIdle
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.wordpress.android.fluxc.model.SiteModel

@ExperimentalCoroutinesApi
class ObserveShippingLabelStatusTest {

    private lateinit var observeShippingLabelStatus: ObserveShippingLabelStatus
    private val selectedSite: SelectedSite = mock()
    private val labelRepository: WooShippingLabelRepository = mock()

    private val mockSite = SiteModel().apply { id = 123 }
    private val mockOrderId = 456L
    private val mockLabelId = 789L

    @Before
    fun setup() {
        whenever(selectedSite.get()).thenReturn(mockSite)
        observeShippingLabelStatus = ObserveShippingLabelStatus(selectedSite, labelRepository)
    }

    @Test
    fun `When response is unknown, then observation stops`() = runTest {
        whenever(labelRepository.fetchShippingLabelStatus(mockSite, mockOrderId, mockLabelId))
            .thenReturn(WooResult(Unknown))

        val result = observeShippingLabelStatus(mockOrderId, mockLabelId).toList()
        advanceUntilIdle()

        verify(labelRepository).fetchShippingLabelStatus(mockSite, mockOrderId, mockLabelId)
        assertEquals(listOf(PurchaseInProgress, Unknown), result)
    }

    @Test
    fun `When response is PurchaseInProgress, continue trying until it changes`() = runTest {
        var statusCallCount = 0
        whenever(labelRepository.fetchShippingLabelStatus(mockSite, mockOrderId, mockLabelId))
            .then {
                if (statusCallCount++ == 0) {
                    WooResult(PurchaseInProgress)
                } else {
                    WooResult(Purchased)
                }
            }

        val result = observeShippingLabelStatus(mockOrderId, mockLabelId).toList()
        advanceUntilIdle()

        verify(labelRepository, times(2)).fetchShippingLabelStatus(mockSite, mockOrderId, mockLabelId)
        assertEquals(listOf(PurchaseInProgress, PurchaseInProgress, Purchased), result)
    }
}
