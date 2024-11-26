package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.PackageResponse
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.WooShippingLabelPackageRestClient
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.BaseRequest.GenericErrorType.UNKNOWN
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooError
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooErrorType.GENERIC_ERROR
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooPayload

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingLabelPackageRepositoryTest : BaseUnitTest() {

    private lateinit var repository: WooShippingLabelPackageRepository
    private val selectedSite: SelectedSite = mock(SelectedSite::class.java)
    private val packageMapper: WooShippingLabelPackageMapper = mock(WooShippingLabelPackageMapper::class.java)
    private val packageRestClient: WooShippingLabelPackageRestClient =
        mock(WooShippingLabelPackageRestClient::class.java)
    private val siteModel: SiteModel = mock(SiteModel::class.java)

    @Before
    fun setUp() {
        whenever(selectedSite.get()).thenReturn(siteModel)
        repository = WooShippingLabelPackageRepository(selectedSite, packageMapper, packageRestClient)
    }

    @Test
    fun `fetchAllStorePackages returns WooResult with result`() = testBlocking {
        val storePackages = WooShippingLabelPackageRepository.StorePackages(
            savedPackages = listOf(),
            carrierPackages = listOf()
        )
        val packageResponse = mock<PackageResponse>()
        whenever(packageRestClient.fetchShippingLabelPackages(siteModel)).thenReturn(WooPayload(packageResponse))
        whenever(packageMapper(packageResponse)).thenReturn(storePackages)

        val result = repository.fetchAllStorePackages()

        assertTrue(result.isError.not())
        assertEquals(storePackages, result.model)
    }

    @Test
    fun `fetchAllStorePackages returns WooResult with error`() = testBlocking {
        val error = WooError(
            type = GENERIC_ERROR,
            original = UNKNOWN
        )
        whenever(packageRestClient.fetchShippingLabelPackages(siteModel)).thenReturn(WooPayload(error))

        val result = repository.fetchAllStorePackages()

        assertTrue(result.isError)
        assertEquals(error, result.error)
    }
}
