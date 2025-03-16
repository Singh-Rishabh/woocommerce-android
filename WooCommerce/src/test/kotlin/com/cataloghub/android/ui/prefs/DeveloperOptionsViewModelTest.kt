package com.cataloghub.android.ui.prefs

import androidx.lifecycle.SavedStateHandle
import com.cataloghub.android.R
import com.cataloghub.android.model.UiString
import com.cataloghub.android.ui.prefs.developer.DeveloperOptionsRepository
import com.cataloghub.android.ui.prefs.developer.DeveloperOptionsViewModel
import com.cataloghub.android.util.captureValues
import com.cataloghub.android.viewmodel.BaseUnitTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class DeveloperOptionsViewModelTest : BaseUnitTest() {
    private lateinit var viewModel: DeveloperOptionsViewModel

    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
    private val developerOptionsRepository: DeveloperOptionsRepository = mock {
        on { observeSimulatedCardReaderEnabled() }.thenReturn(flowOf(false))
        on { observeInteracPaymentEnabled() }.thenReturn(flowOf(false))
        on { observeSavedPrivacyBannerSettings() }.thenReturn(flowOf(false))
    }

    @Before
    fun setup() {
        initViewModel()
    }

    @Test
    fun `when dev options screen accessed, then enable simulated reader label is displayed`() {
        val simulatedReaderRow = captureViewState()?.rows?.find {
            it.label == UiString.UiStringRes(R.string.enable_card_reader)
        }

        assertThat(simulatedReaderRow).isNotNull
        assertThat(simulatedReaderRow?.icon).isEqualTo(R.drawable.img_card_reader)
    }

    @Test
    fun `when simulated card reader btn toggled, then simulated reader state is enabled`() {
        testBlocking {
            whenever(developerOptionsRepository.observeSimulatedCardReaderEnabled()).thenReturn(flowOf(true))

            initViewModel()

            assertThat(
                (
                    captureViewState()?.rows?.find {
                        it.label == UiString.UiStringRes(R.string.enable_card_reader)
                    } as DeveloperOptionsViewModel.DeveloperOptionsViewState.ListItem.ToggleableListItem
                    ).isChecked
            ).isTrue()
        }
    }

    @Test
    fun `when simulated card reader btn untoggled, then simulated reader state is disabled`() {
        testBlocking {
            whenever(developerOptionsRepository.observeSimulatedCardReaderEnabled()).thenReturn(flowOf(false))

            initViewModel()

            assertThat(
                (
                    captureViewState()?.rows?.find {
                        it.label == UiString.UiStringRes(R.string.enable_card_reader)
                    } as DeveloperOptionsViewModel.DeveloperOptionsViewState.ListItem.ToggleableListItem
                    ).isChecked
            ).isFalse()
        }
    }

    @Test
    fun `given reader enabled, when dev options screen accessed, then update simulated reader row displayed`() {
        whenever(developerOptionsRepository.observeSimulatedCardReaderEnabled()).thenReturn(flowOf(true))

        initViewModel()

        assertThat(captureViewState()?.rows)
            .anyMatch {
                it.label == UiString.UiStringRes(R.string.update_simulated_reader)
            }
    }

    @Test
    fun `when simulated card reader btn toggled, then interac row displayed`() {
        whenever(developerOptionsRepository.observeSimulatedCardReaderEnabled()).thenReturn(flowOf(true))

        initViewModel()

        assertThat(captureViewState()?.rows)
            .anyMatch {
                it.label == UiString.UiStringRes(R.string.enable_interac_payment)
            }
    }

    @Test
    fun `given reader disabled, when dev options screen accessed, then update reader row not displayed`() {
        whenever(developerOptionsRepository.observeSimulatedCardReaderEnabled()).thenReturn(flowOf(false))

        initViewModel()

        assertThat(captureViewState()?.rows)
            .noneMatch {
                it.label == UiString.UiStringRes(R.string.update_simulated_reader)
            }
    }

    @Test
    fun `given reader disabled, when dev options screen accessed, then interac row not displayed`() {
        whenever(developerOptionsRepository.observeSimulatedCardReaderEnabled()).thenReturn(flowOf(false))

        initViewModel()

        assertThat(captureViewState()?.rows)
            .noneMatch {
                it.label == UiString.UiStringRes(R.string.enable_interac_payment)
            }
    }

    private fun initViewModel() {
        viewModel = DeveloperOptionsViewModel(
            savedStateHandle,
            developerOptionsRepository
        )
    }

    private fun captureViewState() = viewModel.viewState.captureValues().lastOrNull()
}
