package com.uef.coloring_app.core.utils

import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility class for logging errors and exceptions
 */
object ErrorLogger {
    private const val TAG = "ColoringApp"
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val errorLogs = mutableListOf<ErrorLog>()
    
    data class ErrorLog(
        val timestamp: String,
        val message: String,
        val stackTrace: String?
    )
    
    /**
     * Log an error message
     */
    fun logError(message: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val stackTrace = throwable?.let { getStackTraceString(it) }
        
        // Log to Android logcat
        if (throwable != null) {
            Log.e(TAG, message, throwable)
        } else {
            Log.e(TAG, message)
        }
        
        // Store in memory for viewing in ErrorLogActivity
        errorLogs.add(ErrorLog(timestamp, message, stackTrace))
        
        // Keep only last 100 errors
        if (errorLogs.size > 100) {
            errorLogs.removeAt(0)
        }
    }
    
    /**
     * Log a warning message
     */
    fun logWarning(message: String) {
        Log.w(TAG, message)
    }
    
    /**
     * Log an info message
     */
    fun logInfo(message: String) {
        Log.i(TAG, message)
    }
    
    /**
     * Log a debug message
     */
    fun logDebug(message: String) {
        Log.d(TAG, message)
    }
    
    /**
     * Get all error logs
     */
    fun getErrorLogs(): List<ErrorLog> {
        return errorLogs.toList()
    }
    
    /**
     * Clear all error logs
     */
    fun clearLogs() {
        errorLogs.clear()
    }
    
    /**
     * Get stack trace as string
     */
    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }
}


