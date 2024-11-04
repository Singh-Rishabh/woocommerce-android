package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.R
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.MultiLiveEvent
import com.woocommerce.android.viewmodel.ResourceProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingLabelPackageCreationViewModelTest : BaseUnitTest() {

    private lateinit var sut: WooShippingLabelPackageCreationViewModel
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
    private val resourceProvider: ResourceProvider = mock()

    @Before
    fun setUp() {
        whenever(resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_custom)).thenReturn("Custom")
        whenever(resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_carrier)).thenReturn("Carrier")
        whenever(resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_saved)).thenReturn("Saved")
        sut = WooShippingLabelPackageCreationViewModel(savedStateHandle, resourceProvider)
    }

    @Test
    fun `onAddPackageClick triggers PackageSelected event`() = testBlocking {
        var lastEvent: MultiLiveEvent.Event? = null
        sut.event.observeForever { lastEvent = it }

        val customPackageData = WooShippingLabelPackageCreationViewModel.CustomPackageCreationData(
            type = WooShippingLabelPackageCreationViewModel.PackageType.BOX,
            length = "10",
            width = "10",
            height = "10",
            saveAsTemplate = false
        )
        val viewState = WooShippingLabelPackageCreationViewModel.ViewState(
            pageTabs = emptyList(),
            customPackageCreationData = customPackageData
        )
        whenever(savedStateHandle.get<WooShippingLabelPackageCreationViewModel.ViewState>(any())).thenReturn(viewState)

        sut.onAddPackageClick()

        assertThat(lastEvent).isEqualTo(WooShippingLabelPackageCreationViewModel.PackageSelected(customPackageData))
    }

    @Test
    fun `onPackageTypeSpinnerClick triggers ShowPackageTypeDialog event`() = testBlocking {
        var lastEvent: MultiLiveEvent.Event? = null
        sut.event.observeForever { lastEvent = it }

        val customPackageData = WooShippingLabelPackageCreationViewModel.CustomPackageCreationData(
            type = WooShippingLabelPackageCreationViewModel.PackageType.BOX,
            length = "10",
            width = "10",
            height = "10",
            saveAsTemplate = false
        )
        val viewState = WooShippingLabelPackageCreationViewModel.ViewState(
            pageTabs = emptyList(),
            customPackageCreationData = customPackageData
        )
        whenever(savedStateHandle.get<WooShippingLabelPackageCreationViewModel.ViewState>(any())).thenReturn(viewState)

        sut.onPackageTypeSpinnerClick()

        assertThat(lastEvent).isEqualTo(WooShippingLabelPackageCreationViewModel.ShowPackageTypeDialog(customPackageData.type))
    }

    @Test
    fun `onPackageTypeSelected updates viewState with new type`() = testBlocking {
        val newType = WooShippingLabelPackageCreationViewModel.PackageType.ENVELOPE
        sut.onPackageTypeSelected(newType)

        val updatedState = sut.viewState.value
        assert(updatedState?.customPackageCreationData?.type == newType)
    }

    @Test
    fun `onLengthChange updates viewState with new length`() = testBlocking {
        val newLength = "20"
        sut.onLengthChange(newLength)

        val updatedState = sut.viewState.value
        assertThat(updatedState?.customPackageCreationData?.length).isEqualTo(newLength)
    }

    @Test
    fun `onWidthChange updates viewState with new width`() = testBlocking {
        val newWidth = "20"
        sut.onWidthChange(newWidth)

        val updatedState = sut.viewState.value
        assertThat(updatedState?.customPackageCreationData?.width).isEqualTo(newWidth)
    }

    @Test
    fun `onHeightChange updates viewState with new height`() = testBlocking {
        val newHeight = "20"
        sut.onHeightChange(newHeight)

        val updatedState = sut.viewState.value
        assertThat(updatedState?.customPackageCreationData?.height).isEqualTo(newHeight)
    }

    @Test
    fun `onSavePackageChanged updates viewState with new saveAsTemplate value`() = testBlocking {
        val newSaveAsTemplate = true
        sut.onSavePackageChanged(newSaveAsTemplate)

        val updatedState = sut.viewState.value
        assertThat(updatedState?.customPackageCreationData?.saveAsTemplate).isEqualTo(newSaveAsTemplate)
    }
}
