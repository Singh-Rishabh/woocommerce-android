package com.cataloghub.android.ui.products.filter

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentProductFilterOptionListBinding
import com.cataloghub.android.extensions.hide
import com.cataloghub.android.extensions.navigateToParentWithResult
import com.cataloghub.android.extensions.show
import com.cataloghub.android.extensions.takeIfNotEqualTo
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.products.OnLoadMoreListener
import com.cataloghub.android.ui.products.filter.ProductFilterListViewModel.FilterListOptionItemUiModel
import com.cataloghub.android.ui.products.filter.ProductFilterOptionListAdapter.OnProductFilterOptionClickListener
import com.cataloghub.android.ui.products.list.ProductListFragment
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.viewmodel.MultiLiveEvent
import com.cataloghub.android.viewmodel.fixedHiltNavGraphViewModels
import com.cataloghub.android.widgets.AlignedDividerDecoration
import com.cataloghub.android.widgets.SkeletonView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductFilterOptionListFragment :
    BaseFragment(R.layout.fragment_product_filter_option_list),
    OnLoadMoreListener,
    OnProductFilterOptionClickListener {
    private val viewModel: ProductFilterListViewModel by fixedHiltNavGraphViewModels(R.id.nav_graph_product_filters)

    private val arguments: ProductFilterOptionListFragmentArgs by navArgs()

    private lateinit var mProductFilterOptionListAdapter: ProductFilterOptionListAdapter

    private val skeletonView = SkeletonView()

    override val activityAppBarStatus: AppBarStatus
        get() = AppBarStatus.Visible(
            hasShadow = false,
            hasDivider = true
        )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentProductFilterOptionListBinding.bind(view)
        setupObservers(viewModel, binding)

        mProductFilterOptionListAdapter = ProductFilterOptionListAdapter(this, this)
        with(binding.filterOptionList) {
            addItemDecoration(
                AlignedDividerDecoration(
                    requireActivity(),
                    DividerItemDecoration.VERTICAL,
                    R.id.filterOptionItem_name,
                    clipToMargin = false
                )
            )
            layoutManager = LinearLayoutManager(activity)
            adapter = mProductFilterOptionListAdapter

            // Setting this field to false ensures that the RecyclerView children do NOT receive the multiple clicks,
            // and only processes the first click event. More details on this issue can be found here:
            // https://github.com/woocommerce/woocommerce-android/issues/2074
            isMotionEventSplittingEnabled = false
        }

        binding.filterOptionListBtnShowProducts.setOnClickListener {
            AnalyticsTracker.track(
                AnalyticsEvent.PRODUCT_FILTER_LIST_SHOW_PRODUCTS_BUTTON_TAPPED,
                mapOf(AnalyticsTracker.KEY_FILTERS to viewModel.getFilterString())
            )
            viewModel.onShowProductsClicked()
        }
    }

    private fun setupObservers(
        viewModel: ProductFilterListViewModel,
        binding: FragmentProductFilterOptionListBinding
    ) {
        viewModel.productFilterOptionListViewStateData.observe(viewLifecycleOwner) { old, new ->
            new.screenTitle.takeIfNotEqualTo(old?.screenTitle) { requireActivity().title = it }
            new.isSkeletonShown?.takeIfNotEqualTo(old?.isSkeletonShown) {
                showSkeleton(it, binding)
            }
            new.isLoadingMore?.takeIfNotEqualTo(old?.isLoadingMore) {
                showLoadMoreProgress(it, binding)
            }
        }

        viewModel.filterOptionListItems.observe(viewLifecycleOwner) {
            showProductFilterList(it)
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is MultiLiveEvent.Event.LaunchUrlInChromeTab -> {
                    ChromeCustomTabUtils.launchUrl(requireContext(), event.url)
                }
                is MultiLiveEvent.Event.ExitWithResult<*> -> {
                    navigateToParentWithResult(
                        ProductListFragment.PRODUCT_FILTER_RESULT_KEY,
                        event.data,
                        R.id.productFilterListFragment
                    )
                }
                else -> event.isHandled = false
            }
        }

        viewModel.loadFilterOptions(arguments.selectedFilterItemPosition)
    }

    private fun showSkeleton(show: Boolean, binding: FragmentProductFilterOptionListBinding) {
        if (show) {
            skeletonView.show(
                binding.filterOptionList,
                R.layout.skeleton_product_filter_options_categories_list,
                delayed = true
            )
            binding.filterOptionListBtnFrame.hide()
        } else {
            skeletonView.hide()
            binding.filterOptionListBtnFrame.show()
        }
    }

    private fun showLoadMoreProgress(show: Boolean, binding: FragmentProductFilterOptionListBinding) {
        binding.loadMoreProgress.isVisible = show
    }

    private fun showProductFilterList(productFilterOptionList: List<FilterListOptionItemUiModel>) {
        mProductFilterOptionListAdapter.updateData(productFilterOptionList)
    }

    override fun onFilterOptionClick(selectedFilter: FilterListOptionItemUiModel) {
        viewModel.onFilterOptionItemSelected(arguments.selectedFilterItemPosition, selectedFilter)
    }

    override fun onRequestLoadMore() {
        viewModel.onLoadMoreRequested(arguments.selectedFilterItemPosition)
    }

    override fun onDestroyView() {
        skeletonView.hide()
        super.onDestroyView()
    }
}
