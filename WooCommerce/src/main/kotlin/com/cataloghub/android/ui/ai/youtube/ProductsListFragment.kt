package com.cataloghub.android.ui.ai.youtube

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentProductsListBinding
import com.cataloghub.android.model.AIProduct
import com.cataloghub.android.model.AIProductStatus
import com.cataloghub.android.ui.base.UIMessageResolver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProductsListFragment : Fragment(R.layout.fragment_products_list) {
    
    @Inject lateinit var uiMessageResolver: UIMessageResolver
    
    private val viewModel: VideoDetailViewModel by viewModels({ requireParentFragment() })
    private var _binding: FragmentProductsListBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var productsAdapter: ProductsReviewAdapter
    private lateinit var status: AIProductStatus
    
    companion object {
        private const val ARG_STATUS = "status"
        
        fun newInstance(status: AIProductStatus): ProductsListFragment {
            return ProductsListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_STATUS, status.name)
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_STATUS)?.let {
            status = AIProductStatus.valueOf(it)
        } ?: run {
            status = AIProductStatus.PENDING
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductsListBinding.bind(view)
        
        setupRecyclerView()
        setupObservers()
    }
    
    private fun setupRecyclerView() {
        productsAdapter = ProductsReviewAdapter(
            onApproveClick = { product -> approveProduct(product) },
            onRejectClick = { product -> rejectProduct(product) },
            onEditClick = { product -> editProduct(product) }
        )
        
        binding.recyclerViewProducts.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = productsAdapter
        }
    }
    
    private fun setupObservers() {
        when (status) {
            AIProductStatus.PENDING -> {
                viewModel.pendingProducts.observe(viewLifecycleOwner) { products ->
                    updateProductsList(products)
                }
            }
            AIProductStatus.APPROVED -> {
                viewModel.approvedProducts.observe(viewLifecycleOwner) { products ->
                    updateProductsList(products)
                }
            }
            AIProductStatus.REJECTED -> {
                viewModel.rejectedProducts.observe(viewLifecycleOwner) { products ->
                    updateProductsList(products)
                }
            }
        }
    }
    
    private fun updateProductsList(products: List<AIProduct>) {
        productsAdapter.submitList(products)
        
        // Show empty view if no products
        binding.emptyView.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        binding.recyclerViewProducts.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
    }
    
    private fun approveProduct(product: AIProduct) {
        // Implement product approval
        uiMessageResolver.showSnack(R.string.product_approved)
        viewModel.loadProducts()
    }
    
    private fun rejectProduct(product: AIProduct) {
        // Implement product rejection
        uiMessageResolver.showSnack(R.string.product_rejected)
        viewModel.loadProducts()
    }
    
    private fun editProduct(product: AIProduct) {
        // Implement product editing
        uiMessageResolver.showSnack(R.string.product_edit_not_implemented)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 