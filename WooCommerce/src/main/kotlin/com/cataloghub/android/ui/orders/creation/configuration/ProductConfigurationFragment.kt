package com.cataloghub.android.ui.orders.creation.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.products.variations.picker.VariationPickerFragment
import com.cataloghub.android.ui.products.variations.picker.VariationPickerViewModel
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductConfigurationFragment : BaseFragment() {
    companion object {
        const val PRODUCT_CONFIGURATION_RESULT = "product-configuration-result"
        const val PRODUCT_CONFIGURATION_EDITED_RESULT = "product-configuration-edited-result"
    }

    private val viewModel: ProductConfigurationViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            id = R.id.product_configuration_view

            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

            setContent {
                WooThemeWithBackground {
                    ProductConfigurationScreen(viewModel)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        handleResults()
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MultiLiveEvent.Event.Exit -> findNavController().navigateUp()
                is MultiLiveEvent.Event.ExitWithResult<*> -> {
                    when (event.data) {
                        is SelectProductConfigurationResult -> navigateBackWithResult(
                            PRODUCT_CONFIGURATION_RESULT,
                            event.data
                        )

                        is EditProductConfigurationResult -> navigateBackWithResult(
                            PRODUCT_CONFIGURATION_EDITED_RESULT,
                            event.data
                        )
                    }
                }

                is ProductConfigurationNavigationTarget -> {
                    ProductConfigurationNavigator.navigate(this, event)
                }
            }
        }
    }

    private fun handleResults() {
        handleResult<VariationPickerViewModel.VariationPickerResult>(VariationPickerFragment.VARIATION_PICKER_RESULT) {
            viewModel.onUpdateVariationConfiguration(it.itemId, it.variationId, it.attributes)
        }
    }
}
