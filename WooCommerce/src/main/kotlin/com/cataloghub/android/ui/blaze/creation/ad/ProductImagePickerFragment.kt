package com.cataloghub.android.ui.blaze.creation.ad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.blaze.creation.ad.ProductImagePickerViewModel.ImageSelectedResult
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductImagePickerFragment : BaseFragment() {
    companion object {
        const val ON_PRODUCT_IMAGE_SELECTED = "on_product_image_selected"
    }

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    val viewModel: ProductImagePickerViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return composeView {
            ProductImagePickerScreen(viewModel = viewModel)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is Exit -> findNavController().popBackStack()
                is ExitWithResult<*> -> {
                    navigateBackWithResult(ON_PRODUCT_IMAGE_SELECTED, event.data as ImageSelectedResult)
                }
            }
        }
    }
}
