package com.cataloghub.android.ui.orders.wooshippinglabels.packages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.extensions.handleDialogResult
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.dialog.WooDialog
import com.cataloghub.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageSelected
import com.cataloghub.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.cataloghub.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ShowLoadingDialog
import com.cataloghub.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ShowPackageTypeDialog
import com.cataloghub.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ShowTemplateCreationErrorDialog
import com.cataloghub.android.widgets.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WooShippingLabelPackageCreationFragment : BaseFragment() {
    val viewModel: WooShippingLabelPackageCreationViewModel by viewModels()

    private var progressDialog: CustomProgressDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    WooShippingLabelPackageCreationScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindEventListener()
        bindResultHandlers()
    }

    private fun bindEventListener() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowPackageTypeDialog -> handlePackageTypeSelection(event.currentSelection)
                is ShowLoadingDialog -> showLoadingDialog(event.show)
                is ShowTemplateCreationErrorDialog -> handleTemplateCreationError()
                is PackageSelected -> navigateBackWithResult(PACKAGE_SELECTION_RESULT, event.packageData)
            }
        }
    }

    private fun bindResultHandlers() {
        handleDialogResult<String>(
            key = SELECTOR_REQUEST_KEY,
            entryId = R.id.wooShippingLabelPackageCreationFragment
        ) { result ->
            PackageType.entries
                .firstOrNull { it.toString() == result }
                ?.let { viewModel.onPackageTypeSelected(it) }
        }
    }

    private fun handlePackageTypeSelection(currentSelection: PackageType) {
        WooShippingLabelPackageCreationFragmentDirections
            .actionWooShippingLabelPackageCreationFragmentToItemSelectorDialog(
                requestKey = SELECTOR_REQUEST_KEY,
                selectedItem = currentSelection.name,
                values = PackageType.entries.map { it.name }.toTypedArray(),
                keys = PackageType.entries
                    .map { getString(it.resourceId) }
                    .toTypedArray()
            ).let { findNavController().navigateSafely(it) }
    }

    private fun showLoadingDialog(show: Boolean) {
        if (show) {
            progressDialog = CustomProgressDialog.show(
                title = getString(R.string.loading),
                message = getString(R.string.please_wait)
            ).also { it.show(parentFragmentManager, CustomProgressDialog.TAG) }
        } else {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    private fun handleTemplateCreationError() {
        WooDialog.showDialog(
            activity = requireActivity(),
            titleId = R.string.woo_shipping_labels_package_creation_error_title,
            messageId = R.string.woo_shipping_labels_package_creation_error_message,
            positiveButtonId = R.string.woo_shipping_labels_package_creation_error_proceed,
            neutralButtonId = R.string.woo_shipping_labels_package_creation_error_cancel,
            posBtnAction = { _, _ ->
                viewModel.onAddCustomPackageClick(savePackageAsTemplate = false)
            }
        )
    }

    companion object {
        const val SELECTOR_REQUEST_KEY = "package_type"
        const val PACKAGE_SELECTION_RESULT = "package_selection"
    }
}
