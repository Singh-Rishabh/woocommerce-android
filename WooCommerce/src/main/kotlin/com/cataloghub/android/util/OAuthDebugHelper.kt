package com.cataloghub.android.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent

/**
 * Helper class for debugging OAuth issues
 */
object OAuthDebugHelper {
    
    private const val TAG = "OAuthDebug"
    
    /**
     * Tests if Chrome Custom Tabs can be opened with a simple URL
     */
    fun testCustomTabs(context: Context) {
        try {
            Log.d(TAG, "Testing Custom Tabs with google.com")
            val url = "https://www.google.com"
            
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build()
                
            customTabsIntent.launchUrl(context, Uri.parse(url))
            
            Toast.makeText(context, "Custom Tabs launched successfully", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Custom Tabs opened successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open Custom Tabs: ${e.message}", e)
            Toast.makeText(context, "Custom Tabs failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Tests if regular browser intent can be opened
     */
    fun testBrowserIntent(context: Context) {
        try {
            Log.d(TAG, "Testing browser intent with google.com")
            val url = "https://www.google.com"
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
            
            Toast.makeText(context, "Browser intent launched successfully", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Browser intent opened successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open browser intent: ${e.message}", e)
            Toast.makeText(context, "Browser intent failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Verifies that the OAuth redirect URI is properly configured
     */
    fun checkOAuthRedirectConfig(context: Context) {
        val redirectScheme = "com.cataloghub.android"
        val redirectHost = "oauth2callback"
        
        // Simulate a successful OAuth callback
        val testUrl = "$redirectScheme://$redirectHost?code=test_code"
        
        try {
            Log.d(TAG, "Testing OAuth redirect handling with URL: $testUrl")
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(testUrl))
            context.startActivity(intent)
            
            Toast.makeText(context, "Redirect handling test launched", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to test redirect handling: ${e.message}", e)
            Toast.makeText(context, 
                "Redirect handling failed: ${e.message}", 
                Toast.LENGTH_LONG).show()
        }
    }
} 