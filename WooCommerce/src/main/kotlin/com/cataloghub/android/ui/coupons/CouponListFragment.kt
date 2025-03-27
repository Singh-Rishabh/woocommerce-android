package com.cataloghub.android.ui.coupons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView.OnQueryTextListener
import androidx.appcompat.widget.SearchView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle.State
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.FeedbackPrefs
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentCouponListBinding
import com.cataloghub.android.extensions.navigateSafely
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.ui.compose.theme.WooThemeWithBackground
import com.cataloghub.android.ui.coupons.CouponListViewModel.NavigateToCouponDetailsEvent
import com.cataloghub.android.ui.coupons.CouponListViewModel.NavigateToCouponTypePicker
import com.cataloghub.android.viewmodel.MultiLiveEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CouponListFragment : BaseFragment(R.layout.fragment_coupon_list) {
    companion object {
        const val TAG: String = "CouponListFragment"
    }

    @Inject lateinit var uiMessageResolver: UIMessageResolver

    @Inject lateinit var feedbackPrefs: FeedbackPrefs

    private lateinit var searchMenuItem: MenuItem
    private lateinit var searchView: SearchView
    private var _binding: FragmentCouponListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CouponListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCouponListBinding.inflate(inflater, container, false)

        val view = binding.root
        binding.couponsComposeView.apply {
            // Dispose of the Composition when the view's LifecycleOwner is destroyed
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                WooThemeWithBackground {
                    CouponListScreen(viewModel)
                }
            }
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()
        setupObservers()
        setupListeners()
    }

    private fun setupListeners() {
        binding.addCouponButton.setOnClickListener { viewModel.onAddCouponClicked() }
    }

    private fun setupObservers() {
        viewModel.couponsState.observe(viewLifecycleOwner) { state ->
            if (::searchMenuItem.isInitialized && state.isSearchOpen != searchMenuItem.isActionViewExpanded) {
                if (state.isSearchOpen) searchMenuItem.expandActionView() else searchMenuItem.collapseActionView()
            }
            if (::searchView.isInitialized && state.isSearchOpen && state.searchQuery != searchView.query?.toString()) {
                searchView.setQuery(state.searchQuery, false)
            }
        }
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is NavigateToCouponDetailsEvent -> navigateToCouponDetails(event.couponId)
                is NavigateToCouponTypePicker -> openCouponTypePicker()
                is MultiLiveEvent.Event.ShowSnackbar -> uiMessageResolver.showSnack(event.message)
            }
        }
    }

    private fun setupMenu() {
        requireActivity().addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.menu_search, menu)
                    searchMenuItem = menu.findItem(R.id.menu_search)
                    initSearch()
                }

                override fun onMenuItemSelected(item: MenuItem): Boolean {
                    return false
                }
            },
            viewLifecycleOwner,
            State.RESUMED
        )
    }

    override fun getFragmentTitle(): String = getString(R.string.coupons)

    private fun initSearch() {
        searchView = searchMenuItem.actionView as SearchView
        searchView.queryHint = getString(R.string.coupons_list_search_hint)
        viewModel.couponsState.value?.let {
            if (it.isSearchOpen) {
                searchMenuItem.expandActionView()
                searchView.setQuery(it.searchQuery, false)
            } else {
                searchMenuItem.collapseActionView()
            }
        }
        val textQueryListener = object : OnQueryTextListener, SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (isAdded) {
                    viewModel.onSearchQueryChanged(query.orEmpty())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (isAdded) {
                    viewModel.onSearchQueryChanged(newText.orEmpty())
                }
                return true
            }
        }
        searchMenuItem.setOnActionExpandListener(object : OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                if (isAdded) {
                    viewModel.onSearchStateChanged(open = true)
                    searchView.setOnQueryTextListener(textQueryListener)
                }
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                if (isAdded) {
                    searchView.setOnQueryTextListener(null)
                    viewModel.onSearchStateChanged(open = false)
                }
                return true
            }
        })
    }

    private fun navigateToCouponDetails(couponId: Long) {
        findNavController().navigateSafely(
            CouponListFragmentDirections.actionCouponListFragmentToCouponDetailsFragment(couponId)
        )
    }

    private fun openCouponTypePicker() {
        findNavController().navigateSafely(
            CouponListFragmentDirections.actionCouponListFragmentToCouponTypePickerFragment()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
