package com.cataloghub.android.ui.searchfilter

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentSearchFilterBinding
import com.cataloghub.android.extensions.hide
import com.cataloghub.android.extensions.navigateBackWithResult
import com.cataloghub.android.extensions.show
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.searchfilter.SearchFilterEvent.ItemSelected
import com.cataloghub.android.ui.searchfilter.SearchFilterViewState.Empty
import com.cataloghub.android.ui.searchfilter.SearchFilterViewState.Loaded
import com.cataloghub.android.ui.searchfilter.SearchFilterViewState.Search
import com.cataloghub.android.widgets.AlignedDividerDecoration
import com.cataloghub.android.widgets.WCEmptyView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFilterFragment : BaseFragment(R.layout.fragment_search_filter) {
    companion object {
        val TAG: String = SearchFilterFragment::class.java.simpleName
    }

    private val viewModel: SearchFilterViewModel by viewModels()

    private val navArgs: SearchFilterFragmentArgs by navArgs()

    private var _binding: FragmentSearchFilterBinding? = null
    private val binding: FragmentSearchFilterBinding
        get() = _binding!!

    private lateinit var searchFilterAdapter: SearchFilterAdapter

    private val searchTextWatcher = object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            // noop
        }

        override fun beforeTextChanged(charSequence: CharSequence?, start: Int, count: Int, after: Int) {
            // noop
        }

        override fun onTextChanged(charSequence: CharSequence?, start: Int, before: Int, count: Int) {
            viewModel.onSearch(charSequence.toString())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSearchFilterBinding.bind(view)
        viewModel.start(
            searchFilterItems = navArgs.items,
            searchHint = navArgs.hint,
            requestKey = navArgs.requestKey
        )
        setupViewStateObserver()
        setupEventObserver()
        setupSearchInput()
        setupSearchList()
    }

    override fun onDestroyView() {
        binding.searchEditText.removeTextChangedListener(searchTextWatcher)
        super.onDestroyView()
        _binding = null
    }

    override fun getFragmentTitle(): String = navArgs.title

    private fun setupViewStateObserver() {
        viewModel.viewStateLiveData.observe(
            viewLifecycleOwner,
            { viewState ->
                when (viewState) {
                    is Loaded -> {
                        showSearchList()
                        updateSearchList(viewState.searchFilterItems)
                        binding.searchEditText.hint = viewState.searchHint
                    }
                    is Search -> {
                        showSearchList()
                        updateSearchList(viewState.searchFilterItems)
                        hideEmptyView()
                    }
                    is Empty -> {
                        hideSearchList()
                        showEmptyView(viewState.searchQuery)
                    }
                }
            }
        )
    }

    private fun setupEventObserver() {
        viewModel.eventLiveData.observe(
            viewLifecycleOwner,
            { event ->
                when (event) {
                    is ItemSelected -> {
                        navigateBackWithResult(event.requestKey, event.selectedItemValue)
                    }
                }
            }
        )
    }

    private fun setupSearchInput() {
        binding.searchEditText.apply {
            addTextChangedListener(searchTextWatcher)
        }
    }

    private fun setupSearchList() {
        binding.searchItemsList.apply {
            layoutManager = LinearLayoutManager(context)
            searchFilterAdapter = SearchFilterAdapter(
                onItemSelectedListener = { selectedItem ->
                    viewModel.onItemSelected(selectedItem)
                }
            )
            adapter = searchFilterAdapter
            addItemDecoration(
                AlignedDividerDecoration(
                    ctx = context,
                    orientation = DividerItemDecoration.VERTICAL,
                    alignStartToStartOf = R.id.filterItemName
                )
            )
        }
    }

    private fun updateSearchList(searchFilterItems: List<SearchFilterItem>) {
        searchFilterAdapter.submitList(searchFilterItems)
    }

    private fun showSearchList() {
        binding.searchItemsList.show()
    }

    private fun hideSearchList() {
        binding.searchItemsList.hide()
    }

    private fun hideEmptyView() {
        binding.emptyView.hide()
    }

    private fun showEmptyView(searchQuery: String) {
        binding.emptyView.show(
            WCEmptyView.EmptyViewType.SEARCH_RESULTS,
            searchQueryOrFilter = searchQuery
        )
    }
}
