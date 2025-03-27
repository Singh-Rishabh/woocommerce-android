package com.cataloghub.android.ui.orders.details.editing

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.fixedHiltNavGraphViewModels
import org.wordpress.android.util.ActivityUtils
import javax.inject.Inject

abstract class BaseOrderEditingFragment : BaseFragment, BackPressListener {
    constructor() : super()
    constructor(@LayoutRes layoutId: Int) : super(layoutId)

    protected val sharedViewModel by fixedHiltNavGraphViewModels<OrderEditingViewModel>(R.id.nav_graph_orders)

    @Inject lateinit var uiMessageResolver: UIMessageResolver

    protected var doneMenuItem: MenuItem? = null

    /**
     * The value to pass to analytics for the specific screen, used to record when the user enters or
     * exits the screen. Should be one of:
     *      AnalyticsTracker.ORDER_EDIT_CUSTOMER_NOTE
     *      AnalyticsTracker.ORDER_EDIT_SHIPPING_ADDRESS
     *      AnalyticsTracker.ORDER_EDIT_BILLING_ADDRESS
     */
    abstract val analyticsValue: String

    /**
     * This TextWatcher can be used to detect EditText changes in any order editing fragment
     */
    protected val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            // noop
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // noop
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            updateDoneMenuItem()
        }
    }

    /**
     * Descendants should return true if the user made any changes
     */
    abstract fun hasChanges(): Boolean

    /**
     * Descendants should override this to tell the shared view model to save specific changes. Note that
     * since we're using optimistic updating, a True result doesn't necessarily mean the update succeeded,
     * just that it was sent. A False result means the request couldn't be sent, either due to connection
     * problems or validation issues.
     */
    abstract fun saveChanges(): Boolean

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            trackEventStarted()
        }
    }

    @CallSuper
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    @CallSuper
    fun onPrepareMenu() {
        updateDoneMenuItem()
    }

    private fun setupObservers() {
        sharedViewModel.event.observe(
            viewLifecycleOwner
        ) { event ->
            when (event) {
                is MultiLiveEvent.Event.ShowSnackbar -> {
                    uiMessageResolver.showSnack(event.message)
                }
            }
        }

        sharedViewModel.start()
    }

    @CallSuper
    fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_done -> {
                if (saveChanges()) {
                    navigateUp()
                }
                true
            }
            else -> false
        }
    }

    protected fun updateDoneMenuItem() {
        doneMenuItem?.isVisible = hasChanges()
    }

    @CallSuper
    override fun onRequestAllowBackPress(): Boolean {
        return if (hasChanges()) {
            confirmDiscard()
            false
        } else {
            trackEventCanceled()
            true
        }
    }

    private fun confirmDiscard() {
        MultiLiveEvent.Event.ShowDialog.buildDiscardDialogEvent(
            positiveBtnAction = { _, _ ->
                navigateUp()
            }
        ).showDialog()
    }

    protected fun navigateUp() {
        trackEventCanceled()
        ActivityUtils.hideKeyboard(activity)
        findNavController().navigateUp()
    }

    private fun trackEventStarted() {
        AnalyticsTracker.track(
            AnalyticsEvent.ORDER_DETAIL_EDIT_FLOW_STARTED,
            mapOf(
                AnalyticsTracker.KEY_SUBJECT to analyticsValue
            )
        )
    }

    private fun trackEventCanceled() {
        AnalyticsTracker.track(
            AnalyticsEvent.ORDER_DETAIL_EDIT_FLOW_CANCELED,
            mapOf(
                AnalyticsTracker.KEY_SUBJECT to analyticsValue
            )
        )
    }
}
