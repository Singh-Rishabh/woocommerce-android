package com.cataloghub.android.ui.products

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.extensions.isTwoPanesShouldBeUsed
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.composeView
import com.cataloghub.android.ui.products.UpdateProductStockStatusViewModel.UpdateStockStatusUiState
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.util.DisplayUtils
import javax.inject.Inject

@AndroidEntryPoint
class UpdateProductStockStatusFragment : DialogFragment() {

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver
    private val viewModel: UpdateProductStockStatusViewModel by viewModels()

    companion object {
        const val UPDATE_STOCK_STATUS_EXIT_STATE_KEY = "update_stock_status_exit_state_key"

        private const val LANDSCAPE_WIDTH_RATIO = 0.5f
        private const val LANDSCAPE_HEIGHT_RATIO = 0.8f

        private const val PORTRAIT_WIDTH_RATIO = 0.7f
        private const val PORTRAIT_HEIGHT_RATIO = 0.5f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (context?.isTwoPanesShouldBeUsed == true) {
            setStyle(STYLE_NO_TITLE, R.style.Theme_Woo_Dialog)
        } else {
            setStyle(STYLE_NO_TITLE, R.style.Theme_Woo)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return composeView {
            val uiState by viewModel.viewState.observeAsState(UpdateStockStatusUiState())

            UpdateProductStockStatusScreen(
                currentStockStatusState = uiState.currentStockStatusState,
                statusMessage = uiState.statusMessage,
                currentProductStockStatus = uiState.currentProductStockStatus,
                stockStatuses = uiState.stockStockStatuses,
                isProgressDialogVisible = uiState.isProgressDialogVisible,
                onStockStatusChanged = { newStatus ->
                    viewModel.onStockStatusSelected(newStatus)
                },
                onNavigationUpClicked = { viewModel.onBackPressed() },
                onUpdateClicked = {
                    viewModel.onDoneButtonClicked()
                }
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupObservers()
    }

    override fun onStart() {
        super.onStart()
        val isTwoPaneLayout = context?.isTwoPanesShouldBeUsed
        val width = DisplayUtils.getWindowPixelWidth(requireContext())
        val height = DisplayUtils.getWindowPixelHeight(requireContext())

        val isLandscape = DisplayUtils.isLandscape(context)

        val (widthRatio, heightRatio) = when {
            isTwoPaneLayout == false -> 1f to 1f
            isLandscape -> LANDSCAPE_WIDTH_RATIO to LANDSCAPE_HEIGHT_RATIO
            else -> PORTRAIT_WIDTH_RATIO to PORTRAIT_HEIGHT_RATIO
        }

        dialog?.window?.setLayout(
            (width * widthRatio).toInt(),
            (height * heightRatio).toInt()
        )
    }

    private fun setupObservers() {
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MultiLiveEvent.Event.Exit -> findNavController().popBackStack()
                is MultiLiveEvent.Event.ExitWithResult<*> -> {
                    navigateBackWithResult(
                        UPDATE_STOCK_STATUS_EXIT_STATE_KEY,
                        event.data
                    )
                }

                is MultiLiveEvent.Event.ShowSnackbar -> {
                    uiMessageResolver.showSnack(event.message)
                }

                else -> event.isHandled = false
            }
        }
    }
}
