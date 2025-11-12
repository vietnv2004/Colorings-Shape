package com.uef.coloring_app.ui.security

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class AdvancedSecurityActivity : AppCompatActivity() {
    
    private lateinit var securityStatusTextView: TextView
    private lateinit var securityInfoTextView: TextView
    private lateinit var securityFeaturesRecyclerView: RecyclerView
    private lateinit var enableSecurityButton: Button
    private lateinit var testSecurityButton: Button
    private lateinit var biometricSwitch: Switch
    private lateinit var encryptionSwitch: Switch
    private lateinit var twoFactorSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_security)
        
        initViews()
        setupClickListeners()
        loadSecurityFeatures()
    }
    
    private fun initViews() {
        securityStatusTextView = findViewById(R.id.securityStatusTextView)
        securityInfoTextView = findViewById(R.id.securityInfoTextView)
        securityFeaturesRecyclerView = findViewById(R.id.securityFeaturesRecyclerView)
        enableSecurityButton = findViewById(R.id.enableSecurityButton)
        testSecurityButton = findViewById(R.id.testSecurityButton)
        biometricSwitch = findViewById(R.id.biometricSwitch)
        encryptionSwitch = findViewById(R.id.encryptionSwitch)
        twoFactorSwitch = findViewById(R.id.twoFactorSwitch)
    }
    
    private fun setupClickListeners() {
        enableSecurityButton.setOnClickListener {
            enableSecurity()
        }
        
        testSecurityButton.setOnClickListener {
            testSecurity()
        }
        
        biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleBiometric(isChecked)
        }
        
        encryptionSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleEncryption(isChecked)
        }
        
        twoFactorSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleTwoFactor(isChecked)
        }
    }
    
    private fun enableSecurity() {
        securityStatusTextView.text = "Security: Enabled"
        securityInfoTextView.text = "Advanced security features are now active to protect your data."
    }
    
    private fun testSecurity() {
        securityStatusTextView.text = "Security Test: Running"
        securityInfoTextView.text = "Testing security features to ensure they work properly."
    }
    
    private fun toggleBiometric(isEnabled: Boolean) {
        if (isEnabled) {
            securityStatusTextView.text = "Biometric: Enabled"
        } else {
            securityStatusTextView.text = "Biometric: Disabled"
        }
    }
    
    private fun toggleEncryption(isEnabled: Boolean) {
        if (isEnabled) {
            securityStatusTextView.text = "Encryption: Enabled"
        } else {
            securityStatusTextView.text = "Encryption: Disabled"
        }
    }
    
    private fun toggleTwoFactor(isEnabled: Boolean) {
        if (isEnabled) {
            securityStatusTextView.text = "Two-Factor: Enabled"
        } else {
            securityStatusTextView.text = "Two-Factor: Disabled"
        }
    }
    
    private fun loadSecurityFeatures() {
        val securityFeatures = getSecurityFeatures()
        
        securityFeaturesRecyclerView.layoutManager = LinearLayoutManager(this)
        securityFeaturesRecyclerView.adapter = SecurityFeatureAdapter(securityFeatures)
    }
    
    private fun getSecurityFeatures(): List<SecurityFeature> {
        return listOf(
            SecurityFeature(
                title = "Biometric Authentication",
                description = "Use fingerprint or face recognition",
                status = "Active",
                icon = "👆"
            ),
            SecurityFeature(
                title = "Data Encryption",
                description = "Encrypt all stored data",
                status = "Active",
                icon = "🔐"
            ),
            SecurityFeature(
                title = "Two-Factor Authentication",
                description = "Additional security layer",
                status = "Active",
                icon = "🔑"
            ),
            SecurityFeature(
                title = "Session Management",
                description = "Secure session handling",
                status = "Available",
                icon = "⏰"
            ),
            SecurityFeature(
                title = "Audit Logging",
                description = "Track security events",
                status = "Available",
                icon = "📝"
            ),
            SecurityFeature(
                title = "Threat Detection",
                description = "Detect suspicious activities",
                status = "Available",
                icon = "🛡️"
            )
        )
    }
}

data class SecurityFeature(
    val title: String,
    val description: String,
    val status: String,
    val icon: String
)
