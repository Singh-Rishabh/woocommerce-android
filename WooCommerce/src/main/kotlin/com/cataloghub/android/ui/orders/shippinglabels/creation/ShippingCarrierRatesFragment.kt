package com.cataloghub.android.ui.orders.shippinglabels.creation

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentShippingCarrierRatesBinding
import com.cataloghub.android.extensions.navigateBackWithNotice
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.MainActivity.Companion.BackPressListener
import com.cataloghub.android.util.DateUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.Exit
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.viewmodel.ResourceProvider
import com.cataloghub.android.widgets.SkeletonView
import com.cataloghub.android.widgets.WCEmptyView.EmptyViewType.SHIPPING_LABEL_CARRIER_RATES
import dagger.hilt.android.AndroidEntryPoint
import org.wordpress.android.util.ActivityUtils
import javax.inject.Inject

@AndroidEntryPoint
class ShippingCarrierRatesFragment :
    BaseFragment(R.layout.fragment_shipping_carrier_rates),
    BackPressListener {
    companion object {
        const val SHIPPING_CARRIERS_CLOSED = "shipping_carriers_closed"
        const val SHIPPING_CARRIERS_RESULT = "shipping_carriers_result"
    }

    @Inject lateinit var uiMessageResolver: UIMessageResolver

    @Inject lateinit var resourceProvider: ResourceProvider

    @Inject lateinit var dateUtils: DateUtils

    private var doneMenuItem: MenuItem? = null

    private var _binding: FragmentShippingCarrierRatesBinding? = null
    private val binding get() = _binding!!

    private val skeletonView: SkeletonView = SkeletonView()

    val viewModel: ShippingCarrierRatesViewModel by viewModels()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onStop() {
        super.onStop()
        activity?.let {
            ActivityUtils.hideKeyboard(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShippingCarrierRatesBinding.bind(view)
        setupToolbar()
        initializeViewModel()
        initializeViews()
    }

    private fun setupToolbar() {
        binding.toolbar.title = getString(R.string.shipping_label_shipping_carriers_title)
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            onMenuItemSelected(menuItem)
        }
        binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireActivity(),
            R.drawable.ic_back_24dp
        )
        binding.toolbar.setNavigationOnClickListener {
            onRequestAllowBackPress()
        }
        binding.toolbar.inflateMenu(R.menu.menu_done)
        doneMenuItem = binding.toolbar.menu.findItem(R.id.menu_done)
        doneMenuItem?.isVisible = viewModel.viewStateData.liveData.value?.isDoneButtonVisible ?: false
    }

    private fun initializeViewModel() {
        subscribeObservers()
    }

    private fun initializeViews() {
        binding.carrierRates.apply {
            adapter = binding.carrierRates.adapter ?: ShippingCarrierRatesAdapter(
                onRateSelected = viewModel::onShippingRateSelected,
                dateUtils = dateUtils
            )
            layoutManager = LinearLayoutManager(context)
            itemAnimator = DefaultItemAnimator().apply {
                supportsChangeAnimations = false
            }
        }
    }

    override fun getFragmentTitle() = getString(R.string.shipping_label_shipping_carriers_title)

    private fun onMenuItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_done -> {
                ActivityUtils.hideKeyboard(activity)
                viewModel.onDoneButtonClicked()
                true
            }
            else -> false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun subscribeObservers() {
        viewModel.shippingRates.observe(viewLifecycleOwner) { rates ->
            (binding.carrierRates.adapter as? ShippingCarrierRatesAdapter)?.items = rates
        }

        viewModel.viewStateData.observe(viewLifecycleOwner) { old, new ->
            new.bannerMessage.takeIfNotEqualTo(old?.bannerMessage) { message ->
                binding.infoBanner.isVisible = !message.isNullOrEmpty()
                binding.infoBannerMessage.text = message
            }
            new.isSkeletonVisible.takeIfNotEqualTo(old?.isSkeletonVisible) { isVisible ->
                showSkeleton(isVisible)
            }
            new.isEmptyViewVisible.takeIfNotEqualTo(old?.isEmptyViewVisible) { isVisible ->
                showEmptyView(isVisible)
            }
            new.isDoneButtonVisible.takeIfNotEqualTo(old?.isDoneButtonVisible) { isVisible ->
                doneMenuItem?.isVisible = isVisible
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ExitWithResult<*> -> navigateBackWithResult(SHIPPING_CARRIERS_RESULT, event.data)
                is Exit -> navigateBackWithNotice(SHIPPING_CARRIERS_CLOSED)
                else -> event.isHandled = false
            }
        }
    }

    private fun showEmptyView(isVisible: Boolean) {
        if (isVisible) {
            binding.emptyView.show(SHIPPING_LABEL_CARRIER_RATES)
        } else {
            binding.emptyView.hide()
        }
    }

    fun showSkeleton(show: Boolean) {
        if (show) {
            skeletonView.show(binding.carrierRates, R.layout.skeleton_shipping_label_carrier_list, delayed = false)
        } else {
            skeletonView.hide()
        }
    }

    // Let the ViewModel know the user is attempting to close the screen
    override fun onRequestAllowBackPress(): Boolean {
        return (viewModel.event.value == Exit).also { if (it.not()) viewModel.onExit() }
    }
}
