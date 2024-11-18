package com.woocommerce.android.ui.login.jetpack.wpcom

import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.analytics.AnalyticsTrackerWrapper
import com.woocommerce.android.ui.login.WPComLoginRepository
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.MultiLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Test
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@ExperimentalCoroutinesApi
@OptIn(ExperimentalCoroutinesApi::class)
class JetpackActivationWPComEmailViewModelTest : BaseUnitTest() {

    private val savedStateHandle: SavedStateHandle = mock()
    private val wpComLoginRepository: WPComLoginRepository = mock()
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper = mock()
    private lateinit var viewModel: JetpackActivationWPComEmailViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = JetpackActivationWPComEmailViewModel(
            savedStateHandle,
            wpComLoginRepository,
            analyticsTrackerWrapper
        )
    }

    @Test
    fun `when close is clicked, then trigger Exit event and clear access token`() = testBlocking {
        viewModel.onCloseClick()

        assert(viewModel.event.value is MultiLiveEvent.Event.Exit)
        verify(wpComLoginRepository).clearAccessToken()
    }
}

