package com.uef.coloring_app.ui.auth

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.local.entity.UserEntity
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.launch
import java.util.UUID

class RegisterActivity : BaseActivity() {
    
    private lateinit var fullNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var birthYearEditText: TextInputEditText
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var confirmPasswordEditText: TextInputEditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var registerButton: MaterialButton
    private lateinit var registerCard: MaterialCardView
    private var googleSignInClient: GoogleSignInClient? = null
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val RC_SIGN_UP_GOOGLE = 9101
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        
        initViews()
        setupClickListeners()
        animateCard()
        initGoogle()
    }
    
    private fun initViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        birthYearEditText = findViewById(R.id.birthYearEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        registerButton = findViewById(R.id.registerButton)
        registerCard = findViewById(R.id.registerCard)

        // Prefill nếu đi từ Google Sign-In
        intent.getStringExtra("prefill_email")?.let { emailEditText.setText(it) }
        intent.getStringExtra("prefill_name")?.let { fullNameEditText.setText(it) }
    }
    
    private fun setupClickListeners() {
        findViewById<ImageButton>(R.id.backButton).setOnClickListenerWithSound {
            finish()
        }
        
        registerButton.setOnClickListenerWithSound {
            if (validateInput()) {
                performRegister()
            }
        }

        // Google sign-up button (nếu có trong layout)
        val googleBtnId = resources.getIdentifier("googleRegisterButton", "id", packageName)
        if (googleBtnId != 0) {
            findViewById<com.google.android.material.button.MaterialButton>(googleBtnId)
                ?.setOnClickListenerWithSound { startGoogleSignUp() }
        }
    }
    
    private fun animateCard() {
        registerCard.alpha = 0f
        registerCard.translationY = 100f
        registerCard.postDelayed({
            registerCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .start()
        }, 200)
    }
    
    private fun validateInput(): Boolean {
        val fullName = fullNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val birthYear = birthYearEditText.text.toString()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()
        
        // Clear errors
        (fullNameEditText.parent.parent as TextInputLayout).error = null
        (emailEditText.parent.parent as TextInputLayout).error = null
        (birthYearEditText.parent.parent as TextInputLayout).error = null
        (passwordEditText.parent.parent as TextInputLayout).error = null
        (confirmPasswordEditText.parent.parent as TextInputLayout).error = null
        
        when {
            fullName.isEmpty() -> {
                (fullNameEditText.parent.parent as TextInputLayout).error = "Vui lòng nhập họ tên"
                return false
            }
            email.isEmpty() -> {
                (emailEditText.parent.parent as TextInputLayout).error = "Vui lòng nhập email"
                return false
            }
            // Cho phép đăng ký với mọi domain email (gmail, uef, ...)
            birthYear.isEmpty() -> {
                (birthYearEditText.parent.parent as TextInputLayout).error = "Vui lòng nhập năm sinh"
                return false
            }
            birthYear.toIntOrNull() == null || birthYear.toInt() < 1950 || birthYear.toInt() > 2010 -> {
                (birthYearEditText.parent.parent as TextInputLayout).error = "Năm sinh không hợp lệ"
                return false
            }
            password.isEmpty() -> {
                (passwordEditText.parent.parent as TextInputLayout).error = "Vui lòng nhập mật khẩu"
                return false
            }
            password.length < 6 -> {
                (passwordEditText.parent.parent as TextInputLayout).error = "Mật khẩu tối thiểu 6 ký tự"
                return false
            }
            confirmPassword != password -> {
                (confirmPasswordEditText.parent.parent as TextInputLayout).error = "Mật khẩu không khớp"
                return false
            }
        }
        
        return true
    }
    
    private fun performRegister() {
        registerButton.isEnabled = false
        registerButton.text = "Đang đăng ký..."

        val fullName = fullNameEditText.text.toString()
        val email = emailEditText.text.toString()
        val birthYear = birthYearEditText.text.toString()
        val password = passwordEditText.text.toString()

        val gender = when (genderRadioGroup.checkedRadioButtonId) {
            R.id.maleRadio -> "male"
            R.id.femaleRadio -> "female"
            R.id.otherRadio -> "other"
            else -> "other"
        }

        // Tạo tài khoản Firebase và gửi email xác thực
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    FirebaseAuth.getInstance().currentUser?.sendEmailVerification()
                    lifecycleScope.launch { saveUserData(fullName, email, birthYear, password, gender) }
                    Toast.makeText(this, "Đăng ký thành công. Vui lòng mở email và bấm link để xác thực, sau đó đăng nhập.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Đăng ký thất bại: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    registerButton.isEnabled = true
                    registerButton.text = getString(R.string.register)
                }
            }
    }
    
    private fun saveUserData(name: String, email: String, birthYear: String, password: String, gender: String) {
        lifecycleScope.launch {
            try {
                // Initialize database
                val database = ColoringDatabase.getDatabase(this@RegisterActivity)
                val userRepository = UserRepository(database.userDao())
                
                // Generate user ID
                val userId = email.replace("@", "_").replace(".", "_")
                
                // Check if user already exists
                val existingUser = userRepository.getUserById(userId)
                if (existingUser != null) {
                    // User already exists, just update SharedPreferences
                    val sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("current_user_id", userId)
                    editor.putString("user_name", name)
                    editor.putString("user_email", email)
                    editor.putString("birth_year", birthYear)
                    editor.putString("gender", gender)
                    editor.putString("user_password", password)
                    editor.putInt("user_level", 1)
                    editor.putInt("user_score", 0)
                    editor.apply()
                    return@launch
                }
                
                // Create new user entity
                val user = UserEntity(
                    id = userId,
                    email = email,
                    name = name,
                    password = password,
                    birthYear = birthYear.toIntOrNull() ?: 2000,
                    gender = when (gender) {
                        "male" -> "Nam"
                        "female" -> "Nữ"
                        else -> "Khác"
                    },
                    role = "participant",
                    isActive = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                
                // Save to database
                userRepository.insertUser(user)
                
                // Save to SharedPreferences
                val sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("current_user_id", userId)
                editor.putString("user_name", name)
                editor.putString("user_email", email)
                editor.putString("birth_year", birthYear)
                editor.putString("gender", gender)
                editor.putString("user_password", password)
                editor.putInt("user_level", 1)
                editor.putInt("user_score", 0)
                editor.apply()
                
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback: save to SharedPreferences only
                val sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                val userId = email.replace("@", "_").replace(".", "_")
                editor.putString("current_user_id", userId)
                editor.putString("user_name", name)
                editor.putString("user_email", email)
                editor.putString("birth_year", birthYear)
                editor.putString("gender", gender)
                editor.putString("user_password", password)
                editor.putInt("user_level", 1)
                editor.putInt("user_score", 0)
                editor.apply()
            }
        }
    }

    private fun initGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @Suppress("DEPRECATION")
    private fun startGoogleSignUp() {
        val intent = googleSignInClient?.signInIntent
        if (intent != null) startActivityForResult(intent, RC_SIGN_UP_GOOGLE)
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_UP_GOOGLE) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                firebaseAuth.signInWithCredential(credential).addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        val email = account.email ?: "user@google.com"
                        val name = account.displayName ?: email.substringBefore("@")
                        lifecycleScope.launch { saveUserData(name, email, "2000", "google", "other") }
                        Toast.makeText(this, "Đăng ký/đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, com.uef.coloring_app.ui.simple.SimpleMainActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Google Sign-In thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: ApiException) {
                Toast.makeText(this, "Google Sign-In lỗi", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

