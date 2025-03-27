package com.cataloghub.android.ui.orders.wooshippinglabels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.StartCustomsFormEdit
import com.cataloghub.android.ui.orders.wooshippinglabels.WooShippingLabelCreationViewModel.StartPackageSelection
import com.cataloghub.android.ui.orders.wooshippinglabels.packages.WooShippingLabelPackageCreationFragment.Companion.PACKAGE_SELECTION_RESULT
import com.cataloghub.android.ui.orders.wooshippinglabels.packages.ui.PackageData
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class WooShippingLabelCreationFragment : BaseFragment(), BackPressListener {
    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

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
        setupResultHandlers()
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
                        .actionWooShippingLabelCreationFragmentToWooShippingLabelPurchasedFragment(
                            purchaseData = event.purchaseData
                        ).let { findNavController().navigateSafely(it) }
                }

                is WooShippingLabelCreationViewModel.StartOriginAddressEdit ->
                    WooShippingLabelCreationFragmentDirections
                        .actionWooShippingLabelCreationFragmentToWooShippingEditOriginAddressFragment(
                            originAddress = event.originAddress
                        ).let { findNavController().navigateSafely(it) }

                is StartCustomsFormEdit -> {
                    WooShippingLabelCreationFragmentDirections
                        .actionWooShippingLabelCreationFragmentToWooShippingLabelCustomsFormFragment()
                        .let { findNavController().navigateSafely(it) }
                }
                is MultiLiveEvent.Event.ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is MultiLiveEvent.Event.Exit -> findNavController().navigateUp()
            }
        }
    }

    private fun setupResultHandlers() {
        handleResult<PackageData>(PACKAGE_SELECTION_RESULT) {
            viewModel.onPackageSelected(it)
        }
    }

    override fun onRequestAllowBackPress(): Boolean = viewModel.allowBackNavigation()
}
