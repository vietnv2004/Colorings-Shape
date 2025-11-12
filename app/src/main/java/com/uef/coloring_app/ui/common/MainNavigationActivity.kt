package com.uef.coloring_app.ui.common

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.ErrorLogger

/**
 * Main navigation activity with bottom navigation
 */
class MainNavigationActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // For now, just redirect to SimpleMainActivity since we don't have navigation setup
            ErrorLogger.logInfo("MainNavigationActivity: Redirecting to SimpleMainActivity")
            finish()
        } catch (e: Exception) {
            ErrorLogger.logError("Error in MainNavigationActivity", e)
            finish()
        }
    }
}


