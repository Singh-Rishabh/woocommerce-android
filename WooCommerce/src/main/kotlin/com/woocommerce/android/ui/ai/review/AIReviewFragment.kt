package com.woocommerce.android.ui.ai.review

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.woocommerce.android.R
import com.woocommerce.android.databinding.FragmentAiReviewBinding
import com.woocommerce.android.ui.base.BaseFragment
import com.woocommerce.android.ui.base.UIMessageResolver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AIReviewFragment : BaseFragment(R.layout.fragment_ai_review) {
    @Inject lateinit var uiMessageResolver: UIMessageResolver
    
    private val viewModel: AIReviewViewModel by viewModels()
    private var _binding: FragmentAiReviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var productsAdapter: ProductsReviewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAiReviewBinding.bind(view)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        productsAdapter = ProductsReviewAdapter(
            onApproveClick = { product -> viewModel.approveProduct(product) },
            onRejectClick = { product -> viewModel.rejectProduct(product) },
            onEditClick = { product -> viewModel.editProduct(product) }
        )
        
        binding.recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = productsAdapter
        }
    }

    private fun setupObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.buttonSubmit.isEnabled = !state.isLoading && state.hasChanges
            
            if (state.products.isEmpty() && !state.isLoading) {
                binding.textNoProducts.visibility = View.VISIBLE
                binding.recyclerViewProducts.visibility = View.GONE
            } else {
                binding.textNoProducts.visibility = View.GONE
                binding.recyclerViewProducts.visibility = View.VISIBLE
                productsAdapter.submitList(state.products)
            }
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AIReviewViewModel.Event.ShowError -> {
                    uiMessageResolver.showSnack(event.message)
                }
                is AIReviewViewModel.Event.ShowSuccess -> {
                    uiMessageResolver.showSnack(event.message)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonSubmit.setOnClickListener {
            viewModel.submitChanges()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 