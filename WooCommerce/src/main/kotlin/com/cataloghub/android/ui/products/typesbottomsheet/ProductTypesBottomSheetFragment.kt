package com.cataloghub.android.ui.products.typesbottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.R
import com.cataloghub.android.databinding.DialogProductDetailBottomSheetListBinding
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.ui.dialog.WooDialog
import com.cataloghub.android.ui.products.ProductNavigationTarget
import com.cataloghub.android.ui.products.ProductNavigator
import com.cataloghub.android.ui.products.typesbottomsheet.ProductTypesBottomSheetViewModel.ProductTypesBottomSheetUiItem
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.widgets.WCBottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductTypesBottomSheetFragment : WCBottomSheetDialogFragment() {
    companion object {
        const val KEY_PRODUCT_TYPE_RESULT = "key_product_type_result"
    }

    @Inject
    internal lateinit var navigator: ProductNavigator
    val viewModel: ProductTypesBottomSheetViewModel by viewModels()

    private val navArgs: ProductTypesBottomSheetFragmentArgs by navArgs()

    private var _binding: DialogProductDetailBottomSheetListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = DialogProductDetailBottomSheetListBinding.inflate(inflater)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        binding.productDetailInfoLblTitle.text = getString(R.string.product_type_list_header)
    }

    private fun setupObservers() {
        viewModel.productTypesBottomSheetList.observe(viewLifecycleOwner) {
            showProductTypeOptions(it)
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MultiLiveEvent.Event.Exit -> {
                    dismiss()
                }

                is MultiLiveEvent.Event.ShowDialog -> WooDialog.showDialog(
                    requireActivity(),
                    event.positiveBtnAction,
                    event.negativeBtnAction,
                    event.neutralBtnAction,
                    event.titleId,
                    event.messageId,
                    event.positiveButtonId,
                    event.negativeButtonId,
                    event.neutralButtonId
                )

                is MultiLiveEvent.Event.ExitWithResult<*> -> {
                    (event.data as? ProductTypesBottomSheetUiItem)?.let {
                        navigateWithSelectedResult(productTypesBottomSheetUiItem = it)
                    }
                }

                is ProductNavigationTarget -> navigator.navigate(this, event)
                else -> event.isHandled = false
            }
        }
    }

    private fun showProductTypeOptions(productTypes: List<ProductTypesBottomSheetUiItem>) {
        with(binding.productDetailInfoOptionsList) {
            adapter = ProductTypesBottomSheetAdapter(
                productTypes,
                viewModel::onProductTypeSelected
            )
            layoutManager = LinearLayoutManager(activity)
        }
    }

    private fun navigateWithSelectedResult(productTypesBottomSheetUiItem: ProductTypesBottomSheetUiItem) {
        when (navArgs.isAddProduct) {
            true -> dismiss()
            else -> navigateBackWithResult(KEY_PRODUCT_TYPE_RESULT, productTypesBottomSheetUiItem)
        }
    }
}
