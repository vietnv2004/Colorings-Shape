package com.uef.coloring_app.ui.haptics

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.uef.coloring_app.R
import com.uef.coloring_app.core.haptic.HapticManager
import com.uef.coloring_app.ui.common.BaseActivity

class AdvancedHapticsActivity : BaseActivity() {
    
    private lateinit var hapticStatusTextView: TextView
    private lateinit var hapticInfoTextView: TextView
    private lateinit var testHapticButton: Button
    private lateinit var intensitySeekBar: SeekBar
    private lateinit var enableHapticsSwitch: Switch
    private lateinit var vibrationHapticsSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_haptics)
        
        initViews()
        setupClickListeners()
        setupSeekBars()
        loadCurrentSettings()
    }
    
    private fun initViews() {
        hapticStatusTextView = findViewById(R.id.hapticStatusTextView)
        hapticInfoTextView = findViewById(R.id.hapticInfoTextView)
        testHapticButton = findViewById(R.id.testHapticButton)
        intensitySeekBar = findViewById(R.id.intensitySeekBar)
        enableHapticsSwitch = findViewById(R.id.enableHapticsSwitch)
        vibrationHapticsSwitch = findViewById(R.id.vibrationHapticsSwitch)
    }
    
    private fun setupClickListeners() {
        testHapticButton.setOnClickListener {
            HapticManager.buttonClick(this)
            testHaptic()
        }
        
        enableHapticsSwitch.setOnCheckedChangeListener { _, isChecked ->
            HapticManager.setEnabled(isChecked)
            updateStatus()
        }
        
        vibrationHapticsSwitch.setOnCheckedChangeListener { _, _ ->
            // Vibration is always enabled when haptics are enabled
            updateStatus()
        }
    }
    
    private fun setupSeekBars() {
        intensitySeekBar.max = 100
        intensitySeekBar.progress = HapticManager.getIntensity()
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    HapticManager.setIntensity(progress)
                    updateStatus()
                }
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun loadCurrentSettings() {
        enableHapticsSwitch.isChecked = HapticManager.isHapticEnabled()
        vibrationHapticsSwitch.isChecked = HapticManager.isHapticEnabled()
        intensitySeekBar.progress = HapticManager.getIntensity()
        updateStatus()
    }
    
    private fun updateStatus() {
        val status = if (HapticManager.isHapticEnabled()) {
            getString(R.string.haptic_status_enabled, HapticManager.getIntensity())
        } else {
            getString(R.string.haptic_status_disabled)
        }
        hapticStatusTextView.text = status
        
        hapticInfoTextView.text = getString(R.string.haptic_description)
    }
    
    private fun testHaptic() {
        hapticStatusTextView.text = getString(R.string.haptic_testing)
        hapticInfoTextView.text = getString(R.string.haptic_question)
        
        // Test các loại rung khác nhau
        HapticManager.buttonClick(this)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            HapticManager.success(this)
        }, 500)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            HapticManager.taskCompleted(this)
        }, 1000)
        
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            updateStatus()
        }, 1500)
    }
}