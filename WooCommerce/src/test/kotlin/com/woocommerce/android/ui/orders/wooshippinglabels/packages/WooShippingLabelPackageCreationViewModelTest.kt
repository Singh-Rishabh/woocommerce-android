package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.R
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.CustomPackageCreated
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ShowPackageTypeDialog
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ViewState
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.FetchCarrierPackagesFromStore
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.FetchSavedPackagesFromStore
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ResourceProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingLabelPackageCreationViewModelTest : BaseUnitTest() {

    private lateinit var sut: WooShippingLabelPackageCreationViewModel
    private val resourceProvider: ResourceProvider = mock()
    private val fetchSavedPackages: FetchSavedPackagesFromStore = mock()
    private val fetchCarrierPackages: FetchCarrierPackagesFromStore = mock()

    @Before
    fun setUp() {
        whenever(
            resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_custom)
        ).thenReturn("Custom")
        whenever(
            resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_carrier)
        ).thenReturn("Carrier")
        whenever(
            resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_saved)
        ).thenReturn("Saved")

        whenever(fetchSavedPackages()).thenReturn(emptyList())
        sut = WooShippingLabelPackageCreationViewModel(
            SavedStateHandle(),
            resourceProvider,
            fetchSavedPackages,
            fetchCarrierPackages
        )
    }

    @Test
    fun `onAddPackageClick triggers PackageSelected event`() = testBlocking {
        var lastEvent: MultiLiveEvent.Event? = null
        sut.event.observeForever { lastEvent = it }

        val customPackageData = CustomPackageCreationData(
            type = PackageType.ENVELOPE,
            length = "10",
            width = "10",
            height = "10",
            saveAsTemplate = true
        )

        sut.onLengthChange("10")
        sut.onWidthChange("10")
        sut.onHeightChange("10")
        sut.onSavePackageChanged(true)
        sut.onPackageTypeSelected(PackageType.ENVELOPE)

        sut.onAddCustomPackageClick()

        assertThat(lastEvent).isEqualTo(CustomPackageCreated(customPackageData))
    }

    @Test
    fun `onPackageTypeSpinnerClick triggers ShowPackageTypeDialog event`() = testBlocking {
        var lastEvent: MultiLiveEvent.Event? = null
        sut.event.observeForever { lastEvent = it }
        sut.onPackageTypeSelected(PackageType.ENVELOPE)

        sut.onPackageTypeSpinnerClick()

        assertThat(
            lastEvent
        ).isEqualTo(ShowPackageTypeDialog(PackageType.ENVELOPE))
    }

    @Test
    fun `onPackageTypeSelected updates viewState with new type`() = testBlocking {
        var lastViewState: ViewState? = null
        sut.viewState.observeForever { lastViewState = it }
        val newType = PackageType.ENVELOPE
        sut.onPackageTypeSelected(newType)

        assertThat(lastViewState?.customPackageCreationData?.type).isEqualTo(newType)
    }

    @Test
    fun `onLengthChange updates viewState with new length`() = testBlocking {
        var lastViewState: ViewState? = null
        sut.viewState.observeForever { lastViewState = it }
        val newLength = "20"
        sut.onLengthChange(newLength)

        assertThat(lastViewState?.customPackageCreationData?.length).isEqualTo(newLength)
    }

    @Test
    fun `onWidthChange updates viewState with new width`() = testBlocking {
        var lastViewState: ViewState? = null
        sut.viewState.observeForever { lastViewState = it }
        val newWidth = "20"
        sut.onWidthChange(newWidth)

        assertThat(lastViewState?.customPackageCreationData?.width).isEqualTo(newWidth)
    }

    @Test
    fun `onHeightChange updates viewState with new height`() = testBlocking {
        var lastViewState: ViewState? = null
        sut.viewState.observeForever { lastViewState = it }
        val newHeight = "20"
        sut.onHeightChange(newHeight)

        assertThat(lastViewState?.customPackageCreationData?.height).isEqualTo(newHeight)
    }

    @Test
    fun `onSavePackageChanged updates viewState with new saveAsTemplate value`() = testBlocking {
        var lastViewState: ViewState? = null
        sut.viewState.observeForever { lastViewState = it }
        val newSaveAsTemplate = true
        sut.onSavePackageChanged(newSaveAsTemplate)

        assertThat(lastViewState?.customPackageCreationData?.saveAsTemplate).isEqualTo(newSaveAsTemplate)
    }

    @Test
    fun `onSavedPackageSelected selects only one package at a time`() = testBlocking {
        var lastViewState: ViewState? = null
        val package1 = PackageData(
            type = PackageType.BOX,
            name = "Package 1",
            description = "Description 1",
            length = "10",
            width = "10",
            height = "10",
            isSelected = false
        )
        val package2 = PackageData(
            type = PackageType.ENVELOPE,
            name = "Package 2",
            description = "Description 2",
            length = "20",
            width = "20",
            height = "20",
            isSelected = false
        )
        whenever(fetchSavedPackages()).thenReturn(listOf(package1, package2))

        sut = WooShippingLabelPackageCreationViewModel(
            SavedStateHandle(),
            resourceProvider,
            fetchSavedPackages,
            fetchCarrierPackages
        )
        sut.viewState.observeForever { lastViewState = it }
        sut.onSavedPackageSelected(package1, true)

        val selectedPackages = lastViewState?.savedPackageSelection?.packages?.filter { it.isSelected }
        assertThat(selectedPackages).isNotNull
        assertThat(selectedPackages).size().isEqualTo(1)
        assertThat(selectedPackages?.first()).isEqualTo(package1.copy(isSelected = true))
    }

    @Test
    fun `onCarrierPackageSelected selects only one package at a time`() = testBlocking {
        var lastViewState: ViewState? = null
        val carrier = Carrier(id = "dhl", name = "DHL Express", logoRes = R.drawable.dhl_logo)
        val package1 = PackageData(
            type = PackageType.BOX,
            name = "Package 1",
            description = "Description 1",
            length = "10",
            width = "10",
            height = "10",
            isSelected = false
        )
        val package2 = PackageData(
            type = PackageType.ENVELOPE,
            name = "Package 2",
            description = "Description 2",
            length = "20",
            width = "20",
            height = "20",
            isSelected = false
        )
        val carrierPackages = mapOf(
            carrier to listOf(
                CarrierPackageGroup(
                    groupName = "Group 1",
                    packages = listOf(package1, package2)
                )
            )
        )
        whenever(fetchCarrierPackages()).thenReturn(carrierPackages)

        sut = WooShippingLabelPackageCreationViewModel(SavedStateHandle(), resourceProvider, fetchSavedPackages, fetchCarrierPackages)
        sut.viewState.observeForever { lastViewState = it }
        sut.onCarrierPackageSelected(package1, true)

        val selectedPackages = lastViewState?.carrierPackageSection?.carrierPackages?.values?.flatten()?.flatMap { it.packages }?.filter { it.isSelected }
        assertThat(selectedPackages).isNotNull
        assertThat(selectedPackages).size().isEqualTo(1)
        assertThat(selectedPackages?.first()).isEqualTo(package1.copy(isSelected = true))
    }
}
