package com.woocommerce.android.ui.ai.process

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.woocommerce.android.R
import com.woocommerce.android.databinding.FragmentAiProcessBinding
import com.woocommerce.android.ui.base.BaseFragment
import com.woocommerce.android.ui.base.UIMessageResolver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AIProcessFragment : BaseFragment(R.layout.fragment_ai_process) {
    @Inject lateinit var uiMessageResolver: UIMessageResolver
    
    private val viewModel: AIProcessViewModel by viewModels()
    private var _binding: FragmentAiProcessBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAiProcessBinding.bind(view)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewModel.viewState.observe(viewLifecycleOwner) { state ->
            binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
            binding.buttonProcess.isEnabled = !state.isLoading
            binding.switchAutoApprove.isEnabled = !state.isLoading
            binding.editTextUrl.isEnabled = !state.isLoading
        }

        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AIProcessViewModel.Event.ShowError -> {
                    uiMessageResolver.showSnack(event.message)
                }
                is AIProcessViewModel.Event.NavigateToReview -> {
                    findNavController().navigate(R.id.action_ai_to_review)
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.buttonProcess.setOnClickListener {
            val url = binding.editTextUrl.text.toString()
            val autoApprove = binding.switchAutoApprove.isChecked
            viewModel.processVideo(url, autoApprove)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 