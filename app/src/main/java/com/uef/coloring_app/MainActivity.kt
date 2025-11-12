package com.uef.coloring_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.uef.coloring_app.ui.auth.LoginActivity
import com.uef.coloring_app.ui.simple.SimpleMainActivity
import com.uef.coloring_app.ui.common.BaseActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.data.local.entity.UserEntity

class MainActivity : BaseActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Sử dụng SoundManager từ Application
        ColoringApplication.soundManager.startAutoPlay()
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        
        // Seed tài khoản admin mặc định nếu chưa có
        seedDefaultAdmin()

        // Check if user is already logged in
        checkLoginStatus()
    }
    
    private fun checkLoginStatus() {
        val userEmail = sharedPreferences.getString("user_email", null)
        val userPassword = sharedPreferences.getString("user_password", null)
        
        // If user has saved credentials, auto-login
        if (!userEmail.isNullOrEmpty() && !userPassword.isNullOrEmpty()) {
            // Navigate directly to main activity
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, SimpleMainActivity::class.java))
                finish()
            }, 500) // Small delay for smooth transition
        } else {
            // No saved credentials, go to login screen
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }, 500)
        }
    }

    private fun seedDefaultAdmin() {
        lifecycleScope.launch {
            try {
                val db = ColoringDatabase.getDatabase(this@MainActivity)
                val userRepo = UserRepository(db.userDao())
                val adminEmail = "admin@uef.edu.vn"
                val adminId = adminEmail.replace("@", "_").replace(".", "_")
                val existing = userRepo.getUserById(adminId)
                if (existing == null) {
                    val adminUser = UserEntity(
                        id = adminId,
                        email = adminEmail,
                        name = "Administrator",
                        password = "admin123",
                        birthYear = 2000,
                        gender = "Khác",
                        role = "admin",
                        isActive = true,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    userRepo.insertUser(adminUser)
                }
            } catch (_: Exception) { }
        }
    }
}
