package com.uef.coloring_app.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.ErrorLogger
import java.util.*

/**
 * Activity for language settings
 */
class LanguageSettingsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            ErrorLogger.logInfo("LanguageSettingsActivity opened")
            // For now, just finish since we don't have the UI layout
            finish()
        } catch (e: Exception) {
            ErrorLogger.logError("Error in LanguageSettingsActivity", e)
            finish()
        }
    }
    
    /**
     * Change app language
     */
    private fun changeLanguage(languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val currentConfig = resources.configuration
        val newConfig = android.content.res.Configuration(currentConfig)
        newConfig.setLocale(locale)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            applyOverrideConfiguration(newConfig)
        } else {
            @Suppress("DEPRECATION")
            resources.updateConfiguration(newConfig, resources.displayMetrics)
        }
        
        // Restart the activity to apply changes
        val intent = Intent(this, LanguageSettingsActivity::class.java)
        finish()
        startActivity(intent)
    }
}


