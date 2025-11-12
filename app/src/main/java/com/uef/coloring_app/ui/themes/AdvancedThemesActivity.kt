package com.uef.coloring_app.ui.themes

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.uef.coloring_app.R

class AdvancedThemesActivity : AppCompatActivity() {

    private lateinit var themeStatusTextView: TextView
    private lateinit var themeInfoTextView: TextView
    private lateinit var themeRadioGroup: RadioGroup
    private lateinit var lightThemeRadio: RadioButton
    private lateinit var darkThemeRadio: RadioButton
    private lateinit var autoThemeRadio: RadioButton
    private lateinit var colorPickerButton: Button
    private lateinit var resetButton: Button

    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        private const val PREFS_NAME = "ThemeSettings"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val THEME_LIGHT = 0
        private const val THEME_DARK = 1
        private const val THEME_AUTO = 2
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_themes)

        // Hide action bar
        supportActionBar?.hide()

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initViews()
        loadThemeSettings()
        setupClickListeners()
    }

    private fun initViews() {
        themeStatusTextView = findViewById(R.id.themeStatusTextView)
        themeInfoTextView = findViewById(R.id.themeInfoTextView)
        themeRadioGroup = findViewById(R.id.themeRadioGroup)
        lightThemeRadio = findViewById(R.id.lightThemeRadio)
        darkThemeRadio = findViewById(R.id.darkThemeRadio)
        autoThemeRadio = findViewById(R.id.autoThemeRadio)
        colorPickerButton = findViewById(R.id.colorPickerButton)
        resetButton = findViewById(R.id.resetButton)
    }

    private fun loadThemeSettings() {
        val savedThemeMode = sharedPreferences.getInt(KEY_THEME_MODE, THEME_LIGHT)
        
        when (savedThemeMode) {
            THEME_LIGHT -> {
                lightThemeRadio.isChecked = true
                themeStatusTextView.text = "Theme is ready for customization"
            }
            THEME_DARK -> {
                darkThemeRadio.isChecked = true
                themeStatusTextView.text = "Dark theme is active"
            }
            THEME_AUTO -> {
                autoThemeRadio.isChecked = true
                themeStatusTextView.text = "Auto theme is enabled"
            }
        }
    }

    private fun setupClickListeners() {
        themeRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.lightThemeRadio -> {
                    applyTheme(THEME_LIGHT)
                    themeStatusTextView.text = "Theme is ready for customization"
                }
                R.id.darkThemeRadio -> {
                    applyTheme(THEME_DARK)
                    themeStatusTextView.text = "Dark theme is active"
                }
                R.id.autoThemeRadio -> {
                    applyTheme(THEME_AUTO)
                    themeStatusTextView.text = "Auto theme is enabled"
                }
            }
        }

        colorPickerButton.setOnClickListener {
            val intent = android.content.Intent(this, ColorPickerActivity::class.java)
            startActivity(intent)
        }

        resetButton.setOnClickListener {
            resetToDefaultTheme()
        }
    }

    private fun applyTheme(themeMode: Int) {
        // Save theme preference
        sharedPreferences.edit().putInt(KEY_THEME_MODE, themeMode).apply()

        // Apply theme
        when (themeMode) {
            THEME_LIGHT -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            THEME_DARK -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            THEME_AUTO -> {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            }
        }

        // Recreate activity to apply theme
        recreate()
    }

    private fun resetToDefaultTheme() {
        // Reset to light theme
        lightThemeRadio.isChecked = true
        applyTheme(THEME_LIGHT)
        
        // Clear color preferences
        sharedPreferences.edit()
            .remove("primary_color")
            .remove("secondary_color")
            .remove("color_intensity")
            .apply()
            
        android.widget.Toast.makeText(this, "Theme đã được đặt lại về mặc định", android.widget.Toast.LENGTH_SHORT).show()
    }
}