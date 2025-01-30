package com.woocommerce.android.ui.orders.wooshippinglabels.address.origin

import androidx.compose.runtime.snapshots.Snapshot
import com.woocommerce.android.ui.orders.wooshippinglabels.address.AddressValidationHelper
import com.woocommerce.android.ui.orders.wooshippinglabels.models.OriginShippingAddress
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingEditOriginViewModelTest : BaseUnitTest() {
    private val addressValidator: AddressValidationHelper = mock()

    private lateinit var sut: WooShippingEditOriginViewModel

    fun createViewModel(originAddress: OriginShippingAddress) {
        sut = WooShippingEditOriginViewModel(
            addressValidator = addressValidator,
            savedState = WooShippingEditOriginAddressFragmentArgs(originAddress).toSavedStateHandle()
        )
    }

    @Test
    fun `when name is not empty and company is empty, then name error is null`() = testBlocking {
        val name = "name"
        val company = ""
        val address = OriginShippingAddress.EMPTY.copy(
            firstName = name,
            company = company
        )
        whenever(addressValidator.validateFieldRequired(any())).doReturn(null)
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.name.error).isNull()
        assertThat(editableAddress.company.error).isNull()
    }

    @Test
    fun `when name is empty and company Not is empty, then name error is null`() = testBlocking {
        val name = ""
        val company = "company"
        val address = OriginShippingAddress.EMPTY.copy(
            firstName = name,
            company = company
        )
        whenever(addressValidator.validateFieldRequired(any())).doReturn(null)
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.name.error).isNull()
        assertThat(editableAddress.company.error).isNull()
    }

    @Test
    fun `when name is empty and company is empty, then name error is not null`() = testBlocking {
        val name = ""
        val company = ""
        val address = OriginShippingAddress.EMPTY.copy(
            firstName = name,
            company = company
        )
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired(any())).doReturn(null)
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.name.error).isNotEmpty()
        assertThat(editableAddress.company.error).isNull()
    }

    @Test
    fun `when address is empty then address error is not null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.address.error).isNotEmpty()
    }

    @Test
    fun `when address is NOT empty then address error is null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY.copy(address1 = "This is an address")
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.address.error).isNull()
    }

    @Test
    fun `when city is empty then address error is not null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.city.error).isNotEmpty()
    }

    @Test
    fun `when city is NOT empty then address error is null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY.copy(city = "This is a city")
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.city.error).isNull()
    }

    @Test
    fun `when postal code is empty then address error is not null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.postalCode.error).isNotEmpty()
    }

    @Test
    fun `when postal code is NOT empty then address error is null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY.copy(postcode = "This is a post code")
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.postalCode.error).isNull()
    }

    @Test
    fun `when email is empty then address error is not null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.email.error).isNotEmpty()
    }

    @Test
    fun `when email is NOT empty then address error is null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY.copy(email = "This is an email")
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.email.error).isNull()
    }

    @Test
    fun `when phone is empty then phone error is not null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        whenever(addressValidator.validateUSCustomsPhone("")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.phone.error).isNotEmpty()
    }

    @Test
    fun `when phone is NOT empty and invalid then phone error is not null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY.copy(phone = "1234567890")
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        whenever(addressValidator.validateUSCustomsPhone("1234567890")).doReturn("error")
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.phone.error).isNotEmpty()
    }

    @Test
    fun `when phone is NOT empty and valid then phone error is null`() = testBlocking {
        val address = OriginShippingAddress.EMPTY.copy(phone = "1234567890")
        whenever(addressValidator.validateAtLeastOneOf(eq(""), eq(""))).doReturn("error")
        whenever(addressValidator.validateFieldRequired("")).doReturn("error")
        whenever(addressValidator.validateUSCustomsPhone("1234567890")).doReturn(null)
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val editableAddress = (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).editableAddress

        assertThat(editableAddress.phone.error).isNull()
    }

    @Test
    fun `when company is NOT empty, then company control is expanded`() = testBlocking {
        val company = "company"
        val address = OriginShippingAddress.EMPTY.copy(
            company = company
        )
        whenever(addressValidator.validateFieldRequired(any())).doReturn(null)
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val isCompanyExpanded =
            (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).isCompanyExpanded

        assertThat(isCompanyExpanded).isTrue()
    }

    @Test
    fun `when company is empty, then company control is NOT expanded`() = testBlocking {
        val address = OriginShippingAddress.EMPTY
        whenever(addressValidator.validateFieldRequired(any())).doReturn(null)
        Snapshot.withMutableSnapshot {
            createViewModel(address)
        }

        advanceUntilIdle()

        val result = sut.viewState.value

        assertThat(result).isInstanceOf(WooShippingEditOriginViewModel.EditAddressViewState::class.java)
        val isCompanyExpanded =
            (result as WooShippingEditOriginViewModel.EditAddressViewState.DataState).isCompanyExpanded

        assertThat(isCompanyExpanded).isFalse()
    }
}
