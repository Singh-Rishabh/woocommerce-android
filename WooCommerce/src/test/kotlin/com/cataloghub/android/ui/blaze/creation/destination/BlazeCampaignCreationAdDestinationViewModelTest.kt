package com.cataloghub.android.ui.blaze.creation.destination

import com.cataloghub.android.analytics.AnalyticsTrackerWrapper
import com.cataloghub.android.model.Product
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.blaze.BlazeRepository.DestinationParameters
import com.cataloghub.android.ui.blaze.creation.destination.BlazeCampaignCreationAdDestinationViewModel.NavigateToParametersScreen
import com.cataloghub.android.ui.products.details.ProductDetailRepository
import com.cataloghub.android.util.captureValues
import com.cataloghub.android.viewmodel.BaseUnitTest
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.model.SiteModel
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BlazeCampaignCreationAdDestinationViewModelTest : BaseUnitTest() {
    private val selectedSite: SelectedSite = mock()
    private val productDetailRepository: ProductDetailRepository = mock()
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper = mock()
    private val product = mock<Product>()

    private lateinit var viewModel: BlazeCampaignCreationAdDestinationViewModel

    fun setup(url: String, productId: Long) {
        viewModel = BlazeCampaignCreationAdDestinationViewModel(
            savedStateHandle = BlazeCampaignCreationAdDestinationFragmentArgs(
                productId,
                DestinationParameters(url, emptyMap())
            ).toSavedStateHandle(),
            selectedSite = selectedSite,
            productDetailRepository = productDetailRepository,
            analyticsTrackerWrapper = analyticsTrackerWrapper
        )
    }

    @Before
    fun setupTests() {
        whenever(product.permalink).thenReturn("https://woocommerce.com")
        whenever(selectedSite.get()).thenReturn(SiteModel().apply { url = "https://woo2.com" })
        whenever(productDetailRepository.getProduct(any())).thenReturn(product)
    }

    @Test
    fun `given a product id, when back is pressed, then exit with result`() = testBlocking {
        setup("https://woocommerce.com", productId = 1L)

        viewModel.onBackPressed()

        val event = viewModel.event.value
        assertThat(event).isInstanceOf(ExitWithResult::class.java)
    }

    @Test
    fun `when url property is tapped, then url dialog becomes visible`() = testBlocking {
        setup("https://woocommerce.com", productId = 1L)

        viewModel.onUrlPropertyTapped()

        val viewState = viewModel.viewState.captureValues().last()
        assertThat(viewState.isUrlDialogVisible).isTrue()
        assertThat(viewState.targetUrl).isEqualTo(viewState.productUrl)
    }

    @Test
    fun `when parameter property is tapped, then navigate to parameters screen`() = testBlocking {
        val url = "https://woocommerce.com"
        setup(url, productId = 1L)

        viewModel.onParameterPropertyTapped()

        val event = viewModel.event.value
        assertThat(event).isEqualTo(
            NavigateToParametersScreen(
                destinationParameters = DestinationParameters(url, emptyMap())
            )
        )
    }

    @Test
    fun `when target url is updated, then view state is updated`() = testBlocking {
        val url = "https://woocommerce.com?productId=1"
        val base = "https://cnn.com"
        val params = mapOf("a" to "b", "c" to "d")
        setup(url, productId = 1L)

        viewModel.onDestinationParametersUpdated(base, params)

        val viewState = viewModel.viewState.captureValues().last()
        assertThat(viewState.targetUrl).isEqualTo(base)
        assertThat(viewState.parameters).isEqualTo(params)
    }

    @Test
    fun `when destination url is changed, then view state is updated and url dialog is not visible`() = testBlocking {
        val url = "https://woocommerce.com"
        val url2 = "https://cnn.com"
        setup(url, productId = 1L)

        viewModel.onDestinationParametersUpdated(url2)

        val viewState = viewModel.viewState.captureValues().last()
        assertThat(viewState.targetUrl).isEqualTo(url2)
        assertThat(viewState.targetUrl).isEqualTo(url2)
        assertThat(viewState.isUrlDialogVisible).isFalse()
    }
}
