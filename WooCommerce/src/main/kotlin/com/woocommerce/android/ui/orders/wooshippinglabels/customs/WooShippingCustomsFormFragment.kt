package com.woocommerce.android.ui.orders.wooshippinglabels.customs

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
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ContentType
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.RestrictionType
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ShowContentTypeDialog
import com.woocommerce.android.ui.orders.wooshippinglabels.customs.WooShippingCustomsFormViewModel.ShowRestrictionTypeDialog
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WooShippingCustomsFormFragment : BaseFragment() {
    private val viewModel: WooShippingCustomsFormViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooShippingCustomsFormScreen(viewModel = viewModel)
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
                is ShowContentTypeDialog -> {
                    handleItemSelection(
                        currentSelection = event.currentSelection.name,
                        selectionEntries = ContentType.entries.map { it.name }.toTypedArray(),
                        translatedEntries = ContentType.entries
                            .map { getString(it.resourceId) }
                            .toTypedArray()
                    )
                }

                is ShowRestrictionTypeDialog -> {
                    handleItemSelection(
                        currentSelection = event.currentSelection.name,
                        selectionEntries = RestrictionType.entries.map { it.name }.toTypedArray(),
                        translatedEntries = RestrictionType.entries
                            .map { getString(it.resourceId) }
                            .toTypedArray()
                    )
                }
            }
        }
    }

    private fun handleItemSelection(
        currentSelection: String,
        selectionEntries: Array<String>,
        translatedEntries: Array<String>
    ) {
        WooShippingCustomsFormFragmentDirections
            .actionWooShippingLabelCustomsFormFragmentToItemSelectorDialog(
                requestKey = SELECTOR_REQUEST_KEY,
                selectedItem = currentSelection,
                values = selectionEntries,
                keys = translatedEntries
            ).let { findNavController().navigateSafely(it) }
    }

    companion object {
        const val SELECTOR_REQUEST_KEY = "label_customs_selector"
    }
}
