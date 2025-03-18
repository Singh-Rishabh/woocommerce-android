package com.cataloghub.android.ui.ai

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentAiBinding
import com.cataloghub.android.ui.base.TopLevelFragment
import com.cataloghub.android.ui.base.UIMessageResolver
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

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

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
        setupObservers()
        
        // Check if YouTube is already connected
        viewModel.checkYouTubeConnectionStatus(selectedSite.get().url)
    }

    private fun setupClickListeners() {
        binding.youtubeCard.setOnClickListener {
            if (viewModel.isYouTubeConnected.value == true) {
                // Navigate to YouTube videos list
                findNavController().navigate(R.id.action_ai_to_youtube_videos)
            } else {
                // Start YouTube connection flow
                connectYouTube()
            }
        }

        binding.youtubeConnectButton.setOnClickListener {
            if (viewModel.isYouTubeConnected.value == true) {
                // Navigate to YouTube videos list
                findNavController().navigate(R.id.action_ai_to_youtube_videos)
            } else {
                // Start YouTube connection flow
                connectYouTube()
            }
        }

        // Facebook and Instagram are disabled (coming soon)
    }

    private fun setupObservers() {
        viewModel.isYouTubeConnected.observe(viewLifecycleOwner) { isConnected ->
            updateYouTubeConnectionUI(isConnected)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.youtubeConnectButton.isEnabled = !isLoading
            if (isLoading) {
                binding.youtubeConnectButton.text = getString(R.string.loading)
            } else {
                updateYouTubeConnectionUI(viewModel.isYouTubeConnected.value ?: false)
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                uiMessageResolver.showSnack(it)
                viewModel.errorMessageShown()
            }
        }

        viewModel.authUrl.observe(viewLifecycleOwner) { url ->
            url?.let {
                // Open the OAuth URL in a browser
                openOAuthUrl(it)
                viewModel.authUrlOpened()
            }
        }
    }

    private fun updateYouTubeConnectionUI(isConnected: Boolean) {
        if (isConnected) {
            binding.youtubeConnectButton.text = getString(R.string.view_videos)
            binding.youtubeSubtitle.text = getString(R.string.youtube_connected)
        } else {
            binding.youtubeConnectButton.text = getString(R.string.connect)
            binding.youtubeSubtitle.text = getString(R.string.youtube_not_connected)
        }
    }

    private fun connectYouTube() {
        AINetworkLogger.logNavigation("AI Fragment", "YouTube Connect")
        viewModel.getYouTubeAuthUrl(selectedSite.get().url)
    }

    private fun openOAuthUrl(url: String) {
        // Navigate to a WebView fragment or use an external browser
        val action = AIFragmentDirections.actionAiToWebView(url)
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        AINetworkLogger.logNavigation("Background", "AI Fragment")
        
        // Check connection status on resume (in case user completed OAuth flow)
        viewModel.checkYouTubeConnectionStatus(selectedSite.get().url)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AINetworkLogger.logNavigation("AI Fragment", "Destroyed")
        _binding = null
    }
}
