package com.cataloghub.android.ui.orders.tracking

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.AppPrefs
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentAddShipmentTrackingBinding
import com.cataloghub.android.extensions.handleResult
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.tools.NetworkStatus
import com.cataloghub.android.ui.barcodescanner.BarcodeScanningFragment.Companion.KEY_BARCODE_SCANNING_SCAN_STATUS
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.dialog.WooDialog
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.ui.orders.OrderNavigationTarget
import com.cataloghub.android.ui.orders.OrderNavigator
import com.cataloghub.android.ui.orders.creation.CodeScannerStatus
import com.cataloghub.android.ui.orders.tracking.AddOrderShipmentTrackingViewModel.SaveTrackingPrefsEvent
import com.cataloghub.android.ui.orders.tracking.AddOrderShipmentTrackingViewModel.SetScannedTrackingNumberEvent
import com.cataloghub.android.ui.orders.tracking.AddOrderShipmentTrackingViewModel.ShowTrackingNumberScanFailed
import com.cataloghub.android.util.DateUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowDialog
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.widgets.AppRatingDialog
import com.cataloghub.android.widgets.CustomProgressDialog
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.util.ActivityUtils
import java.util.Calendar
import javax.inject.Inject
import org.wordpress.android.fluxc.utils.DateUtils as FluxCDateUtils

@AndroidEntryPoint
class AddOrderShipmentTrackingFragment :
    BaseFragment(R.layout.fragment_add_shipment_tracking),
    BackPressListener {
    companion object {
        const val KEY_ADD_SHIPMENT_TRACKING_RESULT = "key_add_shipment_tracking_result"
    }

    @Inject lateinit var networkStatus: NetworkStatus

    @Inject lateinit var uiMessageResolver: UIMessageResolver

    @Inject lateinit var navigator: OrderNavigator

    @Inject lateinit var dateUtils: DateUtils

    private val viewModel: AddOrderShipmentTrackingViewModel by viewModels()

    private var dateShippedPickerDialog: DatePickerDialog? = null
    private var progressDialog: CustomProgressDialog? = null

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_add_shipment_tracking, container, false)
    }

    override fun getFragmentTitle() = getString(R.string.order_shipment_tracking_toolbar_title)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentAddShipmentTrackingBinding.bind(view)
        setupToolbar(binding)
        initUi(binding)
        setupObservers(binding)
    }

    private fun setupToolbar(binding: FragmentAddShipmentTrackingBinding) {
        binding.toolbar.inflateMenu(R.menu.menu_add)
        binding.toolbar.title = getString(R.string.order_shipment_tracking_toolbar_title)
        binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireActivity(),
            R.drawable.ic_gridicons_cross_24dp
        )
        binding.toolbar.setNavigationOnClickListener {
            if (onRequestAllowBackPress()) {
                findNavController().navigateUp()
            }
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            onMenuItemSelected(menuItem)
        }
    }

    private fun setupObservers(binding: FragmentAddShipmentTrackingBinding) {
        viewModel.addOrderShipmentTrackingViewStateData.observe(viewLifecycleOwner) { old, new ->
            // Carrier data
            new.carrier.takeIfNotEqualTo(old?.carrier) {
                if (it.isCustom) {
                    showCustomProviderFields(binding)
                    binding.carrier.setText(getString(R.string.order_shipment_tracking_custom_provider_section_name))
                    if (binding.customProviderName.text.toString() != it.name) {
                        binding.customProviderName.setText(it.name)
                    }
                } else {
                    hideCustomProviderFields(binding)
                    binding.carrier.setText(it.name)
                }
            }
            new.carrierError.takeIfNotEqualTo(old?.carrierError) { error ->
                binding.carrierLayout.error = error?.let { getString(it) }
            }
            new.customCarrierNameError.takeIfNotEqualTo(old?.customCarrierNameError) { error ->
                binding.customProviderNameLayout.error = error?.let { getString(it) }
            }

            // tracking number
            new.trackingNumber.takeIfNotEqualTo(old?.trackingNumber) {
                if (binding.trackingNumber.text.toString() != it) {
                    binding.trackingNumber.setText(it)
                }
            }
            new.trackingNumberError.takeIfNotEqualTo(old?.trackingNumberError) { error ->
                binding.trackingNumberLayout.error = error?.let { getString(it) }
            }

            // custom URL
            new.trackingLink.takeIfNotEqualTo(old?.trackingLink) {
                if (binding.customProviderUrl.text.toString() != it) {
                    binding.customProviderUrl.setText(it)
                }
            }

            new.date.takeIfNotEqualTo(old?.date) {
                dateUtils.getLocalizedLongDateString(requireActivity(), it)
                    .let { localizedString ->
                        binding.date.setText(localizedString.orEmpty())
                    }
            }

            new.showLoadingProgress.takeIfNotEqualTo(old?.showLoadingProgress) {
                showProgressDialog(it)
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is OrderNavigationTarget -> navigator.navigate(this, event)
                is ShowDialog -> event.showDialog()
                is Exit -> findNavController().navigateUp()
                is ExitWithResult<*> -> navigateBackWithResult(KEY_ADD_SHIPMENT_TRACKING_RESULT, event.data)
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is SaveTrackingPrefsEvent -> {
                    AppPrefs.setSelectedShipmentTrackingProviderName(event.carrier.name)
                    AppPrefs.setIsSelectedShipmentTrackingProviderNameCustom(event.carrier.isCustom)
                }

                is SetScannedTrackingNumberEvent -> {
                    binding.trackingNumber.setText(event.trackingNumber)
                    viewModel.onTrackingNumberEntered(event.trackingNumber)
                }

                is ShowTrackingNumberScanFailed -> {
                    uiMessageResolver.showSnack(event.errorMessage)
                }

                else -> event.isHandled = false
            }
        }

        handleResult<Carrier>(AddOrderTrackingProviderListFragment.SHIPMENT_TRACKING_PROVIDER_RESULT) {
            viewModel.onCarrierSelected(it)
        }

        handleResult<CodeScannerStatus>(KEY_BARCODE_SCANNING_SCAN_STATUS) { status ->
            viewModel.handleBarcodeScannedStatus(status)
        }
    }

    private fun initUi(binding: FragmentAddShipmentTrackingBinding) {
        binding.carrier.setOnClickListener {
            viewModel.onCarrierClicked()
        }

        // Let's not hide the scan button with the error icon
        binding.trackingNumberLayout.errorIconDrawable = null
        binding.trackingNumberLayout.setEndIconOnClickListener {
            viewModel.onScanTrackingNumberClicked()
        }

        binding.date.setOnClickListener {
            val calendar = FluxCDateUtils.getCalendarInstance(viewModel.currentSelectedDate)
            dateShippedPickerDialog = DatePickerDialog(
                requireActivity(),
                { _, year, month, dayOfMonth ->
                    viewModel.onDateChanged("$year-${month + 1}-$dayOfMonth")
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
            )
            dateShippedPickerDialog?.show()
        }

        binding.customProviderName.doAfterTextChanged { text ->
            if (!binding.customProviderNameLayout.isVisible) return@doAfterTextChanged
            viewModel.onCustomCarrierNameEntered(text.toString())
        }
        binding.trackingNumber.doAfterTextChanged { text ->
            viewModel.onTrackingNumberEntered(text.toString())
        }
        binding.customProviderUrl.doAfterTextChanged { text ->
            if (!binding.customProviderUrlLayout.isVisible) return@doAfterTextChanged
            viewModel.onTrackingLinkEntered(text.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onStop() {
        super.onStop()
        WooDialog.onCleared()
        dateShippedPickerDialog?.dismiss()
        dateShippedPickerDialog = null

        activity?.let {
            ActivityUtils.hideKeyboard(it)
        }
    }

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_add -> {
                activity?.let {
                    ActivityUtils.hideKeyboard(it)
                }
                AppRatingDialog.incrementInteractions()
                viewModel.onAddButtonTapped()
                true
            }
            else -> false
        }
    }

    override fun onRequestAllowBackPress(): Boolean {
        return viewModel.onBackButtonPressed()
    }

    private fun showCustomProviderFields(binding: FragmentAddShipmentTrackingBinding) {
        binding.customProviderNameLayout.visibility = View.VISIBLE
        binding.customProviderUrlLayout.visibility = View.VISIBLE
    }

    private fun hideCustomProviderFields(binding: FragmentAddShipmentTrackingBinding) {
        binding.customProviderNameLayout.visibility = View.GONE
        binding.customProviderUrlLayout.visibility = View.GONE
    }

    private fun showProgressDialog(show: Boolean) {
        progressDialog?.dismiss()
        if (show) {
            progressDialog = CustomProgressDialog.show(
                getString(R.string.order_shipment_tracking_progress_title),
                getString(R.string.order_shipment_tracking_progress_message)
            ).also {
                it.show(parentFragmentManager, CustomProgressDialog.TAG)
            }
            progressDialog?.isCancelable = false
        }
    }
}
