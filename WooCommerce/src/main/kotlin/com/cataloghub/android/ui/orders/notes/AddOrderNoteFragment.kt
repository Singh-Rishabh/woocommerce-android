package com.cataloghub.android.ui.orders.notes

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent.ADD_ORDER_NOTE_ADD_BUTTON_TAPPED
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentAddOrderNoteBinding
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.dialog.WooDialog
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialog
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.widgets.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.util.ActivityUtils
import javax.inject.Inject

@AndroidEntryPoint
class AddOrderNoteFragment : BaseFragment(R.layout.fragment_add_order_note), BackPressListener {
    companion object {
        const val TAG = "AddOrderNoteFragment"
        const val KEY_ADD_NOTE_RESULT = "key_add_note_result"
    }

    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private val viewModel: AddOrderNoteViewModel by viewModels()

    private var progressDialog: CustomProgressDialog? = null

    private var addMenuItem: MenuItem? = null

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddOrderNoteBinding.bind(view)
        setupToolbar(binding)
        initUi(binding)
        setupObservers(binding)

        if (savedInstanceState == null) {
            binding.addNoteEditor.requestFocus()
            ActivityUtils.showKeyboard(binding.addNoteEditor)
        }
    }

    private fun setupToolbar(binding: FragmentAddOrderNoteBinding) {
        binding.toolbar.title = viewModel.screenTitle
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            onMenuItemSelected(menuItem)
        }
        // Set up the toolbar menu
        binding.toolbar.inflateMenu(R.menu.menu_add)
        binding.toolbar.setNavigationOnClickListener {
            viewModel.onBackPressed()
        }
        setupToolbarMenu(binding.toolbar.menu)
    }

    private fun setupToolbarMenu(menu: Menu) {
        addMenuItem = menu.findItem(R.id.menu_add)
        addMenuItem!!.isVisible = viewModel.shouldShowAddButton
    }

    override fun getFragmentTitle() = viewModel.screenTitle

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onStop() {
        super.onStop()
        WooDialog.onCleared()
        activity?.let {
            ActivityUtils.hideKeyboard(it)
        }
    }

    fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add -> {
                AnalyticsTracker.track(ADD_ORDER_NOTE_ADD_BUTTON_TAPPED)
                activity?.let {
                    ActivityUtils.hideKeyboard(it)
                }
                viewModel.pushOrderNote()
                true
            }
            else -> false
        }
    }

    override fun onRequestAllowBackPress(): Boolean {
        viewModel.onBackPressed()
        return false
    }

    private fun initUi(binding: FragmentAddOrderNoteBinding) {
        binding.addNoteEditor.doOnTextChanged { text, _, _, _ ->
            viewModel.onOrderTextEntered(text.toString())
        }

        binding.addNoteSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onIsCustomerCheckboxChanged(isChecked)
        }
    }

    private fun setupObservers(binding: FragmentAddOrderNoteBinding) {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ExitWithResult<*> -> navigateBackWithResult(KEY_ADD_NOTE_RESULT, event.data)
                is Exit -> findNavController().navigateUp()
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ShowDialog -> event.showDialog()
            }
        }

        viewModel.addOrderNoteViewStateData.observe(viewLifecycleOwner) { old, new ->
            new.draftNote.takeIfNotEqualTo(old?.draftNote) {
                if (binding.addNoteEditor.text.toString() != it.note) {
                    binding.addNoteEditor.setText(it.note)
                }
                binding.addNoteSwitch.isChecked = it.isCustomerNote

                val noteIcon = if (it.isCustomerNote) R.drawable.ic_note_public else R.drawable.ic_note_private
                binding.addNoteIcon.setImageResource(noteIcon)
            }

            new.showCustomerNoteSwitch.takeIfNotEqualTo(old?.showCustomerNoteSwitch) {
                binding.addNoteSwitch.isVisible = it
            }

            new.canAddNote.takeIfNotEqualTo(old?.canAddNote) {
                addMenuItem?.isVisible = it
            }

            new.isProgressDialogShown.takeIfNotEqualTo(old?.isProgressDialogShown) {
                showProgressDialog(it)
            }
        }
    }

    private fun showProgressDialog(show: Boolean) {
        progressDialog?.dismiss()
        if (show) {
            progressDialog = CustomProgressDialog.show(
                getString(R.string.add_order_note_progress_title),
                getString(R.string.add_order_note_progress_message)
            ).also {
                it.show(parentFragmentManager, CustomProgressDialog.TAG)
            }
            progressDialog?.isCancelable = false
        }
    }
}
