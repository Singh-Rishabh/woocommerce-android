package com.cataloghub.android.ui.ai

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentAiBinding
import com.cataloghub.android.ui.base.TopLevelFragment
import com.cataloghub.android.tools.SelectedSite
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AIFragment : TopLevelFragment(R.layout.fragment_ai) {
    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AIViewModel by viewModels()

    @Inject
    lateinit var selectedSite: SelectedSite

    override fun getFragmentTitle() = getString(R.string.ai_screen_title)

    override fun shouldExpandToolbar(): Boolean = false

    override fun scrollToTop() {
        // No scrolling needed for this fragment
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AINetworkLogger.logNavigation("Main", "AI Fragment")
        _binding = FragmentAiBinding.bind(view)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.buttonProcess.setOnClickListener {
            AINetworkLogger.logNavigation("AI Fragment", "Process")
            findNavController().navigate(R.id.action_ai_to_process)
        }

        binding.buttonReview.setOnClickListener {
            AINetworkLogger.logNavigation("AI Fragment", "Review")
            findNavController().navigate(R.id.action_ai_to_review)
        }
    }

    override fun onResume() {
        super.onResume()
        AINetworkLogger.logNavigation("Background", "AI Fragment")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AINetworkLogger.logNavigation("AI Fragment", "Destroyed")
        _binding = null
    }
}
