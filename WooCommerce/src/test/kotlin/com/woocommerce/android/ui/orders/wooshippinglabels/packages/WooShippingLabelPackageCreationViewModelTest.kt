package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.R
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.ResourceProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingLabelPackageCreationViewModelTest : BaseUnitTest() {

    private lateinit var sut: WooShippingLabelPackageCreationViewModel
    private val savedStateHandle: SavedStateHandle = mock()
    private val resourceProvider: ResourceProvider = mock()

    @Before
    fun setUp() {
        whenever(resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_custom)).thenReturn("Custom")
        whenever(resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_carrier)).thenReturn("Carrier")
        whenever(resourceProvider.getString(R.string.woo_shipping_labels_package_creation_tab_saved)).thenReturn("Saved")
        sut = WooShippingLabelPackageCreationViewModel(savedStateHandle, resourceProvider)
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
        assert(updatedState?.customPackageCreationData?.length == newLength)
    }

    @Test
    fun `onWidthChange updates viewState with new width`() = testBlocking {
        val newWidth = "20"
        sut.onWidthChange(newWidth)

        val updatedState = sut.viewState.value
        assert(updatedState?.customPackageCreationData?.width == newWidth)
    }

    @Test
    fun `onHeightChange updates viewState with new height`() = testBlocking {
        val newHeight = "20"
        sut.onHeightChange(newHeight)

        val updatedState = sut.viewState.value
        assert(updatedState?.customPackageCreationData?.height == newHeight)
    }

    @Test
    fun `onSavePackageChanged updates viewState with new saveAsTemplate value`() = testBlocking {
        val newSaveAsTemplate = true
        sut.onSavePackageChanged(newSaveAsTemplate)

        val updatedState = sut.viewState.value
        assert(updatedState?.customPackageCreationData?.saveAsTemplate == newSaveAsTemplate)
    }
}
