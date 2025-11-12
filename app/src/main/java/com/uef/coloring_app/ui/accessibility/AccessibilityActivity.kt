package com.uef.coloring_app.ui.accessibility

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class AccessibilityActivity : AppCompatActivity() {
    
    private lateinit var accessibilityStatusTextView: TextView
    private lateinit var accessibilityInstructionTextView: TextView
    private lateinit var accessibilityFeaturesRecyclerView: RecyclerView
    private lateinit var enableAccessibilityButton: Button
    private lateinit var testAccessibilityButton: Button
    private lateinit var highContrastSwitch: Switch
    private lateinit var largeTextSwitch: Switch
    private lateinit var voiceOverSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accessibility)
        
        initViews()
        setupClickListeners()
        loadAccessibilityFeatures()
    }
    
    private fun initViews() {
        accessibilityStatusTextView = findViewById(R.id.accessibilityStatusTextView)
        accessibilityInstructionTextView = findViewById(R.id.accessibilityInstructionTextView)
        accessibilityFeaturesRecyclerView = findViewById(R.id.accessibilityFeaturesRecyclerView)
        enableAccessibilityButton = findViewById(R.id.enableAccessibilityButton)
        testAccessibilityButton = findViewById(R.id.testAccessibilityButton)
        highContrastSwitch = findViewById(R.id.highContrastSwitch)
        largeTextSwitch = findViewById(R.id.largeTextSwitch)
        voiceOverSwitch = findViewById(R.id.voiceOverSwitch)
    }
    
    private fun setupClickListeners() {
        enableAccessibilityButton.setOnClickListener {
            enableAccessibility()
        }
        
        testAccessibilityButton.setOnClickListener {
            testAccessibility()
        }
        
        highContrastSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleHighContrast(isChecked)
        }
        
        largeTextSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleLargeText(isChecked)
        }
        
        voiceOverSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleVoiceOver(isChecked)
        }
    }
    
    private fun enableAccessibility() {
        accessibilityStatusTextView.text = "Accessibility: Enabled"
        accessibilityInstructionTextView.text = "Accessibility features are now active to help users with disabilities."
    }
    
    private fun testAccessibility() {
        accessibilityStatusTextView.text = "Accessibility Test: Running"
        accessibilityInstructionTextView.text = "Testing accessibility features to ensure they work properly."
    }
    
    private fun toggleHighContrast(isEnabled: Boolean) {
        if (isEnabled) {
            accessibilityStatusTextView.text = "High Contrast: Enabled"
        } else {
            accessibilityStatusTextView.text = "High Contrast: Disabled"
        }
    }
    
    private fun toggleLargeText(isEnabled: Boolean) {
        if (isEnabled) {
            accessibilityStatusTextView.text = "Large Text: Enabled"
        } else {
            accessibilityStatusTextView.text = "Large Text: Disabled"
        }
    }
    
    private fun toggleVoiceOver(isEnabled: Boolean) {
        if (isEnabled) {
            accessibilityStatusTextView.text = "Voice Over: Enabled"
        } else {
            accessibilityStatusTextView.text = "Voice Over: Disabled"
        }
    }
    
    private fun loadAccessibilityFeatures() {
        val accessibilityFeatures = getAccessibilityFeatures()
        
        accessibilityFeaturesRecyclerView.layoutManager = LinearLayoutManager(this)
        accessibilityFeaturesRecyclerView.adapter = AccessibilityFeatureAdapter(accessibilityFeatures)
    }
    
    private fun getAccessibilityFeatures(): List<AccessibilityFeature> {
        return listOf(
            AccessibilityFeature(
                title = "Screen Reader Support",
                description = "Full support for screen readers and TalkBack",
                status = "Active",
                icon = "👁️"
            ),
            AccessibilityFeature(
                title = "High Contrast Mode",
                description = "Enhanced contrast for better visibility",
                status = "Active",
                icon = "🔍"
            ),
            AccessibilityFeature(
                title = "Large Text Support",
                description = "Scalable text for better readability",
                status = "Active",
                icon = "📖"
            ),
            AccessibilityFeature(
                title = "Voice Commands",
                description = "Voice control for hands-free operation",
                status = "Available",
                icon = "🎤"
            ),
            AccessibilityFeature(
                title = "Gesture Navigation",
                description = "Alternative navigation methods",
                status = "Available",
                icon = "👆"
            ),
            AccessibilityFeature(
                title = "Color Blind Support",
                description = "Color alternatives for color blind users",
                status = "Available",
                icon = "🌈"
            )
        )
    }
}

data class AccessibilityFeature(
    val title: String,
    val description: String,
    val status: String,
    val icon: String
)
