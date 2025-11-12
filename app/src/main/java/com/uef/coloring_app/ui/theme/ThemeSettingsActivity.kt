package com.uef.coloring_app.ui.theme

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.uef.coloring_app.R
import com.uef.coloring_app.ui.common.BaseActivity

class ThemeSettingsActivity : BaseActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var systemThemeRadio: RadioButton
    private lateinit var lightThemeRadio: RadioButton
    private lateinit var darkThemeRadio: RadioButton
    private lateinit var tetThemeRadio: RadioButton
    private lateinit var systemThemeLayout: LinearLayout
    private lateinit var lightThemeLayout: LinearLayout
    private lateinit var darkThemeLayout: LinearLayout
    private lateinit var tetThemeLayout: LinearLayout
    private lateinit var systemIconLayout: LinearLayout
    private lateinit var lightIconLayout: LinearLayout
    private lateinit var darkIconLayout: LinearLayout
    private lateinit var tetIconLayout: LinearLayout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setContentView
        applyCurrentTheme()
        
        setContentView(R.layout.activity_theme_settings)
        
        setupToolbar()
        initViews()
        loadCurrentTheme()
        setupClickListeners()
    }
    
    private fun applyCurrentTheme() {
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getString("theme_mode", "light") ?: "light"
        
        when (themeMode) {
            "tet" -> setTheme(R.style.Theme_ColoringShapes_Tet)
            else -> setTheme(R.style.Theme_ColoringShapes)
        }
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.theme_settings)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun initViews() {
        sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        systemThemeRadio = findViewById(R.id.systemThemeRadio)
        lightThemeRadio = findViewById(R.id.lightThemeRadio)
        darkThemeRadio = findViewById(R.id.darkThemeRadio)
        tetThemeRadio = findViewById(R.id.tetThemeRadio)
        systemThemeLayout = findViewById(R.id.systemThemeLayout)
        lightThemeLayout = findViewById(R.id.lightThemeLayout)
        darkThemeLayout = findViewById(R.id.darkThemeLayout)
        tetThemeLayout = findViewById(R.id.tetThemeLayout)
        systemIconLayout = findViewById(R.id.systemIconLayout)
        lightIconLayout = findViewById(R.id.lightIconLayout)
        darkIconLayout = findViewById(R.id.darkIconLayout)
        tetIconLayout = findViewById(R.id.tetIconLayout)
        
        // Hide Tet theme
        tetThemeLayout.visibility = android.view.View.GONE
        tetThemeRadio.visibility = android.view.View.GONE
        tetIconLayout.visibility = android.view.View.GONE
    }
    
    private fun loadCurrentTheme() {
        val currentTheme = sharedPreferences.getString("theme_mode", "light") ?: "light"
        
        when (currentTheme) {
            "system" -> systemThemeRadio.isChecked = true
            "light" -> lightThemeRadio.isChecked = true
            "dark" -> darkThemeRadio.isChecked = true
            "tet" -> {
                // If Tet theme is selected, switch to light theme
                lightThemeRadio.isChecked = true
                saveThemePreference("light")
            }
        }
    }
    
    private fun setupClickListeners() {
        // Layout click listeners - click anywhere on the row
        systemThemeLayout.setOnClickListener {
            selectTheme("system")
        }
        
        lightThemeLayout.setOnClickListener {
            selectTheme("light")
        }
        
        darkThemeLayout.setOnClickListener {
            selectTheme("dark")
        }
        
        // Icon click listeners - click on icon
        systemIconLayout.setOnClickListener {
            selectTheme("system")
        }
        
        lightIconLayout.setOnClickListener {
            selectTheme("light")
        }
        
        darkIconLayout.setOnClickListener {
            selectTheme("dark")
        }
        
        // Radio button click listeners
        systemThemeRadio.setOnClickListener {
            selectTheme("system")
        }
        
        lightThemeRadio.setOnClickListener {
            selectTheme("light")
        }
        
        darkThemeRadio.setOnClickListener {
            selectTheme("dark")
        }
    }
    
    private fun selectTheme(theme: String) {
        // Uncheck all radio buttons first
        systemThemeRadio.isChecked = false
        lightThemeRadio.isChecked = false
        darkThemeRadio.isChecked = false
        
        // Check the selected theme
        when (theme) {
            "system" -> {
                systemThemeRadio.isChecked = true
                saveThemePreference("system")
                applyTheme("system")
                showThemePreview(getString(R.string.system_theme))
            }
            "light" -> {
                lightThemeRadio.isChecked = true
                saveThemePreference("light")
                applyTheme("light")
                showThemePreview(getString(R.string.light_theme))
            }
            "dark" -> {
                darkThemeRadio.isChecked = true
                saveThemePreference("dark")
                applyTheme("dark")
                showThemePreview(getString(R.string.dark_theme))
            }
        }
    }
    
    private fun saveThemePreference(theme: String) {
        sharedPreferences.edit()
            .putString("theme_mode", theme)
            .apply()
    }
    
    private fun applyTheme(theme: String) {
        when (theme) {
            "system" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
            "light" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            "dark" -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            "tet" -> {
                // Apply Tết theme - set to light mode and apply custom theme
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                // Apply Tết theme to current activity
                setTheme(R.style.Theme_ColoringShapes_Tet)
            }
        }
        
        // Restart activity to apply theme changes
        recreate()
    }
    
    private fun showThemePreview(themeName: String) {
        val previewText = findViewById<TextView>(R.id.themePreviewText)
        val message = "${getString(R.string.theme_selected, themeName)}\n\n${getString(R.string.theme_applied_immediately)}"
        previewText.text = message
    }
}
