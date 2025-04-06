package com.cataloghub.android.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.R
import com.cataloghub.android.analytics.AnalyticsEvent
import com.cataloghub.android.analytics.AnalyticsTracker
import com.cataloghub.android.databinding.FragmentCategoriesListBinding
import com.cataloghub.android.extensions.pinFabAboveBottomNavigationBar
import com.cataloghub.android.model.UiState
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.main.AppBarStatus
import com.cataloghub.android.ui.main.BottomNavigationPosition
import com.cataloghub.android.ui.main.MainActivity
import com.cataloghub.android.ui.main.MainNavigationRouter
import com.cataloghub.android.ui.products.categories.ProductCategoryItemUiModel
import com.cataloghub.android.util.ChromeCustomTabUtils
import com.cataloghub.android.util.WooLog
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CategoriesListFragment : BaseFragment() {
    companion object {
        private const val STATE_ACTIVE_FILTER_SORT = "active-filter-sort"
        private const val STATE_SEARCH_QUERY = "search-query"
        private const val SEARCH_DEBOUNCE_MS = 300L
        
        // Static cache for categories to improve performance
        private var cachedCategories: List<ProductCategoryItemUiModel>? = null
    }

    // Ensure the app bar and bottom navigation remain visible
    override val activityAppBarStatus: AppBarStatus = AppBarStatus.Visible()

    @Inject lateinit var uiMessageResolver: UIMessageResolver

    private val viewModel: CategoriesListViewModel by viewModels()
    private var _binding: FragmentCategoriesListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var categoriesAdapter: CategoriesListAdapter
    private var searchJob: Job? = null
    private val searchQueryFlow = MutableStateFlow("")
    private var searchQuery: String = ""
    private var activeFilterSort: CategoriesListViewModel.SortBy = CategoriesListViewModel.SortBy.ALPHABETICAL_ASC

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if (savedInstanceState != null) {
            savedInstanceState.getString(STATE_SEARCH_QUERY)?.let { searchQuery = it }
            activeFilterSort = savedInstanceState.getSerializable(STATE_ACTIVE_FILTER_SORT) as? CategoriesListViewModel.SortBy
                ?: CategoriesListViewModel.SortBy.ALPHABETICAL_ASC
            viewModel.sortCategories(activeFilterSort)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun getFragmentTitle() = getString(R.string.categories)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // The bottom nav visibility is now handled by the ViewModel and observer
            
            setupObservers()
            setupRecyclerView()
            setupFilters()
            setupSearchListener()
            setupRefresh()
            setupChips()
            setupAddCategoryButton()

            // Load categories - use cache if available
            cachedCategories?.let {
                categoriesAdapter.setCategories(it)
                binding.progressBar.isVisible = false
                binding.categoriesList.isVisible = true
                binding.emptyView.isVisible = false
                binding.errorView.isVisible = false
            } ?: run {
                // Load the categories
                viewModel.loadCategories()
            }
        } catch (e: Exception) {
            WooLog.e(WooLog.T.PRODUCTS, "Error in Categories onViewCreated: ${e.message}", e)
            uiMessageResolver.showSnack(getString(R.string.error_generic))
        }
    }

    override fun onResume() {
        super.onResume()
        
        try {
            // Always ensure Categories tab is selected in the bottom navigation
            (activity as? MainActivity)?.let { mainActivity ->
                mainActivity.setCurrentNavigationPosition(BottomNavigationPosition.CATEGORIES)
            }
        } catch (e: Exception) {
            WooLog.e(WooLog.T.PRODUCTS, "Error in Categories onResume: ${e.message}", e)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_SEARCH_QUERY, searchQuery)
        outState.putSerializable(STATE_ACTIVE_FILTER_SORT, activeFilterSort)
        super.onSaveInstanceState(outState)
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchListener() {
        searchQueryFlow
            .debounce(SEARCH_DEBOUNCE_MS)
            .onEach { query ->
                if (isAdded) {
                    viewModel.searchCategories(query)
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupChips() {
        binding.chipGroupSortBy.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val sortBy = when (checkedIds[0]) {
                    R.id.chipSortByAZ -> CategoriesListViewModel.SortBy.ALPHABETICAL_ASC
                    R.id.chipSortByZA -> CategoriesListViewModel.SortBy.ALPHABETICAL_DESC
                    R.id.chipSortByNewest -> CategoriesListViewModel.SortBy.DATE_DESC
                    R.id.chipSortByOldest -> CategoriesListViewModel.SortBy.DATE_ASC
                    else -> CategoriesListViewModel.SortBy.ALPHABETICAL_ASC
                }
                activeFilterSort = sortBy
                viewModel.sortCategories(sortBy)
                refreshSortByUI(sortBy)
            }
        }

        refreshSortByUI(activeFilterSort)
    }

    private fun refreshSortByUI(sortBy: CategoriesListViewModel.SortBy) {
        val chipId = when (sortBy) {
            CategoriesListViewModel.SortBy.ALPHABETICAL_ASC -> R.id.chipSortByAZ
            CategoriesListViewModel.SortBy.ALPHABETICAL_DESC -> R.id.chipSortByZA
            CategoriesListViewModel.SortBy.DATE_DESC -> R.id.chipSortByNewest
            CategoriesListViewModel.SortBy.DATE_ASC -> R.id.chipSortByOldest
        }
        binding.chipGroupSortBy.check(chipId)
    }

    private fun setupRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            // Clear cache to force complete refresh
            cachedCategories = null
            viewModel.loadCategories(forceRefresh = true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_search, menu)

        val searchMenuItem = menu.findItem(R.id.menu_search)
        val searchView = searchMenuItem.actionView as SearchView
        searchView.queryHint = getString(R.string.search)

        searchMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        viewModel.searchCategories(query)
                        searchQuery = query
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {
                        searchQuery = newText
                        searchJob?.cancel()
                        searchJob = lifecycleScope.launch {
                            delay(SEARCH_DEBOUNCE_MS)
                            searchQueryFlow.value = newText
                        }
                        return true
                    }
                })
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                searchView.setOnQueryTextListener(null)
                searchQuery = ""
                viewModel.searchCategories("")
                return true
            }
        })

        if (searchQuery.isNotEmpty()) {
            searchMenuItem.expandActionView()
            searchView.setQuery(searchQuery, false)
            searchView.clearFocus()
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun setupObservers() {
        viewModel.categoriesViewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is UiState.Content -> {
                    binding.swipeRefresh.isRefreshing = false
                    // Save to cache
                    cachedCategories = state.data
                    showCategories(state.data)
                }
                is UiState.Empty -> {
                    binding.swipeRefresh.isRefreshing = false
                    cachedCategories = emptyList()
                    showEmptyView()
                }
                is UiState.Error -> {
                    binding.swipeRefresh.isRefreshing = false
                    showErrorView(state.message)
                }
                is UiState.Loading -> {
                    // Only show loading if cache is empty
                    if (cachedCategories == null) {
                        showLoadingView()
                    }
                }
                else -> {
                    WooLog.d(WooLog.T.PRODUCTS, "Categories view state: $state")
                }
            }
        }
        
        viewModel.isBottomNavBarVisible.observe(viewLifecycleOwner) { isVisible ->
            showBottomNavBar(isVisible)
        }
    }

    private fun showLoadingView() {
        if (!binding.swipeRefresh.isRefreshing) {
            binding.emptyView.isVisible = false
            binding.categoriesList.isVisible = false
            binding.progressBar.isVisible = true
        }
        binding.errorView.isVisible = false
    }

    private fun showCategories(categories: List<ProductCategoryItemUiModel>) {
        binding.emptyView.isVisible = false
        binding.errorView.isVisible = false
        binding.progressBar.isVisible = false

        binding.categoriesList.isVisible = true
        categoriesAdapter.setCategories(categories)
    }

    private fun showEmptyView() {
        binding.progressBar.isVisible = false
        binding.categoriesList.isVisible = false
        binding.errorView.isVisible = false
        binding.emptyView.isVisible = true
    }

    private fun showErrorView(message: String) {
        binding.progressBar.isVisible = false
        binding.categoriesList.isVisible = false
        binding.emptyView.isVisible = false
        binding.errorView.isVisible = true
        binding.errorView.text = message
    }

    private fun showBottomNavBar(isVisible: Boolean) {
        try {
            if (isVisible) {
                (activity as? MainActivity)?.showBottomNav()
            } else {
                (activity as? MainActivity)?.hideBottomNav()
            }
        } catch (e: Exception) {
            WooLog.e(WooLog.T.PRODUCTS, "Error showing/hiding bottom nav bar: ${e.message}", e)
        }
    }

    private fun setupRecyclerView() {
        try {
            WooLog.d(WooLog.T.PRODUCTS, "Setting up Categories RecyclerView")
            categoriesAdapter = CategoriesListAdapter { categoryId, categoryName ->
                // Navigate to ProductListFragment with category filter
                navigateToProductsForCategory(categoryId, categoryName)
                // Track analytics
                AnalyticsTracker.track(AnalyticsEvent.CATEGORY_TAPPED)
            }

            binding.categoriesList.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = categoriesAdapter
                addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
            }
            WooLog.d(WooLog.T.PRODUCTS, "Categories RecyclerView setup complete")
        } catch (e: Exception) {
            WooLog.e(WooLog.T.PRODUCTS, "Error setting up Categories RecyclerView: ${e.message}", e)
        }
    }

    /**
     * Navigate to the Products list screen filtered by the selected category
     */
    private fun navigateToProductsForCategory(categoryId: Long, categoryName: String) {
        try {
            WooLog.d(WooLog.T.PRODUCTS, "Navigating to products for category: $categoryName (ID: $categoryId)")
            
            // Use direct navigation to avoid NavGraphMainDirections dependency
            val bundle = Bundle().apply {
                putString("categoryId", categoryId.toString())
                putString("categoryName", categoryName)
            }
            findNavController().navigate(R.id.action_global_productsFragment, bundle)
        } catch (e: Exception) {
            WooLog.e(WooLog.T.PRODUCTS, "Error navigating to products for category: ${e.message}", e)
            uiMessageResolver.showSnack(getString(R.string.error_generic))
        }
    }

    private fun setupFilters() {
        binding.sortAndFilterCard.setOnClickListener {
            binding.filtersContainer.isVisible = !binding.filtersContainer.isVisible
        }
    }

    private fun setupAddCategoryButton() {
        // Position the FAB above the bottom navigation bar
        pinFabAboveBottomNavigationBar(binding.addCategoryButton)
        
        binding.addCategoryButton.setOnClickListener {
            try {
                // Navigate to AI Process screen to process YouTube video
                findNavController().navigate(R.id.action_categories_to_aiProcessFragment)
                
                // Track analytics event
                AnalyticsTracker.track(
                    AnalyticsEvent.CATEGORY_ADD_BUTTON_TAPPED,
                    mapOf("source" to "youtube")
                )
            } catch (e: Exception) {
                WooLog.e(WooLog.T.PRODUCTS, "Error navigating to AI Process: ${e.message}", e)
                uiMessageResolver.showSnack(getString(R.string.error_generic))
            }
        }
    }

    override fun onDestroyView() {
        // Clear all observers to prevent memory leaks
        viewModel.categoriesViewState.removeObservers(viewLifecycleOwner)
        viewModel.isBottomNavBarVisible.removeObservers(viewLifecycleOwner)
        
        // Clear adapter and RecyclerView references to prevent memory leaks
        if (::categoriesAdapter.isInitialized && _binding != null) {
            // First clear the adapter data
            categoriesAdapter.cleanup()
            
            // Then remove the adapter from the RecyclerView
            binding.categoriesList.adapter = null
            
            // Remove any item decorations
            val decorCount = binding.categoriesList.itemDecorationCount
            for (i in 0 until decorCount) {
                binding.categoriesList.removeItemDecorationAt(0)
            }
            
            // Clear any listeners
            binding.categoriesList.clearOnScrollListeners()
            binding.swipeRefresh.setOnRefreshListener(null)
            binding.sortAndFilterCard.setOnClickListener(null)
            binding.chipGroupSortBy.setOnCheckedStateChangeListener(null)
        }
        
        // Cancel any ongoing jobs
        searchJob?.cancel()
        searchJob = null
        
        // Clear binding reference
        _binding = null
        
        super.onDestroyView()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Clear any references in fragment lifecycle callbacks
        if (!requireActivity().isChangingConfigurations) {
            // Clear category data if app is fully closing the fragment
            // but keep it if just rotating/changing config
            viewModel.onCleanup()
        }
    }
} 