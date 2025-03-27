package com.cataloghub.android.ui.orders.tracking

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.DialogOrderTrackingProviderListBinding
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.model.OrderShipmentProvider
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.orders.tracking.AddOrderTrackingProviderListAdapter.OnProviderClickListener
import com.cataloghub.android.util.StringUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ExitWithResult
import com.cataloghub.android.viewmodel.MultiLiveEvent.Event.ShowSnackbar
import com.cataloghub.android.widgets.SkeletonView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AddOrderTrackingProviderListFragment :
    BaseFragment(R.layout.dialog_order_tracking_provider_list),
    OnQueryTextListener,
    OnProviderClickListener {
    companion object {
        const val TAG: String = "AddOrderTrackingProviderListFragment"
        const val SHIPMENT_TRACKING_PROVIDER_RESULT = "tracking-provider-result"
    }

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Hidden

    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private val viewModel: AddOrderTrackingProviderListViewModel by viewModels()

    private val providerListAdapter: AddOrderTrackingProviderListAdapter by lazy {
        val countryName = StringUtils.getCountryByCountryCode(requireContext(), viewModel.countryCode)
        AddOrderTrackingProviderListAdapter(
            context,
            countryName,
            this
        )
    }

    private var searchView: SearchView? = null

    private val skeletonView = SkeletonView()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = DialogOrderTrackingProviderListBinding.bind(view)
        setupToolbar(binding)

        initUi(binding)
        setupObservers(binding)
    }

    private fun setupToolbar(binding: DialogOrderTrackingProviderListBinding) {
        onCreateMenu(binding)
        binding.toolbar.title = getString(R.string.order_shipment_tracking_provider_toolbar_title)
        binding.toolbar.navigationIcon = AppCompatResources.getDrawable(
            requireActivity(),
            R.drawable.ic_back_24dp
        )
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun onCreateMenu(binding: DialogOrderTrackingProviderListBinding) {
        binding.toolbar.inflateMenu(R.menu.menu_search)
        val searchMenuItem = binding.toolbar.menu.findItem(R.id.menu_search)
        searchView = searchMenuItem!!.actionView as SearchView
        searchView?.let {
            val currentQuery = viewModel.trackingProviderListViewStateData.liveData.value?.query ?: ""
            it.setQuery(currentQuery, false)
            if (currentQuery.isNotEmpty()) it.isIconified = false
            it.imeOptions = it.imeOptions or EditorInfo.IME_FLAG_NO_EXTRACT_UI
            it.setOnQueryTextListener(this@AddOrderTrackingProviderListFragment)
        }
    }
    override fun onResume() {
        super.onResume()
        AnalyticsTracker.trackViewShown(this)
    }

    override fun onDestroyView() {
        searchView = null
        super.onDestroyView()
    }

    private fun setupObservers(binding: DialogOrderTrackingProviderListBinding) {
        viewModel.trackingProviderListViewStateData.observe(viewLifecycleOwner) { old, new ->
            new.providersList.takeIfNotEqualTo(old?.providersList) {
                providerListAdapter.setProviders(it)
            }

            new.showSkeleton.takeIfNotEqualTo(old?.showSkeleton) { show ->
                if (show) {
                    skeletonView.show(binding.providersView, R.layout.skeleton_tracking_provider_list, delayed = true)
                } else {
                    skeletonView.hide()
                }
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is ShowSnackbar -> uiMessageResolver.showSnack(event.message)
                is ExitWithResult<*> -> navigateBackWithResult(SHIPMENT_TRACKING_PROVIDER_RESULT, event.data)
                else -> event.isHandled = false
            }
        }
    }

    private fun initUi(binding: DialogOrderTrackingProviderListBinding) {
        providerListAdapter.selectedCarrierName = viewModel.currentSelectedProvider

        binding.providerList.apply {
            layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
            itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
            setHasFixedSize(true)
            adapter = providerListAdapter
        }
    }

    override fun getFragmentTitle(): String {
        return getString(R.string.order_shipment_tracking_provider_toolbar_title)
    }

    override fun onProviderClick(provider: OrderShipmentProvider) {
        viewModel.onProviderSelected(provider)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        viewModel.onSearchQueryChanged(query)
        org.wordpress.android.util.ActivityUtils.hideKeyboard(activity)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        viewModel.onSearchQueryChanged(newText)
        return true
    }
}
