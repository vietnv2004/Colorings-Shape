package com.uef.coloring_app.ui.auth

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
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
import com.uef.coloring_app.ui.simple.SimpleMainActivity
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    
    private lateinit var emailEditText: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordEditText: TextInputEditText
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loginButton: MaterialButton
    private lateinit var registerButton: MaterialButton
    private lateinit var loginCard: MaterialCardView
    private lateinit var logoCard: MaterialCardView
    private var googleSignInClient: GoogleSignInClient? = null
    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val RC_SIGN_IN = 9001
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        initViews()
        setupClickListeners()
        animateViews()

        // Init Google Sign-In
        initGoogleSignIn()
    }
    
    private fun initViews() {
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        registerButton = findViewById(R.id.registerTextView)
        loginCard = findViewById(R.id.loginCard)
        logoCard = findViewById(R.id.logoCard)
        
        // Get TextInputLayouts
        emailInputLayout = emailEditText.parent.parent as TextInputLayout
        passwordInputLayout = passwordEditText.parent.parent as TextInputLayout
    }
    
    private fun setupClickListeners() {
        loginButton.setOnClickListenerWithSound {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            
            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }
        
        registerButton.setOnClickListenerWithSound {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        findViewById<View>(R.id.forgotPasswordTextView)?.setOnClickListenerWithSound {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }

        // Google sign-in (nếu layout có nút này)
        val googleBtnId = resources.getIdentifier("googleSignInButton", "id", packageName)
        if (googleBtnId != 0) {
            findViewById<View>(googleBtnId)?.setOnClickListenerWithSound {
                startGoogleSignIn()
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    @Suppress("DEPRECATION")
    private fun startGoogleSignIn() {
        val intent = googleSignInClient?.signInIntent
        if (intent != null) {
            startActivityForResult(intent, RC_SIGN_IN)
        } else {
            Toast.makeText(this, "Google Sign-In chưa sẵn sàng", Toast.LENGTH_SHORT).show()
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Toast.makeText(this, "Đăng nhập Google thất bại", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    val email = account.email ?: "user@google.com"
                    val name = account.displayName ?: extractNameFromEmail(email)
                    val userId = email.replace("@", "_").replace(".", "_")
                    // Kiểm tra xem đã có tài khoản nội bộ chưa
                    lifecycleScope.launch {
                        try {
                            val database = com.uef.coloring_app.data.local.database.ColoringDatabase.getDatabase(this@LoginActivity)
                            val userRepository = com.uef.coloring_app.data.repository.UserRepository(database.userDao())
                            val existing = userRepository.getUserById(userId)
                            if (existing == null) {
                                // Chưa đăng ký nội bộ: tự động tạo tài khoản mới từ thông tin Google
                                val newUser = com.uef.coloring_app.data.local.entity.UserEntity(
                                    id = userId,
                                    email = email,
                                    name = name,
                                    password = "google_auth", // Password mặc định cho tài khoản Google
                                    birthYear = 2000,
                                    gender = "Khác",
                                    role = "participant",
                                    isActive = true,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                )
                                userRepository.insertUser(newUser)
                                
                                // Lưu thông tin vào SharedPreferences
                                saveUserData(email, "google", name)
                                Toast.makeText(this@LoginActivity, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, SimpleMainActivity::class.java))
                                finish()
                            } else {
                                // Đã có tài khoản, đăng nhập bình thường
                                saveUserData(email, "google", name)
                                Toast.makeText(this@LoginActivity, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                                startActivity(Intent(this@LoginActivity, SimpleMainActivity::class.java))
                                finish()
                            }
                        } catch (_: Exception) {
                            // Nếu có lỗi, vẫn cho vào app như fallback
                            saveUserData(email, "google", name)
                            startActivity(Intent(this@LoginActivity, SimpleMainActivity::class.java))
                            finish()
                        }
                    }
                } else {
                    Toast.makeText(this, "Xác thực Firebase thất bại", Toast.LENGTH_SHORT).show()
                }
            }
    }
    
    private fun animateViews() {
        // Animate logo
        logoCard.alpha = 0f
        logoCard.translationY = -100f
        logoCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
        
        // Animate login card
        loginCard.alpha = 0f
        loginCard.translationY = 100f
        loginCard.postDelayed({
            loginCard.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(600)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
        }, 200)
    }
    
    private fun performLogin(email: String, password: String) {
        // Show loading
        loginButton.isEnabled = false
        loginButton.text = getString(R.string.loading_login)
        
        // Kiểm tra thông tin với database: chỉ cho login khi đã đăng ký
        lifecycleScope.launch {
            try {
                val database = com.uef.coloring_app.data.local.database.ColoringDatabase.getDatabase(this@LoginActivity)
                val userRepository = com.uef.coloring_app.data.repository.UserRepository(database.userDao())
                val userId = email.replace("@", "_").replace(".", "_")
                val user = userRepository.getUserById(userId)

                if (user == null) {
                    Toast.makeText(this@LoginActivity, "Tài khoản chưa đăng ký. Vui lòng đăng ký trước.", Toast.LENGTH_SHORT).show()
                    loginButton.isEnabled = true
                    loginButton.text = getString(R.string.login)
                    return@launch
                }

                if (user.password != password) {
                    Toast.makeText(this@LoginActivity, "Mật khẩu không đúng.", Toast.LENGTH_SHORT).show()
                    loginButton.isEnabled = true
                    loginButton.text = getString(R.string.login)
                    return@launch
                }

                if (email.contains("admin")) {
                    // Bỏ qua kiểm tra Firebase cho tài khoản admin cục bộ
                    val userName = user.name.ifEmpty { extractNameFromEmail(email) }
                    saveUserData(email, password, userName)
                    Toast.makeText(this@LoginActivity, "Welcome Admin! 👨‍💼", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, SimpleMainActivity::class.java))
                    finish()
                } else {
                    // Kiểm tra xác thực email trên Firebase cho user thường
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { signInTask ->
                            if (signInTask.isSuccessful) {
                                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                                if (firebaseUser?.isEmailVerified == true) {
                                    val userName = user.name.ifEmpty { extractNameFromEmail(email) }
                                    saveUserData(email, password, userName)
                                    Toast.makeText(this@LoginActivity, "Welcome! 🎨", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity, SimpleMainActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@LoginActivity, "Email chưa xác thực. Vui lòng mở email và bấm link để xác thực.", Toast.LENGTH_LONG).show()
                                    loginButton.isEnabled = true
                                    loginButton.text = getString(R.string.login)
                                }
                            } else {
                                Toast.makeText(this@LoginActivity, signInTask.exception?.localizedMessage ?: "Không thể đăng nhập Firebase", Toast.LENGTH_LONG).show()
                                loginButton.isEnabled = true
                                loginButton.text = getString(R.string.login)
                            }
                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@LoginActivity, "Có lỗi xảy ra khi đăng nhập.", Toast.LENGTH_SHORT).show()
                loginButton.isEnabled = true
                loginButton.text = getString(R.string.login)
            }
        }
    }
    
    private fun saveUserData(email: String, password: String, userName: String) {
        val sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        
        // Extract username from email if not provided
        val finalUserName = userName.ifEmpty { extractNameFromEmail(email) }
        
        // Generate user ID from email
        val userId = email.replace("@", "_").replace(".", "_")
        
        // Try to save to database first
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            lifecycleScope.launch {
                try {
                    val database = com.uef.coloring_app.data.local.database.ColoringDatabase.getDatabase(this@LoginActivity)
                    val userRepository = com.uef.coloring_app.data.repository.UserRepository(database.userDao())
                    
                    // Check if user exists in database
                    var user = userRepository.getUserById(userId)
                    
                    if (user == null) {
                        // Create new user in database
                        user = com.uef.coloring_app.data.local.entity.UserEntity(
                            id = userId,
                            email = email,
                            name = finalUserName,
                            password = password,
                            birthYear = 2000,
                            gender = "Khác",
                            role = if (email.contains("admin")) "admin" else "participant",
                            isActive = true,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        userRepository.insertUser(user)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Continue with SharedPreferences only
                }
            }
        }
        
        // Save to SharedPreferences
        val editor = sharedPreferences.edit()
        editor.putString("current_user_id", userId)
        editor.putString("user_email", email)
        editor.putString("user_name", finalUserName)
        editor.putString("user_password", password)
        editor.putString("user_role", if (email.contains("admin")) "admin" else "participant")
        editor.putString("gender", "other")
        editor.putString("birth_year", "2000")
        editor.putInt("user_level", 1)
        editor.putInt("user_score", 0)
        editor.apply()
    }
    
    private fun extractNameFromEmail(email: String): String {
        // Extract name from email (e.g., "student1@uef.edu.vn" -> "Student 1")
        val namePart = email.substringBefore("@")
        return namePart.replaceFirstChar { ch ->
            if (ch.isLowerCase()) ch.titlecase(java.util.Locale.getDefault()) else ch.toString()
        }
    }
    
    private fun validateInput(email: String, password: String): Boolean {
        var isValid = true
        
        // Clear previous errors
        emailInputLayout.error = null
        passwordInputLayout.error = null
        
        // Validate email
        when {
            email.isEmpty() -> {
                emailInputLayout.error = getString(R.string.email_required)
                isValid = false
            }
            !email.contains("@") -> {
                emailInputLayout.error = getString(R.string.invalid_email)
                isValid = false
            }
            // Cho phép mọi domain (gmail, uef, ...)
        }
        
        // Validate password
        when {
            password.isEmpty() -> {
                passwordInputLayout.error = getString(R.string.password_required)
                isValid = false
            }
            password.length < 6 -> {
                passwordInputLayout.error = getString(R.string.validation_password)
                isValid = false
            }
        }
        
        return isValid
    }
}