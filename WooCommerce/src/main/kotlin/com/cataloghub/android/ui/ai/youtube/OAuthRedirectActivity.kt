package com.cataloghub.android.ui.ai.youtube

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.cataloghub.android.ui.ai.AINetworkLogger
import com.cataloghub.android.ui.ai.AIViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Transparent activity that handles OAuth redirects from external browsers.
 * This activity has no UI and processes the authorization code from the redirect URL
 * before finishing itself.
 */
@AndroidEntryPoint
class OAuthRedirectActivity : ComponentActivity() {
    
    private val viewModel: AIViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Process the incoming intent
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val uri = intent.data
        
        if (action == Intent.ACTION_VIEW && uri != null) {
            Log.d("OAuth", "Received OAuth callback: $uri")
            AINetworkLogger.logResponse("OAuth Redirect", "Received URL: $uri")
            
            try {
                // Extract the authorization code from the URL
                val code = uri.getQueryParameter("code")
                
                if (code != null) {
                    Log.d("OAuth", "Authorization code: $code")
                    AINetworkLogger.logResponse("OAuth Redirect", "Authorization code received")
                    
                    // Pass the code to the ViewModel
                    viewModel.completeYouTubeAuth(code)
                } else {
                    // Handle error
                    val error = uri.getQueryParameter("error")
                    Log.e("OAuth", "Authorization error: $error")
                    AINetworkLogger.logError("OAuth Redirect", Exception("OAuth error: $error"))
                }
            } catch (e: Exception) {
                Log.e("OAuth", "Error processing OAuth callback", e)
                AINetworkLogger.logError("OAuth Redirect", e)
            }
        }
        
        // Always finish this activity to return to the previous screen
        finish()
    }
} 