package com.woocommerce.android.ui.orders.wooshippinglabels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.woocommerce.android.extensions.navigateSafely
import com.woocommerce.android.ui.base.BaseFragment
import com.woocommerce.android.ui.compose.theme.WooThemeWithBackground
import com.woocommerce.android.ui.main.AppBarStatus
import com.woocommerce.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.StartPackageSelection
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WooShippingLabelCreationFragment : BaseFragment() {
    private val viewModel: WooShippingLabelCreationViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    Surface {
                        WooShippingLabelCreationScreen(viewModel)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    override val activityAppBarStatus: AppBarStatus = AppBarStatus.Hidden

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is StartPackageSelection ->
                    WooShippingLabelCreationFragmentDirections
                        .actionWooShippingLabelCreationFragmentToWooShippingLabelPackageCreationFragment()
                        .let { findNavController().navigateSafely(it) }

                is WooShippingLabelCreationViewModel.LabelPurchased -> {
                    WooShippingLabelCreationFragmentDirections
                        .actionWooShippingLabelCreationFragmentToWooShippingLabelPurchasedFragment()
                        .let {
                            val navController = findNavController()
                            navController.navigateSafely(it)
                        }
                }
            }
        }
    }
}
