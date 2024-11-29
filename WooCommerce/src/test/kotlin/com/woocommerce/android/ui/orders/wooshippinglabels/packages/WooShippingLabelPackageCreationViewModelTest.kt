package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.R
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageSelected
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ShowPackageTypeDialog
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ViewState
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.datasource.FetchPredefinedPackagesFromStore
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.Carrier
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageGroup
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CarrierPackageSelection
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.CustomPackageCreationData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.SavedPackageSelection
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.StorePredefinedPackages
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
    private val fetchPredefinedPackages: FetchPredefinedPackagesFromStore = mock()

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

        sut = WooShippingLabelPackageCreationViewModel(
            SavedStateHandle(),
            resourceProvider,
            fetchPredefinedPackages
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

        assertThat(lastEvent).isEqualTo(PackageSelected(customPackageData.toPackageData()))
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
            name = "Package 1",
            description = "Description 1",
            dimensions = "10 x 10 x 10 cm",
            isSelected = false,
            isLetter = false
        )
        val package2 = PackageData(
            name = "Package 2",
            description = "Description 2",
            dimensions = "20 x 20 x 20 cm",
            isSelected = false,
            isLetter = true
        )
        whenever(fetchPredefinedPackages()).thenReturn(
            StorePredefinedPackages(
                carrierPackageSelection = CarrierPackageSelection(emptyMap()),
                savedPackageSelection = SavedPackageSelection(
                    listOf(package1, package2)
                )
            )
        )

        sut = WooShippingLabelPackageCreationViewModel(
            SavedStateHandle(),
            resourceProvider,
            fetchPredefinedPackages
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
        val carrier: Carrier = Carrier.DHL
        val package1 = PackageData(
            name = "Package 1",
            description = "Description 1",
            dimensions = "10 x 10 x 10 cm",
            isSelected = false,
            isLetter = false
        )
        val package2 = PackageData(
            name = "Package 2",
            description = "Description 2",
            dimensions = "20 x 20 x 20 cm",
            isSelected = false,
            isLetter = true
        )
        val carrierPackages = mapOf(
            carrier to listOf(
                CarrierPackageGroup(
                    groupName = "Group 1",
                    packages = listOf(package1, package2)
                )
            )
        )
        whenever(fetchPredefinedPackages()).thenReturn(
            StorePredefinedPackages(
                carrierPackageSelection = CarrierPackageSelection(carrierPackages),
                savedPackageSelection = SavedPackageSelection(
                    emptyList()
                )
            )
        )

        sut = WooShippingLabelPackageCreationViewModel(
            SavedStateHandle(),
            resourceProvider,
            fetchPredefinedPackages
        )
        sut.viewState.observeForever { lastViewState = it }
        sut.onCarrierPackageSelected(package1, true)

        val selectedPackages = lastViewState?.carrierPackageSection?.carrierPackages?.values?.flatten()?.flatMap {
            it.packages
        }?.filter { it.isSelected }
        assertThat(selectedPackages).isNotNull
        assertThat(selectedPackages).size().isEqualTo(1)
        assertThat(selectedPackages?.first()).isEqualTo(package1.copy(isSelected = true))
    }

    @Test
    @Suppress("LongMethod")
    fun `onCarrierPackageSelected selects only one package at a time with multiple carriers`() = testBlocking {
        var lastViewState: ViewState? = null
        val carrier1: Carrier = Carrier.DHL
        val carrier2: Carrier = Carrier.USPS
        val package1 = PackageData(
            name = "Package 1 - Carrier 1",
            description = "Description 1",
            dimensions = "10 x 10 x 10 cm",
            isSelected = false,
            isLetter = false
        )
        val package2 = PackageData(
            name = "Package 2 - Carrier 1",
            description = "Description 2",
            dimensions = "20 x 20 x 20 cm",
            isSelected = false,
            isLetter = true
        )
        val package3 = PackageData(
            name = "Package 1 - Carrier 2",
            description = "Description 3",
            dimensions = "30 x 30 x 30 cm",
            isSelected = false,
            isLetter = false
        )
        val package4 = PackageData(
            name = "Package 2 - Carrier 2",
            description = "Description 4",
            dimensions = "40 x 40 x 40 cm",
            isSelected = false,
            isLetter = true
        )
        val carrierPackages = mapOf(
            carrier1 to listOf(
                CarrierPackageGroup(
                    groupName = "Group 1",
                    packages = listOf(package1, package2)
                )
            ),
            carrier2 to listOf(
                CarrierPackageGroup(
                    groupName = "Group 2",
                    packages = listOf(package3, package4)
                )
            )
        )
        whenever(fetchPredefinedPackages()).thenReturn(
            StorePredefinedPackages(
                carrierPackageSelection = CarrierPackageSelection(carrierPackages),
                savedPackageSelection = SavedPackageSelection(
                    emptyList()
                )
            )
        )

        sut = WooShippingLabelPackageCreationViewModel(
            SavedStateHandle(),
            resourceProvider,
            fetchPredefinedPackages
        )
        sut.viewState.observeForever { lastViewState = it }
        sut.onCarrierPackageSelected(package1, true)
        sut.onCarrierPackageSelected(package2, true)

        val selectedPackages = lastViewState?.carrierPackageSection?.carrierPackages?.values?.flatten()?.flatMap {
            it.packages
        }?.filter { it.isSelected }
        assertThat(selectedPackages).isNotNull
        assertThat(selectedPackages).size().isEqualTo(1)
        assertThat(selectedPackages?.first()).isEqualTo(package2.copy(isSelected = true))
    }
}
