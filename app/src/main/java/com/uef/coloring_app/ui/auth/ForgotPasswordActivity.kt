package com.uef.coloring_app.ui.auth

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.ui.common.BaseActivity

class ForgotPasswordActivity : BaseActivity() {
    
    private lateinit var emailEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var resetButton: MaterialButton
    private lateinit var resetCard: MaterialCardView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        
        initViews()
        setupClickListeners()
        animateCard()
    }
    
    private fun initViews() {
        emailEditText = findViewById(R.id.emailEditText)
        emailInputLayout = findViewById(R.id.emailInputLayout)
        resetButton = findViewById(R.id.resetButton)
        resetCard = findViewById(R.id.resetCard)
    }
    
    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListenerWithSound {
            finish()
        }
        
        findViewById<TextView>(R.id.backToLoginText).setOnClickListenerWithSound {
            finish()
        }
        
        resetButton.setOnClickListenerWithSound {
            if (validateEmail()) {
                sendResetInstructions()
            }
        }
    }
    
    private fun animateCard() {
        resetCard.alpha = 0f
        resetCard.translationY = 100f
        resetCard.postDelayed({
            resetCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .start()
        }, 200)
    }
    
    private fun validateEmail(): Boolean {
        val email = emailEditText.text.toString()
        
        emailInputLayout.error = null
        
        when {
            email.isEmpty() -> {
                emailInputLayout.error = "Vui lòng nhập email"
                return false
            }
            !email.contains("@uef.edu.vn") -> {
                emailInputLayout.error = "Vui lòng sử dụng email UEF"
                return false
            }
        }
        
        return true
    }
    
    private fun sendResetInstructions() {
        resetButton.isEnabled = false
        resetButton.text = "Đang gửi..."
        
        resetButton.postDelayed({
            Toast.makeText(this, "Hướng dẫn đã được gửi đến email! 📧", Toast.LENGTH_LONG).show()
            finish()
        }, 1500)
    }
}


