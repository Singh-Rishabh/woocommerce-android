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
    private val TAG = "OAuthRedirect"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "OAuthRedirectActivity created")
        AINetworkLogger.logResponse(TAG, "Activity created")
        
        // Process the incoming intent
        handleIntent(intent)
    }
    
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d(TAG, "OAuthRedirectActivity received new intent")
        handleIntent(intent)
    }
    
    private fun handleIntent(intent: Intent) {
        val action = intent.action
        val uri = intent.data
        
        Log.d(TAG, "Handling intent - Action: $action, URI: $uri")
        
        if (action == Intent.ACTION_VIEW && uri != null) {
            Log.d(TAG, "Received OAuth callback: $uri")
            AINetworkLogger.logResponse(TAG, "Received URL: $uri")
            
            try {
                // Extract the authorization code from the URL
                val code = uri.getQueryParameter("code")
                val error = uri.getQueryParameter("error")
                
                Log.d(TAG, "Code: $code, Error: $error, Full URI: $uri")
                
                if (code != null) {
                    Log.d(TAG, "Authorization code: $code")
                    AINetworkLogger.logResponse(TAG, "Authorization code received")
                    
                    // Pass the code to the ViewModel
                    viewModel.completeYouTubeAuth(code)
                    Log.d(TAG, "Auth code passed to ViewModel")
                } else {
                    // Handle error
                    Log.e(TAG, "Authorization error: $error")
                    AINetworkLogger.logError(TAG, Exception("OAuth error: $error"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing OAuth callback", e)
                AINetworkLogger.logError(TAG, e)
            }
        } else {
            Log.e(TAG, "Invalid intent: action=$action, uri=$uri")
        }
        
        // Always finish this activity to return to the previous screen
        Log.d(TAG, "Finishing OAuthRedirectActivity")
        finish()
    }
} 