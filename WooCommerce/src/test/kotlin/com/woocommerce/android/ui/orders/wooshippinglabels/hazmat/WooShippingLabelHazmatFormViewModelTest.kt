package com.woocommerce.android.ui.orders.wooshippinglabels.hazmat

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WooShippingLabelHazmatFormViewModelTest : BaseUnitTest() {
    private lateinit var viewModel: WooShippingLabelHazmatFormViewModel

    @Before
    fun setup() {
        viewModel = WooShippingLabelHazmatFormViewModel(SavedStateHandle())
    }

    @Test
    fun `when initialized, then containsHazmatChecked is false by default`() = testBlocking {
        // Given/When
        var capturedViewState: WooShippingLabelHazmatFormViewModel.ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }

        // Then
        assertThat(capturedViewState?.containsHazmatChecked).isFalse()
    }

    @Test
    fun `when onContainsHazmatChanged is called with true, then viewState is updated accordingly`() = testBlocking {
        // Given
        var capturedViewState: WooShippingLabelHazmatFormViewModel.ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }

        // When
        viewModel.onContainsHazmatChanged(true)

        // Then
        assertThat(capturedViewState?.containsHazmatChecked).isTrue()
    }

    @Test
    fun `when onContainsHazmatChanged is called with false, then viewState is updated accordingly`() = testBlocking {
        // Given
        var capturedViewState: WooShippingLabelHazmatFormViewModel.ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }

        // When
        viewModel.onContainsHazmatChanged(false)

        // Then
        assertThat(capturedViewState?.containsHazmatChecked).isFalse()
    }

    @Test
    fun `when onSelectCategoryClick is called, then OnSelectCategoryClicked event is triggered`() = testBlocking {
        // Given
        var capturedEvent: WooShippingLabelHazmatFormViewModel.OnSelectCategoryClicked? = null
        viewModel.event.observeForever {
            capturedEvent = it as? WooShippingLabelHazmatFormViewModel.OnSelectCategoryClicked
        }

        // When
        viewModel.onSelectCategoryClick()

        // Then
        assertThat(capturedEvent).isInstanceOf(WooShippingLabelHazmatFormViewModel.OnSelectCategoryClicked::class.java)
    }

    @Test
    fun `when multiple calls to onContainsHazmatChanged, then viewState reflects latest value`() = testBlocking {
        // Given
        var capturedViewState: WooShippingLabelHazmatFormViewModel.ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }

        // When
        viewModel.onContainsHazmatChanged(true)
        viewModel.onContainsHazmatChanged(false)
        viewModel.onContainsHazmatChanged(true)

        // Then
        assertThat(capturedViewState?.containsHazmatChecked).isTrue()
    }
}
