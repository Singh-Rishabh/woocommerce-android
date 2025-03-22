package com.cataloghub.android.ui.ai

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Utility class for logging network requests, responses, and errors related to AI features.
 * Helps with debugging API interactions.
 */
object AINetworkLogger {
    private const val TAG = "WooCommerce-AI"
    private const val MAX_LOG_LENGTH = 4000
    private val requestResponseLog = mutableListOf<LogEntry>()
    private const val MAX_LOG_ENTRIES = 50

    /**
     * Log a request being made to an API endpoint
     */
    fun logRequest(endpoint: String, details: String) {
        val message = "📤 Request: $endpoint | $details"
        Log.d(TAG, message)
        addToLog(LogEntryType.REQUEST, endpoint, details)
    }

    /**
     * Log a successful response from an API endpoint
     */
    fun logResponse(endpoint: String, details: String) {
        val message = "📥 Response: $endpoint | $details"
        Log.d(TAG, message)
        addToLog(LogEntryType.RESPONSE, endpoint, details)
    }

    /**
     * Log an error that occurred during an API call
     */
    fun logError(endpoint: String, error: Throwable) {
        val stackTrace = StringWriter().apply {
            error.printStackTrace(PrintWriter(this))
        }.toString()
        
        // Break up long stack traces for Android logging
        val message = "❌ Error: $endpoint (${error.javaClass.simpleName})\n${error.message}"
        Log.e(TAG, message)
        
        // Log stack trace in chunks if needed
        if (stackTrace.length > MAX_LOG_LENGTH) {
            var i = 0
            while (i < stackTrace.length) {
                val end = (i + MAX_LOG_LENGTH).coerceAtMost(stackTrace.length)
                Log.e(TAG, "Stack trace (${i/MAX_LOG_LENGTH + 1}): ${stackTrace.substring(i, end)}")
                i += MAX_LOG_LENGTH
            }
        } else {
            Log.e(TAG, "Stack trace: $stackTrace")
        }
        
        addToLog(LogEntryType.ERROR, endpoint, "${error.javaClass.simpleName}: ${error.message}")
    }

    /**
     * Get the recent log entries for diagnostics or reporting
     */
    fun getRecentLogs(): List<LogEntry> {
        return requestResponseLog.toList()
    }

    /**
     * Clear the log entries
     */
    fun clearLogs() {
        requestResponseLog.clear()
    }

    /**
     * Add a new entry to the log, maintaining the maximum size limit
     */
    private fun addToLog(type: LogEntryType, endpoint: String, details: String) {
        synchronized(requestResponseLog) {
            val entry = LogEntry(
                timestamp = System.currentTimeMillis(),
                type = type,
                endpoint = endpoint,
                details = details
            )
            
            requestResponseLog.add(entry)
            
            // Trim log if it exceeds maximum size
            if (requestResponseLog.size > MAX_LOG_ENTRIES) {
                requestResponseLog.removeAt(0)
            }
        }
    }

    /**
     * Log entry type (request, response, or error)
     */
    enum class LogEntryType {
        REQUEST, RESPONSE, ERROR
    }

    /**
     * Log entry data structure
     */
    data class LogEntry(
        val timestamp: Long,
        val type: LogEntryType,
        val endpoint: String,
        val details: String
    ) {
        fun getFormattedTimestamp(): String {
            val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", java.util.Locale.US)
                .format(java.util.Date(timestamp))
            return date
        }
    }
}
