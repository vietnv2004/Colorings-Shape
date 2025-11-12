package com.uef.coloring_app.ui.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.ErrorLogger

/**
 * Activity to display error logs
 */
class ErrorLogActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            ErrorLogger.logInfo("ErrorLogActivity opened")
            // For now, just finish since we don't have the UI layout
            finish()
        } catch (e: Exception) {
            ErrorLogger.logError("Error in ErrorLogActivity", e)
            finish()
        }
    }
}


