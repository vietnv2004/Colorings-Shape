package com.uef.coloring_app.ui.task

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.ErrorLogger

/**
 * Canvas drawing activity for coloring tasks
 */
class CanvasDrawingActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // For now, just redirect to DrawingActivity since we already have it
            ErrorLogger.logInfo("CanvasDrawingActivity: Redirecting to DrawingActivity")
            finish()
        } catch (e: Exception) {
            ErrorLogger.logError("Error in CanvasDrawingActivity", e)
            finish()
        }
    }
}


