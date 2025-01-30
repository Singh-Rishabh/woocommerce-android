package com.woocommerce.android.ui.orders.wooshippinglabels.customs

import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ContentType
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.RestrictionType
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ShowContentTypeDialog
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ShowRestrictionTypeDialog
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ViewState
import com.woocommerce.android.viewmodel.BaseUnitTest
import com.woocommerce.android.viewmodel.MultiLiveEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.mock

@ExperimentalCoroutinesApi
class WooShippingCustomsFormViewModelTest : BaseUnitTest() {
    private var eventObserver: Observer<MultiLiveEvent.Event> = mock()

    private lateinit var viewModel: WooShippingCustomsFormViewModel

    @Before
    fun setup() {
        viewModel = WooShippingCustomsFormViewModel(
            savedState = SavedStateHandle()
        )
        viewModel.event.observeForever(eventObserver)
    }

    @Test
    fun `onContentTypeClick should trigger ShowContentTypeDialog event`() = testBlocking {
        viewModel.onContentTypeClick()
        verify(eventObserver).onChanged(ShowContentTypeDialog(ContentType.MERCHANDISE))
    }

    @Test
    fun `onRestrictionTypeClick should trigger ShowRestrictionTypeDialog event`() = testBlocking {
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
    fun `onReturnToSenderChanged should update returnToSenderChecked in viewState`() = testBlocking {
        val isChecked = true
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onReturnToSenderChanged(isChecked)
        assert(capturedViewState?.returnToSenderChecked == isChecked)
    }

    @Test
    fun `onContentTypeSelected should update contentType in viewState`() = testBlocking {
        val contentType = ContentType.GIFT
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onContentTypeSelected(contentType)
        assert(capturedViewState?.contentType == contentType)
    }

    @Test
    fun `onRestrictionTypeSelected should update restrictionType in viewState`() = testBlocking {
        val restrictionType = RestrictionType.QUARANTINE
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onRestrictionTypeSelected(restrictionType)
        assert(capturedViewState?.restrictionType == restrictionType)
    }

    @Test
    fun `onOtherContentInputChanged should update otherContentInput in viewState`() = testBlocking {
        val newValue = "Important Stuff"
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onOtherContentInputChanged(newValue)
        assert(capturedViewState?.otherContentInput == newValue)
    }

    @Test
    fun `onRestrictionDetailsInputChanged should update otherRestrictionInput in viewState`() = testBlocking {
        val newValue = "Restricted Stuff"
        var capturedViewState: ViewState? = null
        viewModel.viewState.observeForever {
            capturedViewState = it
        }
        viewModel.onRestrictionDetailsInputChanged(newValue)
        assert(capturedViewState?.otherRestrictionInput == newValue)
    }
}
