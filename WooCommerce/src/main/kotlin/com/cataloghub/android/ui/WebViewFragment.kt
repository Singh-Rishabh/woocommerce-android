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
                    
                    // Check for OAuth callback URLs as early as possible
                    url?.let { currentUrl ->
                        if (shouldIntercept(currentUrl)) {
                            Log.d(TAG, "Intercepted OAuth callback URL during page start: $currentUrl")
                            handleCallbackUrl(currentUrl)
                        }
                    }
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
                    return url.startsWith("woocommerce://") ||
                           url.contains("youtube_callback") ||
                           url.contains("oauth2/callback") ||
                           url.contains("oauth2callback") ||
                           (url.contains("callback") && url.contains("code="))
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
            val storeUrl = uri.getQueryParameter("state") ?: aiViewModel.getCurrentStoreUrl()
            
            // The state parameter may contain the store URL
            Log.d(TAG, "Auth code: $code, state/storeUrl: $storeUrl")
            
            if (code != null) {
                if (url.contains("youtube")) {
                    Log.d(TAG, "Handling YouTube auth callback with store URL: $storeUrl")
                    aiViewModel.handleYouTubeAuthCallback(code)
                    // Navigate back to previous screen
                    findNavController().navigateUp()
                    uiMessageResolver.showSnack("YouTube authorization successful")
                    return true
                } else if (url.contains("facebook")) {
                    aiViewModel.handleFacebookAuthCallback(code)
                    findNavController().navigateUp()
                    uiMessageResolver.showSnack("Facebook authorization successful")
                    return true
                } else if (url.contains("instagram")) {
                    aiViewModel.handleInstagramAuthCallback(code)
                    findNavController().navigateUp()
                    uiMessageResolver.showSnack("Instagram authorization successful")
                    return true
                }
            } else {
                // No auth code found in URL
                Log.e(TAG, "No auth code found in callback URL")
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
        
        return false
    }
    
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
} 