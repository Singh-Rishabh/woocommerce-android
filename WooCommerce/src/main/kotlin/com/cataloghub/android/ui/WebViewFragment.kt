package com.cataloghub.android.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.WebSettings
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentWebViewBinding
import com.cataloghub.android.ui.ai.AINetworkLogger
import com.cataloghub.android.ui.ai.AIViewModel
import com.cataloghub.android.ui.base.BaseFragment
import com.cataloghub.android.ui.base.UIMessageResolver
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class WebViewFragment : BaseFragment(R.layout.fragment_web_view) {
    companion object {
        const val TAG = "WebViewFragment"
    }
    
    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!
    
    private val args: WebViewFragmentArgs by navArgs()
    private val viewModel: WebViewViewModel by viewModels()
    
    @Inject lateinit var uiMessageResolver: UIMessageResolver
    
    // Reference to AIViewModel to handle YouTube auth
    private val aiViewModel: AIViewModel by activityViewModels()
    
    override fun getFragmentTitle() = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWebViewBinding.bind(view)
        
        // Set up toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Get the URL to load
        val urlToLoad = args.url
        Log.d(TAG, "Initial URL to load: $urlToLoad")
        
        // Log URL details to help with debugging
        try {
            val uri = Uri.parse(urlToLoad)
            Log.d(TAG, "URL scheme: ${uri.scheme}, host: ${uri.host}, path: ${uri.path}")
            Log.d(TAG, "URL query params: ${uri.query}")
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing URL: ${e.message}")
        }
        
        // Configure WebView with proper OAuth browser settings
        with(binding.webView) {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                useWideViewPort = true
                loadWithOverviewMode = true
                setSupportZoom(true)
                javaScriptCanOpenWindowsAutomatically = true
                setSupportMultipleWindows(true)
                loadsImagesAutomatically = true
                
                // These settings help with Google OAuth compatibility
                userAgentString = System.getProperty("http.agent") ?: "Mozilla/5.0 (Linux; Android 10) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.5563.116 Mobile Safari/537.36"
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                setGeolocationEnabled(false) // Not needed for OAuth
            }
            
            // Enable debugging if in development build
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
            
            // Setup WebViewClient to handle navigation events
            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    Log.d(TAG, "Page load started: $url")
                    binding.progressBar.isVisible = true
                }
                
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    Log.d(TAG, "Page load finished: $url")
                    binding.progressBar.isVisible = false
                    
                    if (url?.contains("error=") == true) {
                        logErrorDetails(url)
                    }
                }
                
                override fun onReceivedError(
                    view: WebView?,
                    request: WebResourceRequest?,
                    error: WebResourceError?
                ) {
                    super.onReceivedError(view, request, error)
                    Log.e(TAG, "Error loading page: ${error?.description}, URL: ${request?.url}")
                    binding.progressBar.isVisible = false
                    
                    uiMessageResolver.showSnack("Error loading page: ${error?.description}")
                }
                
                // Handle URL loading to intercept OAuth callback URLs
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    val url = request?.url?.toString() ?: return false
                    Log.d(TAG, "URL loading: $url")
                    
                    if (shouldIntercept(url)) {
                        Log.d(TAG, "Intercepted URL in shouldOverrideUrlLoading: $url")
                        return handleCallbackUrl(url)
                    }
                    
                    return false
                }
                
                private fun shouldIntercept(url: String): Boolean {
                    // Only intercept the specific callback URI defined for the app
                    val callbackScheme = "com.cataloghub.android://oauth2callback"
                    // Also handle the older woocommerce:// scheme if still used
                    val oldCallbackScheme = "woocommerce://"
                    return url.startsWith(callbackScheme) || url.startsWith(oldCallbackScheme)
                }
                
                private fun logErrorDetails(url: String) {
                    Log.e(TAG, "OAuth error in URL: $url")
                    try {
                        val uri = Uri.parse(url)
                        val error = uri.getQueryParameter("error")
                        val errorDescription = uri.getQueryParameter("error_description")
                        Log.e(TAG, "OAuth error: $error, description: $errorDescription")
                        
                        if (error != null) {
                            uiMessageResolver.showSnack("Authentication error: $error")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse error URL: ${e.message}")
                    }
                }
            }
            
            // Add JavaScript interface for logging
            addJavascriptInterface(object {
                @android.webkit.JavascriptInterface
                fun log(message: String) {
                    Log.d(TAG, "JavaScript log: $message")
                }
            }, "Android")
            
            // Load the URL provided in arguments
            loadUrl(urlToLoad)
        }
    }
    
    /**
     * Handle callback URL from OAuth process
     */
    private fun handleCallbackUrl(url: String): Boolean {
        Log.d(TAG, "Handling callback URL: $url")
        
        try {
            val uri = Uri.parse(url)
            val code = uri.getQueryParameter("code")
            val error = uri.getQueryParameter("error")
            
            // First check for OAuth errors
            if (error != null) {
                Log.e(TAG, "OAuth error: $error")
                val errorDescription = uri.getQueryParameter("error_description")
                val errorMessage = if (errorDescription != null) "$error: $errorDescription" else error
                
                findNavController().navigateUp()
                uiMessageResolver.showSnack("Authorization failed: $errorMessage")
                return true
            }
            
            // If we have a valid auth code, process it
            if (code != null) {
                Log.d(TAG, "Authorization code found: $code")
                
                when {
                    url.contains("youtube") -> {
                        Log.d(TAG, "Processing YouTube auth callback")
                        aiViewModel.handleYouTubeAuthCallback(code)
                        findNavController().navigateUp()
                        uiMessageResolver.showSnack("YouTube authorization successful")
                    }
                    url.contains("facebook") -> {
                        aiViewModel.handleFacebookAuthCallback(code)
                        findNavController().navigateUp()
                        uiMessageResolver.showSnack("Facebook authorization successful")
                    }
                    url.contains("instagram") -> {
                        aiViewModel.handleInstagramAuthCallback(code)
                        findNavController().navigateUp()
                        uiMessageResolver.showSnack("Instagram authorization successful")
                    }
                    else -> {
                        Log.d(TAG, "Unrecognized OAuth provider in callback URL")
                        findNavController().navigateUp()
                        uiMessageResolver.showSnack("Authorization successful")
                    }
                }
                return true
            } else {
                // No auth code found, check if it's a cancellation
                if (url.contains("denied") || url.contains("cancel")) {
                    Log.d(TAG, "User cancelled the authorization")
                    findNavController().navigateUp()
                    uiMessageResolver.showSnack("Authorization cancelled")
                    return true
                }
                
                // Otherwise, it's some other error
                Log.e(TAG, "No authorization code found in callback URL")
                findNavController().navigateUp()
                uiMessageResolver.showSnack("Authorization failed: No code provided")
                return true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling callback URL: ${e.message}", e)
            findNavController().navigateUp()
            uiMessageResolver.showSnack("Error processing authorization")
            return false
        }
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
} 