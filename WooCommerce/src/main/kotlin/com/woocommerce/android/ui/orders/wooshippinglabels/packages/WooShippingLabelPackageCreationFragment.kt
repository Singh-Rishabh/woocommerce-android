package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.woocommerce.android.R
import com.woocommerce.android.extensions.handleDialogResult
import com.woocommerce.android.extensions.navigateSafely
import com.woocommerce.android.ui.base.BaseFragment
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageSelected
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ShowPackageTypeDialog
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WooShippingLabelPackageCreationFragment : BaseFragment() {
    val viewModel: WooShippingLabelPackageCreationViewModel by viewModels()

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
                is PackageSelected -> handlePackageDataAsResult(event.packageData)
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

    private fun handlePackageDataAsResult(packageData: PackageData) {
        setFragmentResult(
            PACKAGE_SELECTION_RESULT,
            Bundle().apply { putParcelable(PACKAGE_SELECTION_RESULT, packageData) }
        )
    }

    companion object {
        const val SELECTOR_REQUEST_KEY = "package_type"
        const val PACKAGE_SELECTION_RESULT = "package_selection"
    }
}
