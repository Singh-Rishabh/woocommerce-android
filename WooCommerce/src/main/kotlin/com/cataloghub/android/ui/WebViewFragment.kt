package com.cataloghub.android.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.cataloghub.android.R
import com.cataloghub.android.databinding.FragmentWebViewBinding
import com.cataloghub.android.ui.ai.AINetworkLogger
import com.cataloghub.android.ui.ai.AIViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WebViewFragment : Fragment() {

    private var _binding: FragmentWebViewBinding? = null
    private val binding get() = _binding!!
    
    private val args: WebViewFragmentArgs by navArgs()
    private val viewModel: AIViewModel by viewModels()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebViewBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("OAuth", "WebViewFragment loaded with URL: ${args.url}")
        AINetworkLogger.logRequest("WebViewFragment", "Loading URL: ${args.url}")
        
        binding.webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            // Allow mixed content for debugging
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            // Add any additional settings that might help
            loadsImagesAutomatically = true
            setSupportMultipleWindows(true)
        }
        
        // Add JavaScript interface for debugging
        binding.webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun logToConsole(message: String) {
                Log.d("OAuth-JS", message)
            }
        }, "Android")
        
        binding.webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                val url = request.url.toString()
                Log.d("OAuth", "Intercepted URL: $url")
                
                // Check if the URL contains the OAuth callback
                if (url.contains("oauth2callback") || url.contains("code=")) {
                    Log.d("OAuth", "Found OAuth callback URL: $url")
                    AINetworkLogger.logResponse("OAuth", "Callback URL: $url")
                    
                    try {
                        // Extract the authorization code
                        val uri = Uri.parse(url)
                        val code = uri.getQueryParameter("code")
                        
                        if (code != null) {
                            Log.d("OAuth", "Successfully extracted code: $code")
                            viewModel.completeYouTubeAuth(code)
                            findNavController().navigateUp()
                            return true
                        } else {
                            // Check for errors
                            val error = uri.getQueryParameter("error")
                            if (error != null) {
                                Log.e("OAuth", "OAuth error: $error, details: ${uri.getQueryParameter("error_description")}")
                                Toast.makeText(
                                    requireContext(),
                                    "OAuth error: $error - ${uri.getQueryParameter("error_description")}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("OAuth", "Error processing callback: ${e.message}", e)
                        AINetworkLogger.logError("OAuth Redirect", e)
                    }
                }
                
                // Log all redirects for debugging
                Log.d("OAuth", "Loading URL: $url")
                return false
            }
            
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                Log.d("OAuth", "Page started loading: $url")
                binding.progressBar.visibility = View.VISIBLE
            }
            
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.d("OAuth", "Page finished loading: $url")
                binding.progressBar.visibility = View.GONE
                
                // Inject JavaScript to capture console logs
                val js = """
                    console.log = function(message) {
                        Android.logToConsole(message);
                    };
                """
                view?.evaluateJavascript(js, null)
            }
            
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                val errorUrl = request?.url.toString()
                Log.e("OAuth", "WebView error loading $errorUrl: ${error?.description}")
                AINetworkLogger.logError("WebView", Exception("Error loading $errorUrl: ${error?.description}"))
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    Toast.makeText(
                        requireContext(),
                        "Error: ${error?.description}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        // Set up toolbar
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Load the URL from arguments
        binding.webView.loadUrl(args.url)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 