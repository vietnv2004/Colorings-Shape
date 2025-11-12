package com.uef.coloring_app.ui.haptic

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.uef.coloring_app.R

class HapticControlsActivity : AppCompatActivity() {
    
    private lateinit var vibrator: Vibrator
    private lateinit var hapticMasterSwitch: Switch
    private lateinit var lightHapticSwitch: Switch
    private lateinit var mediumHapticSwitch: Switch
    private lateinit var heavyHapticSwitch: Switch
    private lateinit var hapticIntensitySeekBar: SeekBar
    private lateinit var hapticIntensityValue: TextView
    private lateinit var hapticStatusText: TextView
    
    private var currentIntensity = 50
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_haptic_controls)
        
        initializeVibrator()
        initializeViews()
        setupListeners()
        loadHapticSettings()
    }
    
    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (!vibrator.hasVibrator()) {
            Toast.makeText(this, "Thiết bị không hỗ trợ rung động", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun initializeViews() {
        hapticMasterSwitch = findViewById(R.id.hapticMasterSwitch)
        lightHapticSwitch = findViewById(R.id.lightHapticSwitch)
        mediumHapticSwitch = findViewById(R.id.mediumHapticSwitch)
        heavyHapticSwitch = findViewById(R.id.heavyHapticSwitch)
        hapticIntensitySeekBar = findViewById(R.id.hapticIntensitySeekBar)
        hapticIntensityValue = findViewById(R.id.hapticIntensityValue)
        hapticStatusText = findViewById(R.id.hapticStatusText)
    }
    
    private fun setupListeners() {
        // Master switch listener
        hapticMasterSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateHapticStatus(isChecked)
            enableHapticControls(isChecked)
        }
        
        // Intensity seekbar listener
        hapticIntensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentIntensity = progress
                hapticIntensityValue.text = "$progress%"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Test buttons
        findViewById<android.widget.Button>(R.id.testLightHapticButton).setOnClickListener {
            testHapticFeedback(HapticType.LIGHT)
        }
        
        findViewById<android.widget.Button>(R.id.testMediumHapticButton).setOnClickListener {
            testHapticFeedback(HapticType.MEDIUM)
        }
        
        findViewById<android.widget.Button>(R.id.testHeavyHapticButton).setOnClickListener {
            testHapticFeedback(HapticType.HEAVY)
        }
        
        // Pattern buttons
        findViewById<android.widget.Button>(R.id.singleTapPatternButton).setOnClickListener {
            testHapticPattern(HapticPattern.SINGLE_TAP)
        }
        
        findViewById<android.widget.Button>(R.id.doubleTapPatternButton).setOnClickListener {
            testHapticPattern(HapticPattern.DOUBLE_TAP)
        }
        
        findViewById<android.widget.Button>(R.id.longPressPatternButton).setOnClickListener {
            testHapticPattern(HapticPattern.LONG_PRESS)
        }
        
        // Advanced controls
        findViewById<android.widget.Button>(R.id.resetHapticButton).setOnClickListener {
            resetHapticSettings()
        }
        
        findViewById<android.widget.Button>(R.id.saveHapticButton).setOnClickListener {
            saveHapticSettings()
        }
    }
    
    private fun updateHapticStatus(isEnabled: Boolean) {
        hapticStatusText.text = if (isEnabled) {
            getString(R.string.haptic_status_ready)
        } else {
            "Tính năng rung động đã tắt"
        }
    }
    
    private fun enableHapticControls(isEnabled: Boolean) {
        lightHapticSwitch.isEnabled = isEnabled
        mediumHapticSwitch.isEnabled = isEnabled
        heavyHapticSwitch.isEnabled = isEnabled
        hapticIntensitySeekBar.isEnabled = isEnabled
    }
    
    private fun testHapticFeedback(type: HapticType) {
        if (!hapticMasterSwitch.isChecked) {
            Toast.makeText(this, "Vui lòng bật tính năng rung động", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intensity = currentIntensity / 100f
        val duration = when (type) {
            HapticType.LIGHT -> (50 * intensity).toLong()
            HapticType.MEDIUM -> (100 * intensity).toLong()
            HapticType.HEAVY -> (200 * intensity).toLong()
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitude = (255 * intensity).toInt()
            val effect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
        
        Toast.makeText(this, "Đã test ${type.name.lowercase()} haptic", Toast.LENGTH_SHORT).show()
    }
    
    private fun testHapticPattern(pattern: HapticPattern) {
        if (!hapticMasterSwitch.isChecked) {
            Toast.makeText(this, "Vui lòng bật tính năng rung động", Toast.LENGTH_SHORT).show()
            return
        }
        
        val intensity = currentIntensity / 100f
        val amplitude = (255 * intensity).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = when (pattern) {
                HapticPattern.SINGLE_TAP -> VibrationEffect.createOneShot(50, amplitude)
                HapticPattern.DOUBLE_TAP -> VibrationEffect.createWaveform(
                    longArrayOf(0, 50, 100, 50), 
                    intArrayOf(0, amplitude, 0, amplitude), 
                    -1
                )
                HapticPattern.LONG_PRESS -> VibrationEffect.createOneShot(200, amplitude)
            }
            vibrator.vibrate(effect)
        } else {
            val duration = when (pattern) {
                HapticPattern.SINGLE_TAP -> 50L
                HapticPattern.DOUBLE_TAP -> 200L
                HapticPattern.LONG_PRESS -> 200L
            }
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
        
        Toast.makeText(this, "Đã test pattern ${pattern.name.lowercase()}", Toast.LENGTH_SHORT).show()
    }
    
    private fun resetHapticSettings() {
        hapticMasterSwitch.isChecked = true
        lightHapticSwitch.isChecked = true
        mediumHapticSwitch.isChecked = true
        heavyHapticSwitch.isChecked = true
        hapticIntensitySeekBar.progress = 50
        currentIntensity = 50
        hapticIntensityValue.text = "50%"
        updateHapticStatus(true)
        enableHapticControls(true)
        
        Toast.makeText(this, "Đã reset cài đặt rung động", Toast.LENGTH_SHORT).show()
    }
    
    private fun saveHapticSettings() {
        // Save settings to SharedPreferences
        val prefs = getSharedPreferences("haptic_settings", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        editor.putBoolean("haptic_enabled", hapticMasterSwitch.isChecked)
        editor.putBoolean("light_haptic_enabled", lightHapticSwitch.isChecked)
        editor.putBoolean("medium_haptic_enabled", mediumHapticSwitch.isChecked)
        editor.putBoolean("heavy_haptic_enabled", heavyHapticSwitch.isChecked)
        editor.putInt("haptic_intensity", currentIntensity)
        
        editor.apply()
        
        Toast.makeText(this, "Đã lưu cài đặt rung động", Toast.LENGTH_SHORT).show()
    }
    
    private fun loadHapticSettings() {
        val prefs = getSharedPreferences("haptic_settings", Context.MODE_PRIVATE)
        
        hapticMasterSwitch.isChecked = prefs.getBoolean("haptic_enabled", true)
        lightHapticSwitch.isChecked = prefs.getBoolean("light_haptic_enabled", true)
        mediumHapticSwitch.isChecked = prefs.getBoolean("medium_haptic_enabled", true)
        heavyHapticSwitch.isChecked = prefs.getBoolean("heavy_haptic_enabled", true)
        currentIntensity = prefs.getInt("haptic_intensity", 50)
        
        hapticIntensitySeekBar.progress = currentIntensity
        hapticIntensityValue.text = "$currentIntensity%"
        
        updateHapticStatus(hapticMasterSwitch.isChecked)
        enableHapticControls(hapticMasterSwitch.isChecked)
    }
    
    enum class HapticType {
        LIGHT, MEDIUM, HEAVY
    }
    
    enum class HapticPattern {
        SINGLE_TAP, DOUBLE_TAP, LONG_PRESS
    }
}
