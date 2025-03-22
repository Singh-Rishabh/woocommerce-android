package com.cataloghub.android.ui.ai.youtube

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabsIntent
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
import com.google.android.gms.tasks.Task

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
    private val authLauncher: ActivityResultLauncher<Intent>
) {
    private val TAG = "YouTubeAuthManager"
    private var currentStoreUrl: String? = null
    private var googleSignInClient: GoogleSignInClient? = null

    /**
     * Start the Google Sign-In flow
     * @param storeUrl The store URL to associate with this auth session
     */
    fun startGoogleSignIn(storeUrl: String) {
        Log.d(TAG, "Starting Google Sign-In flow for store: $storeUrl")
        AINetworkLogger.logNavigation("YouTube Auth", "Starting Google Sign-In")

        currentStoreUrl = storeUrl

        try {
            // Configure Google Sign-In with client ID from string resources
            val clientId = context.getString(R.string.google_web_client_id)
            Log.d(TAG, "Using client ID from resources: $clientId")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(
                    Scope("https://www.googleapis.com/auth/youtube.readonly"),
                    Scope("https://www.googleapis.com/auth/youtube.force-ssl")
                )
                // Force OAuth screen to show every time
                .requestServerAuthCode(clientId, true)
                // Set the web client ID (needed for server-side API access)
                .requestIdToken(clientId)
                .build()

            Log.d(TAG, "Created GSO with client ID: $clientId")
            
            // Create the client first
            googleSignInClient = GoogleSignIn.getClient(context, gso)
            
            // Log existing sign-in status
            val lastAccount = GoogleSignIn.getLastSignedInAccount(context)
            if (lastAccount != null) {
                Log.d(TAG, "Found existing Google Sign-in: ${lastAccount.email}")
                // Sign out to ensure we get a fresh token
                googleSignInClient?.signOut()?.addOnSuccessListener {
                    launchSignInIntent()
                }?.addOnFailureListener { e ->
                    Log.e(TAG, "Failed to sign out: ${e.message}")
                    // Still try to launch sign-in
                    launchSignInIntent()
                }
            } else {
                // No existing sign-in, launch the intent directly
                launchSignInIntent()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting Google Sign-In: ${e.message}", e)
            Log.e(TAG, "Stack trace: ${e.stackTraceToString()}")
        }
    }

    /**
     * Launch the sign-in intent
     */
    private fun launchSignInIntent() {
        try {
            val signInIntent = googleSignInClient?.signInIntent
            if (signInIntent != null) {
                Log.d(TAG, "Launching sign-in intent")
                // Ensure we're on the main thread
                Handler(Looper.getMainLooper()).post {
                    authLauncher.launch(signInIntent)
                    Log.d(TAG, "Sign-in intent launched")
                }
            } else {
                Log.e(TAG, "Sign-in intent is null")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error launching sign-in intent: ${e.message}", e)
        }
    }

    /**
     * Handle the auth result from Google Sign-In
     * @param resultCode The activity result code
     * @param data The intent data
     * @param callback Callback with the auth code or null if failed
     */
    fun handleAuthResult(
        resultCode: Int,
        data: Intent?,
        callback: (String?) -> Unit
    ) {
        Log.d(TAG, "Handling auth result: resultCode=$resultCode, data=${data != null}")

        if (resultCode != Activity.RESULT_OK) {
            Log.e(TAG, "Auth result not OK, resultCode=$resultCode")

            // Try to extract error information if available
            if (data != null) {
                val extras = data.extras
                if (extras != null) {
                    Log.d(TAG, "Intent extras: ${extras.keySet().joinToString()}")
                    extras.keySet().forEach { key ->
                        Log.d(TAG, "Intent extra: $key = ${extras.get(key)}")
                    }
                }

                // Check for GoogleSignIn specific errors
                val status = data.getIntExtra("googleSignInStatus", 0)
                if (status != 0) {
                    Log.e(TAG, "Google Sign-In status code: $status")
                }
            }

            callback(null)
            return
        }

        if (data == null) {
            Log.e(TAG, "Auth result data is null")
            callback(null)
            return
        }

        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task, callback)
        } catch (e: Exception) {
            Log.e(TAG, "Error handling sign-in result: ${e.message}", e)
            Log.e(TAG, "Error stack trace: ${e.stackTraceToString()}")
            callback(null)
        }
    }

    /**
     * Handle the sign-in result task
     */
    private fun handleSignInResult(
        completedTask: Task<GoogleSignInAccount>,
        callback: (String?) -> Unit
    ) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, "Sign-in successful: ${account.displayName}")

            // Get the server auth code
            val authCode = account.serverAuthCode
            if (authCode != null) {
                Log.d(TAG, "Got server auth code")
                callback(authCode)
            } else {
                Log.e(TAG, "Server auth code is null")
                callback(null)
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason
            Log.e(TAG, "Sign-in failed with status code: ${e.statusCode}", e)
            Log.e(TAG, "Error details: ${e.message}")
            callback(null)
        }
    }

    companion object {
        // Client ID from Google API Console
        private const val CLIENT_ID = "71251162222-lbefjggjc925osq9efm0fpufkamdrd80.apps.googleusercontent.com"
    }
}
