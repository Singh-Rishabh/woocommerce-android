package com.cataloghub.android.ui.ai

import com.cataloghub.android.util.WooLog
import com.cataloghub.android.util.WooLog.T

object AINetworkLogger {
    fun logRequest(endpoint: String, payload: Any?) {
        WooLog.d(T.AI, "🌐 API Request - Endpoint: $endpoint")
        WooLog.d(T.AI, "📤 Request Payload: $payload")
    }

    fun logResponse(endpoint: String, response: Any?, error: Any? = null) {
        WooLog.d(T.AI, "🌐 API Response - Endpoint: $endpoint")
        if (error != null) {
            WooLog.e(T.AI, "❌ Error Response: $error")
        } else {
            WooLog.d(T.AI, "📥 Response Data: $response")
        }
    }

    fun logFeatureCheck(isAtomic: Boolean, features: String, hasFeature: Boolean) {
        WooLog.d(T.AI, """
            🔍 AI Feature Check:
            - Is Atomic Site: $isAtomic
            - Plan Features: $features
            - Has AI Feature: $hasFeature
        """.trimIndent())
    }

    fun logNavigation(from: String, to: String) {
        WooLog.d(T.AI, "🔄 Navigation: $from -> $to")
    }

    fun logError(message: String, error: Throwable? = null) {
        if (error != null) {
            WooLog.e(T.AI, "❌ Error: $message", error)
        } else {
            WooLog.e(T.AI, "❌ Error: $message")
        }
    }
}
