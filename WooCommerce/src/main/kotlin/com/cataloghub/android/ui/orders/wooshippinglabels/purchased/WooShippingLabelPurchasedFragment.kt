package com.cataloghub.android.ui.orders.wooshippinglabels.purchased

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Surface
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.dialog.WooDialog
import com.cataloghub.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.OpenLearnMoreScreen
import com.cataloghub.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.OpenShippingLabelFile
import com.cataloghub.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.OpenUrl
import com.cataloghub.android.ui.orders.wooshippinglabels.purchased.WooShippingLabelPurchasedViewModel.ShowError
import com.cataloghub.android.util.ActivityUtils
import com.cataloghub.android.util.ChromeCustomTabUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class WooShippingLabelPurchasedFragment : BaseFragment() {
    private val viewModel: WooShippingLabelPurchasedViewModel by viewModels()

    override fun getFragmentTitle(): String = getString(R.string.shipping_label_print_screen_title)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    Surface {
                        WooShippingLabelPurchasedScreen(viewModel)
                    }
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindEventListeners()
    }

    private fun bindEventListeners() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is OpenShippingLabelFile -> openShippingLabelPreview(event.file)
                is OpenLearnMoreScreen -> openLearnMoreView()
                is OpenUrl -> openUrl(event.url)
                is ShowError -> showErrorDialog(event.errorResId)
            }
        }
    }

    private fun openShippingLabelPreview(file: File) {
        ActivityUtils.previewPDFFile(requireActivity(), file)
    }

    private fun openLearnMoreView() {
        WooShippingLabelPurchasedFragmentDirections
            .actionWooShippingLabelPurchasedFragmentToPrintShippingLabelInfoFragment()
            .let { findNavController().navigate(it) }
    }

    private fun openUrl(url: String) {
        ChromeCustomTabUtils.launchUrl(requireContext(), url)
    }

    private fun showErrorDialog(messageResId: Int) {
        WooDialog.showDialog(
            activity = requireActivity(),
            titleId = R.string.error_generic,
            messageId = messageResId,
            positiveButtonId = R.string.dialog_ok
        )
    }
}
