package com.cataloghub.android.ui.ai

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentAiBinding
import com.cataloghub.android.ui.WebViewFragmentDirections
import com.cataloghub.android.ui.base.TopLevelFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import com.cataloghub.android.tools.SelectedSite
import com.cataloghub.android.ui.ai.youtube.YouTubeAuthManager
import com.cataloghub.android.extensions.pinFabAboveBottomNavigationBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AIFragment : TopLevelFragment(R.layout.fragment_ai) {
    private var _binding: FragmentAiBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AIViewModel by viewModels()
    private lateinit var youTubeAuthManager: YouTubeAuthManager
    
    private val TAG = "AIFragment"
    
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
            Log.d(TAG, "Received auth result: $result")
            val data = result.data
            
            if (result.resultCode != Activity.RESULT_OK) {
                Log.e(TAG, "Google Sign-In failed with result code: ${result.resultCode}")
                // Show error to user
                uiMessageResolver.showSnack("YouTube connection failed. Please try again later.")
                
                // Try to get error information from the intent
                if (data != null && data.hasExtra("googleSignInStatus")) {
                    val statusCode = data.getIntExtra("googleSignInStatus", 0)
                    Log.e(TAG, "Google Sign-In error status code: $statusCode")
                }
                return@registerForActivityResult
            }
            
            youTubeAuthManager.handleAuthResult(result.resultCode, data) { authCode ->
                if (authCode != null) {
                    Log.d(TAG, "Got auth code, saving token")
                    selectedSite.get()?.let { site ->
                        viewModel.saveYouTubeToken(authCode, site.url)
                    }
                } else {
                    Log.e(TAG, "Failed to get auth code")
                    uiMessageResolver.showSnack(R.string.error_youtube_connection_failed)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        AINetworkLogger.logNavigation("Main", "AI Fragment")
        _binding = FragmentAiBinding.bind(view)
        
        // Initialize the YouTube Auth Manager
        youTubeAuthManager = YouTubeAuthManager(requireContext(), youTubeAuthLauncher)
        
        // Set the current site URL in the view model
        selectedSite.get()?.let { site ->
            viewModel.setStoreUrl(site.url)
        }
        
        setupClickListeners()
        setupObservers()
        
        // Position the FAB above the bottom navigation bar
        binding.addCategoryButton?.let { fabButton ->
            pinFabAboveBottomNavigationBar(fabButton)
        }
        
        // Check if YouTube is already connected
        selectedSite.get()?.let { site ->
            viewModel.checkYouTubeConnectionStatus(site.url)
        }
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
        
        // Set up the add category button
        binding.addCategoryButton.setOnClickListener {
            // Navigate to the process fragment to handle YouTube URL input
            findNavController().navigate(R.id.action_ai_to_process)
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
                    // Navigate to WebView fragment inside the app
                    navigateToInAppBrowser(event.url)
                }
                is AIViewModel.Event.NavigateToVideoDetail -> {
                    // Handle video detail navigation if needed
                }
            }
        }
        
        // Observe auth URL for web-based OAuth flow
        viewModel.authUrl.observe(viewLifecycleOwner) { url ->
            url?.let {
                Log.d(TAG, "Received auth URL: $url")
                // Navigate to the WebView fragment with the auth URL
                navigateToInAppBrowser(url)
                // Mark URL as opened to prevent repeated navigation
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

    /**
     * Connect to YouTube - Using direct WebView approach
     */
    private fun connectYouTube() {
        Log.d(TAG, "Starting YouTube connection flow")
        
        if (selectedSite.get() == null) {
            uiMessageResolver.showSnack(R.string.ai_error_no_site_selected)
            return
        }
        
        // Use direct WebView flow
        val storeUrl = selectedSite.get().url
        Log.d(TAG, "Starting YouTube auth via WebView for store: $storeUrl")
        
        // Show a loading state
        binding.youtubeConnectButton.isEnabled = false
        viewModel.setLoading(true)
        
        // Instead of calling the suspend function directly, use the event system
        viewModel.connectYouTube()
    }

    /**
     * Navigate to in-app browser with the given URL
     */
    private fun navigateToInAppBrowser(url: String) {
        Log.d(TAG, "Navigating to WebView with OAuth URL")
        
        try {
            // Use the navigation component to navigate to the WebView fragment
            val directions = WebViewFragmentDirections.actionGlobalWebViewFragment(url)
            findNavController().navigate(directions)
            
            // Reset loading state
            binding.youtubeConnectButton.isEnabled = true
            viewModel.setLoading(false)
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to WebView: ${e.message}", e)
            uiMessageResolver.showSnack("Error opening authentication page. Please try again.")
            binding.youtubeConnectButton.isEnabled = true
            viewModel.setLoading(false)
        }
    }
    
    /**
     * Method to initiate direct OAuth flow using the in-app WebView
     */
    private fun tryDirectOAuthFlow() {
        Log.d(TAG, "Trying direct OAuth flow with in-app WebView")
        // Use proper coroutine scope and handle suspend function
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val url = viewModel.getYouTubeAuthUrl(selectedSite.get().url)
                if (url.isNotEmpty()) {
                    navigateToInAppBrowser(url)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in direct OAuth flow: ${e.message}", e)
                uiMessageResolver.showSnack("Error: ${e.message}")
            }
        }
    }
    
    /**
     * Opens a URL in external browser as fallback
     */
    private fun openExternalBrowser(url: String) {
        try {
            Log.d(TAG, "Opening URL in external browser as fallback: $url")
            val customTabsIntent = CustomTabsIntent.Builder().build()
            customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
        } catch (e: Exception) {
            Log.e(TAG, "Error opening URL in external browser: ${e.message}", e)
            uiMessageResolver.showSnack("Error opening browser: ${e.message}")
        }
    }

    override fun onResume() {
        super.onResume()
        AINetworkLogger.logNavigation("Background", "AI Fragment")
        
        // Check connection status on resume (in case user completed OAuth flow)
        selectedSite.get()?.let { site ->
            viewModel.checkYouTubeConnectionStatus(site.url)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        AINetworkLogger.logNavigation("AI Fragment", "Destroyed")
        _binding = null
    }
}
