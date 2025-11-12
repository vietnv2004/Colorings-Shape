package com.uef.coloring_app

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.uef.coloring_app.core.sounds.SoundManager
import com.uef.coloring_app.core.utils.LanguageManager
import com.uef.coloring_app.core.haptic.HapticManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ColoringApplication : Application() {
    
    companion object {
        lateinit var soundManager: SoundManager
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        
        // Load saved theme preference - default to light theme
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getString("theme_mode", "light") ?: "light"
        
        // Apply saved theme
        when (themeMode) {
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "tet" -> {
                // Apply Tết theme - set to light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                // Note: Individual activities will apply Theme.ColoringShapes.Tet
            }
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        
        // Initialize SoundManager globally
        soundManager = SoundManager.getInstance(this)
        
        // Initialize language system
        LanguageManager.initializeLanguage(this)
        
        // Initialize haptic system
        HapticManager.initialize(this)
        
        // Initialize app
    }
}