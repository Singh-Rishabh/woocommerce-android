package com.cataloghub.android.ui.ai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentAiBinding
import com.cataloghub.android.ui.base.TopLevelFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.ai.youtube.YouTubeAuthManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AIFragment : TopLevelFragment(R.layout.fragment_ai) {
    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AIViewModel by viewModels()
    private lateinit var youTubeAuthManager: YouTubeAuthManager
    
    // Activity result launcher for YouTube authentication
    private lateinit var youTubeAuthLauncher: ActivityResultLauncher<Intent>

    @Inject
    lateinit var selectedSite: SelectedSite

    @Inject
    lateinit var uiMessageResolver: UIMessageResolver

    override fun getFragmentTitle() = getString(R.string.ai_screen_title)

    override fun shouldExpandToolbar(): Boolean = false

    override fun scrollToTop() {
        // No scrolling needed for this fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register the activity result launcher for YouTube auth
        youTubeAuthLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            // Let the YouTubeAuthManager handle the auth result
            if (youTubeAuthManager.handleAuthResult(
                    result.resultCode,
                    result.data,
                    viewModel
                )) {
                // Auth was handled successfully
                Log.d("OAuth", "YouTube auth completed successfully")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AINetworkLogger.logNavigation("Main", "AI Fragment")
        _binding = FragmentAiBinding.bind(view)
        
        // Initialize the YouTube Auth Manager
        youTubeAuthManager = YouTubeAuthManager(requireContext(), youTubeAuthLauncher)
        
        setupClickListeners()
        setupObservers()
        
        // Debug: Long press on YouTube card to test OAuth functionality
        binding.youtubeCard.setOnLongClickListener {
            // Test opening a simple URL in Custom Tabs
            com.cataloghub.android.util.OAuthDebugHelper.testCustomTabs(requireContext())
            true
        }
        
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
        
        // Observe events for snackbar messages and navigation
        viewModel.event.observe(viewLifecycleOwner) { event ->
            when (event) {
                is AIViewModel.Event.ShowSnackbar -> {
                    uiMessageResolver.showSnack(event.message)
                }
                is AIViewModel.Event.NavigateToWebView -> {
                    // Handle web view navigation if needed
                }
                is AIViewModel.Event.NavigateToVideoDetail -> {
                    // Handle video detail navigation if needed
                }
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
        Log.d("OAuth", "Starting YouTube connection flow")
        
        // Use the YouTubeAuthManager to start the authorization process
        youTubeAuthManager.beginAuthorization(requireActivity())
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
