package com.cataloghub.android.ui.ai.process

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentAiProcessBinding
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.ai.AIViewModel
import com.cataloghub.android.ui.WebViewFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AIProcessFragment : BaseFragment(R.layout.fragment_ai_process) {
    companion object {
        private const val TAG = "AIProcessFragment"
    }

    @Inject lateinit var uiMessageResolver: UIMessageResolver
    @Inject lateinit var selectedSite: SelectedSite

    private val viewModel: AIProcessViewModel by viewModels()
    private val aiViewModel: AIViewModel by activityViewModels()

    private var _binding: FragmentAiProcessBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAiProcessBinding.bind(view)

        setupProcessObservers()
        setupYouTubeObservers()

        setupProcessClickListeners()
        setupYouTubeConnectButton()

        checkYouTubeStatus()
    }

    private fun checkYouTubeStatus() {
        selectedSite.get()?.let {
            aiViewModel.checkYouTubeConnectionStatus(it.url)
        } ?: Log.w(TAG, "Cannot check YouTube status: No site selected.")
    }

    private fun setupProcessObservers() {
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

    private fun setupProcessClickListeners() {
        binding.buttonProcess.setOnClickListener {
            val url = binding.editTextUrl.text.toString()
            val autoApprove = binding.switchAutoApprove.isChecked
            viewModel.processVideo(url, autoApprove)
        }
    }

    private fun setupYouTubeObservers() {
        aiViewModel.isYouTubeConnected.observe(viewLifecycleOwner) { isConnected ->
            isConnected?.let { updateYouTubeConnectionUI(it) }
        }

        aiViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.youtubeConnectButton.isEnabled = !(isLoading ?: false)
        }

        aiViewModel.event.observe(viewLifecycleOwner) { event ->
            handleAIEvent(event)
        }

        aiViewModel.authUrl.observe(viewLifecycleOwner) { url ->
            handleAuthUrl(url)
        }

        aiViewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                uiMessageResolver.showSnack(errorMessage)
                aiViewModel.errorMessageShown()
            }
        }
    }

    private fun setupYouTubeConnectButton() {
        binding.youtubeConnectButton.setOnClickListener {
            val isConnected = aiViewModel.isYouTubeConnected.value ?: false
            if (isConnected) {
                try {
                    findNavController().navigate(R.id.youTubeVideosFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "Navigation to YouTube Videos failed", e)
                    uiMessageResolver.showSnack("Could not navigate to YouTube videos.")
                }
            } else {
                selectedSite.get()?.let {
                    aiViewModel.connectYouTube()
                } ?: uiMessageResolver.showSnack("Please select a store first.")
            }
        }
    }

    private fun updateYouTubeConnectionUI(isConnected: Boolean) {
        if (isConnected) {
            binding.youtubeConnectButton.text = "View Videos"
            binding.youtubeSubtitle.text = "YouTube connected"
        } else {
            binding.youtubeConnectButton.text = "Connect"
            binding.youtubeSubtitle.text = "Connect YouTube to generate products from videos"
        }
    }

    private fun handleAIEvent(event: AIViewModel.Event?) {
        when (event) {
            is AIViewModel.Event.ShowSnackbar -> {
                uiMessageResolver.showSnack(event.message)
            }
            else -> {}
        }
    }

    private fun handleAuthUrl(url: String?) {
        if (!url.isNullOrBlank()) {
            Log.d(TAG, "Received YouTube auth URL: $url")
            try {
                val directions = WebViewFragmentDirections.actionGlobalWebViewFragment(url)
                findNavController().navigate(directions)
                aiViewModel.authUrlOpened()
            } catch (e: Exception) {
                Log.e(TAG, "Navigation to WebView failed", e)
                uiMessageResolver.showSnack("Error opening authentication page.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
