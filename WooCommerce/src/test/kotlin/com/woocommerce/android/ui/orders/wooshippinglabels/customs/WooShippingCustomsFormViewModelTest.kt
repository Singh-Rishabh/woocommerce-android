package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ContentType
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.RestrictionType
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ShowContentTypeDialog
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ShowRestrictionTypeDialog
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ViewState
import com.woocommerce.android.viewmodel.MultiLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class WooShippingCustomsFormViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var observer: Observer<ViewState>

    @Mock
    private lateinit var eventObserver: Observer<MultiLiveEvent.Event>

    private lateinit var viewModel: WooShippingCustomsFormViewModel

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = WooShippingCustomsFormViewModel(
            savedState = SavedStateHandle()
        )
        viewModel.event.observeForever(eventObserver)
    }

    @Test
    fun `onContentTypeClick should trigger ShowContentTypeDialog event`() = runBlockingTest {
        viewModel.onContentTypeClick()
        verify(eventObserver).onChanged(ShowContentTypeDialog(ContentType.MERCHANDISE))
    }

    @Test
    fun `onRestrictionTypeClick should trigger ShowRestrictionTypeDialog event`() = runBlockingTest {
        viewModel.onRestrictionTypeClick()
        verify(eventObserver).onChanged(ShowRestrictionTypeDialog(RestrictionType.NONE))
    }

    @Test
    fun `onITNChanged should update itnValue in viewState`() = runBlockingTest {
        val newItnValue = "123456"
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onITNChanged(newItnValue)
        assert(capturedViewState?.itnValue == newItnValue)
    }

    @Test
    fun `onReturnToSenderChanged should update returnToSenderChecked in viewState`() = runBlockingTest {
        val isChecked = true
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onReturnToSenderChanged(isChecked)
        assert(capturedViewState?.returnToSenderChecked == isChecked)
    }

    @Test
    fun `onContentTypeSelected should update contentType in viewState`() = runBlockingTest {
        val contentType = ContentType.GIFT
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onContentTypeSelected(contentType)
        assert(capturedViewState?.contentType == contentType)
    }

    @Test
    fun `onRestrictionTypeSelected should update restrictionType in viewState`() = runBlockingTest {
        val restrictionType = RestrictionType.QUARANTINE
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onRestrictionTypeSelected(restrictionType)
        assert(capturedViewState?.restrictionType == restrictionType)
    }

    @Test
    fun `onOtherContentInputChanged should update otherContentInput in viewState`() = runBlockingTest {
        val newValue = "Important Stuff"
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onOtherContentInputChanged(newValue)
        assert(capturedViewState?.otherContentInput == newValue)
    }

    @Test
    fun `onRestrictionDetailsInputChanged should update otherRestrictionInput in viewState`() = runBlockingTest {
        val newValue = "Restricted Stuff"
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onRestrictionDetailsInputChanged(newValue)
        assert(capturedViewState?.otherRestrictionInput == newValue)
    }
}
