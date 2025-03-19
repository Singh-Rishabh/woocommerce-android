package com.cataloghub.android.ui.ai.youtube

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.cataloghub.android.R
import com.cataloghub.android.ui.ai.AINetworkLogger
import com.cataloghub.android.ui.ai.AIViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Scope

/**
 * Manager class for handling YouTube authorization using Google Sign-In
 * 
 * IMPORTANT IMPLEMENTATION NOTE:
 * This class uses deprecated GoogleSignIn APIs. Google has been transitioning between
 * authentication systems:
 * 1. The original GoogleSignIn API (deprecated but stable for YouTube scopes)
 * 2. The Identity Services API (which has also been marked as deprecated)
 * 3. The Google Identity API/AuthAPI (still evolving)
 * 
 * As of 2023, we've opted to use the original GoogleSignIn APIs with suppression
 * annotations because:
 * - It provides stable support for YouTube scopes which we need for this feature
 * - The newer APIs don't fully support all the YouTube scopes we need yet
 * - It has the most reliable implementation for our current use case
 * 
 * We suppress deprecation warnings via:
 * 1. Class-level @Suppress annotation
 * 2. Kotlin compiler options in build.gradle
 * 
 * FUTURE ENHANCEMENTS:
 * 1. When the Google Identity API stabilizes, migrate to the newer APIs
 * 2. Implement Cross-Account Protection (https://developers.google.com/identity/protocols/risc)
 *    to improve security by handling security events from Google
 * 3. Consider using Sign In With Google (https://developers.google.com/identity/gsi/web/guides/overview)
 *    for a more modern authentication experience
 *
 * This implementation should be reviewed periodically as Google stabilizes their auth APIs.
 */
@Suppress("DEPRECATION", "OVERRIDE_DEPRECATION", "DEPRECATION_ERROR")
class YouTubeAuthManager(
    private val context: Context,
    private val authResultLauncher: ActivityResultLauncher<Intent>
) {
    
    private val TAG = "YouTubeAuth"
    
    // Constants for YouTube authorization scopes
    companion object {
        private const val YOUTUBE_SCOPE = "https://www.googleapis.com/auth/youtube.readonly"
        private const val YOUTUBE_MANAGE_SCOPE = "https://www.googleapis.com/auth/youtube"
        private const val PROFILE_SCOPE = "https://www.googleapis.com/auth/userinfo.profile"
    }
    
    // Google Sign-In client
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_web_client_id))
            .requestServerAuthCode(context.getString(R.string.google_web_client_id), true)
            .requestEmail()
            .requestScopes(
                Scope(YOUTUBE_SCOPE),
                Scope(YOUTUBE_MANAGE_SCOPE),
                Scope(PROFILE_SCOPE)
            )
            .build()
            
        GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Begins the YouTube authorization process
     * @param activity The activity that will handle the result
     */
    fun beginAuthorization(activity: Activity) {
        Log.d(TAG, "Beginning YouTube authorization")
        AINetworkLogger.logRequest(TAG, "Starting YouTube authorization flow")
        
        // Log the client ID being used
        Log.d(TAG, "Using client ID: ${activity.getString(R.string.google_web_client_id)}")
        
        try {
            // Launch the Google Sign-In intent via the activity result launcher
            val signInIntent = googleSignInClient.signInIntent
            authResultLauncher.launch(signInIntent)
            
            Log.d(TAG, "Launched sign-in intent via ActivityResultLauncher")
        } catch (e: Exception) {
            Log.e(TAG, "Error launching sign-in intent: ${e.message}", e)
            AINetworkLogger.logError(TAG, e)
        }
    }
    
    /**
     * Handles the result from the authorization flow
     * @param resultCode The result code from the activity result
     * @param data The intent data from the activity result
     * @param viewModel The AIViewModel to update with the authorization result
     * @return true if the result was handled, false otherwise
     */
    fun handleAuthResult(
        resultCode: Int,
        data: Intent?,
        viewModel: AIViewModel
    ): Boolean {
        Log.d(TAG, "Handling auth result: resultCode=$resultCode, data=${data != null}")
        
        try {
            if (resultCode == Activity.RESULT_OK && data != null) {
                Log.d(TAG, "Auth result received: OK")
                
                // Try to get the sign-in result
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)
                    
                    return handleSignInAccount(account, viewModel)
                } catch (e: ApiException) {
                    // Handle the specific API exception with more details
                    Log.e(TAG, "Sign-in failed: statusCode=${e.statusCode}, message=${e.message}", e)
                    AINetworkLogger.logError(TAG, Exception("Sign-in ApiException (${e.statusCode}): ${e.message}"))
                    
                    // Show more detailed error message based on status code
                    val errorMessage = when (e.statusCode) {
                        CommonStatusCodes.NETWORK_ERROR -> 
                            "Network error - please check your internet connection"
                        CommonStatusCodes.DEVELOPER_ERROR -> 
                            "Developer error - check OAuth configuration"
                        CommonStatusCodes.CANCELED -> 
                            "Sign-in was canceled"
                        else -> "Sign-in failed with error code: ${e.statusCode}"
                    }
                    Log.e(TAG, errorMessage)
                }
            } else {
                Log.e(TAG, "Authorization failed or was cancelled: resultCode=$resultCode")
                AINetworkLogger.logError(TAG, Exception("Authorization cancelled or failed with result code: $resultCode"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling auth result: ${e.message}", e)
            AINetworkLogger.logError(TAG, e)
        }
        
        return false
    }
    
    /**
     * Handles a successful sign-in account
     */
    private fun handleSignInAccount(account: GoogleSignInAccount, viewModel: AIViewModel): Boolean {
        Log.d(TAG, "Sign-in successful: ${account.displayName}, Email: ${account.email}")
        Log.d(TAG, "ID token: ${account.idToken?.take(20)}...")
        
        // Get the authorization code
        val authCode = account.serverAuthCode
        if (authCode != null) {
            Log.d(TAG, "Auth code received: $authCode")
            AINetworkLogger.logResponse(TAG, "Authorization successful, code received")
            
            // Complete the authorization with the ViewModel
            viewModel.completeYouTubeAuth(authCode)
            return true
        } else {
            Log.e(TAG, "Auth code is null - account info: ${account}")
            AINetworkLogger.logError(TAG, Exception("Authorization code is null"))
        }
        
        return false
    }
    
    /**
     * Disconnects the user from YouTube
     */
    fun disconnectYouTube(onComplete: () -> Unit) {
        googleSignInClient.signOut()
            .addOnSuccessListener {
                Log.d(TAG, "Sign out successful")
                AINetworkLogger.logResponse(TAG, "Disconnected from YouTube")
                onComplete()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Sign out failed: ${e.message}", e)
                AINetworkLogger.logError(TAG, e)
                onComplete()
            }
    }
}
