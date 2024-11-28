package com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource

import com.woocommerce.android.tools.SelectedSite
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.viewmodel.BaseUnitTest
import org.mockito.kotlin.whenever
import org.wordpress.android.fluxc.network.BaseRequest
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooError
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooErrorType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.network.rest.wpcom.wc.WooResult

@OptIn(ExperimentalCoroutinesApi::class)
class FetchPredefinedPackagesFromStoreTest : BaseUnitTest() {

    private val packageRepository: WooShippingLabelPackageRepository = mock()
    private val selectedSite: SelectedSite = mock()
    private val fetchPredefinedPackagesFromStore = FetchPredefinedPackagesFromStore(
        selectedSite,
        packageRepository
    )

    @Test
    fun `invoke should return StorePredefinedPackages with carrier and saved packages`() = testBlocking {
        val storePackages = StorePackagesDAO(
            savedPackages = listOf(
                PackageDAO(
                    id = "1",
                    name = "Saved Package 1",
                    dimensions = "dimensions"
                ),
                PackageDAO(
                    id = "2",
                    name = "Saved Package 2",
                    dimensions = "dimensions"
                )
            ),
            carrierPackages = mapOf(
                CarrierType.USPS to CarrierDAO(
                    packageGroup = listOf(
                        CarrierPackageGroupDAO(
                            description = "Group 1",
                            packages = listOf(
                                PackageDAO(
                                    id = "1",
                                    name = "Carrier Package 1",
                                    dimensions = "dimensions"
                                )
                            )
                        )
                    )
                )
            )
        )
        val site = SiteModel().apply { id = 1 }
        whenever(selectedSite.getOrNull()).thenReturn(site)
        whenever(packageRepository.fetchAllStorePackages(site)).thenReturn(WooResult(storePackages))

        val result = fetchPredefinedPackagesFromStore()!!

        assertThat(result.savedPackageSelection.packages).containsExactly(
            PackageData(
                type = PackageType.BOX,
                name = "Saved Package 1",
                description = "",
                length = "",
                width = "",
                height = "",
                isSelected = false
            ),
            PackageData(
                type = PackageType.BOX,
                name = "Saved Package 2",
                description = "",
                length = "",
                width = "",
                height = "",
                isSelected = false
            )
        )
        assertThat(result.carrierPackageSelection.carrierPackages[Carrier.USPS]).containsExactly(
            CarrierPackageGroup(
                groupName = "Group 1",
                packages = listOf(
                    PackageData(
                        type = PackageType.BOX,
                        name = "Carrier Package 1",
                        description = "",
                        length = "",
                        width = "",
                        height = "",
                        isSelected = false
                    )
                )
            )
        )
    }

    @Test
    fun `invoke should return empty StorePredefinedPackages when fetchAllStorePackages returns error`() = testBlocking {
        val error = WooError(WooErrorType.GENERIC_ERROR, BaseRequest.GenericErrorType.UNKNOWN)
        val site = SiteModel().apply { id = 1 }
        whenever(selectedSite.getOrNull()).thenReturn(site)
        whenever(packageRepository.fetchAllStorePackages(site)).thenReturn(WooResult(error))

        val result = fetchPredefinedPackagesFromStore()!!

        assertThat(result.savedPackageSelection.packages).isEmpty()
        assertThat(result.carrierPackageSelection.carrierPackages).isEmpty()
    }
}
