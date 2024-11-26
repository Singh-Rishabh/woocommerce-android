package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.PackageResponse

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.networking.WooShippingLabelPackageRestClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooPayload

class WooShippingLabelPackageRepositoryTest {

    private lateinit var repository: WooShippingLabelPackageRepository
    private val selectedSite: SelectedSite = mock(SelectedSite::class.java)
    private val packageMapper: WooShippingLabelPackageMapper = mock(WooShippingLabelPackageMapper::class.java)
    private val packageRestClient: WooShippingLabelPackageRestClient = mock(WooShippingLabelPackageRestClient::class.java)
    private val siteModel: SiteModel = mock(SiteModel::class.java)

    @Before
    fun setUp() {
        `when`(selectedSite.get()).thenReturn(siteModel)
        repository = WooShippingLabelPackageRepository(selectedSite, packageMapper, packageRestClient)
    }

    @Test
    fun `fetchAllStorePackages returns WooResult with result`() = runBlocking {
        val storePackages = WooShippingLabelPackageRepository.StorePackages(
            savedPackages = listOf(),
            carrierPackages = listOf()
        )
        val packageResponse = mock<PackageResponse>()
        `when`(packageRestClient.fetchShippingLabelPackages(siteModel)).thenReturn(WooPayload(packageResponse))
        `when`(packageMapper(packageResponse)).thenReturn(storePackages)

        val result = repository.fetchAllStorePackages()

        assertTrue(result.isError.not())
        assertEquals(storePackages, result.model)
    }

    @Test
    fun `fetchAllStorePackages returns WooResult with error`() = runBlocking {
        val error = Exception("Error fetching packages")
        `when`(packageRestClient.fetchShippingLabelPackages(siteModel)).thenThrow(error)

        val result = repository.fetchAllStorePackages()

        assertTrue(result.isError)
        assertEquals(error, result.error)
    }
}
