package com.woocommerce.android.ui.orders.wooshippinglabels.packages

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.woocommerce.android.extensions.navigateSafely
import com.woocommerce.android.ui.base.BaseFragment
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.CarrierPackageSelected
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.CustomPackageCreated
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.PackageType
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.SavedPackageSelected
import com.woocommerce.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationViewModel.ShowPackageTypeDialog
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
    }

    private fun bindEventListener() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowPackageTypeDialog -> handlePackageTypeSelection(event.currentSelection)
                is CarrierPackageSelected -> handleCarrierPackageSelection()
                is CustomPackageCreated -> handleCustomPackageCreation()
                is SavedPackageSelected -> handleSavedPackageSelection()
            }
        }
    }

    private fun handlePackageTypeSelection(currentSelection: PackageType) {
        WooShippingLabelPackageCreationFragmentDirections
            .actionWooShippingLabelPackageCreationFragmentToItemSelectorDialog(
                selectedItem = currentSelection.name,
                keys = PackageType.entries.map { it.name }.toTypedArray(),
                values = PackageType.entries
                    .map { getString(it.resourceId) }
                    .toTypedArray(),
                requestKey = "package_type"
            ).let { findNavController().navigateSafely(it) }
    }

    private fun handleCarrierPackageSelection() {

    }

    private fun handleCustomPackageCreation() {

    }

    private fun handleSavedPackageSelection() {

    }
}
