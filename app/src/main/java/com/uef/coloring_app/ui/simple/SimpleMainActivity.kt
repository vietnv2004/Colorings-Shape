package com.uef.coloring_app.ui.simple

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.uef.coloring_app.R
import com.uef.coloring_app.ColoringApplication
import com.uef.coloring_app.core.haptic.HapticManager
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.ui.common.BaseActivity

class SimpleMainActivity : BaseActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    internal var isAdmin = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setContentView
        applyCurrentTheme()
        
        setContentView(R.layout.activity_simple_main)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        
        // Check if user is admin
        isAdmin = checkIfAdmin()
        
        // Hide action bar to avoid duplicate title
        supportActionBar?.hide()
        
        // Sử dụng SoundManager từ Application
        ColoringApplication.soundManager.startAutoPlay()
        
        setupViewPager()
        setupBottomNavigation()
        setupHeaderButtons()
    }
    
    private fun checkIfAdmin(): Boolean {
        val role = sharedPreferences.getString("user_role", "participant")
        return role == "admin"
    }
    
    private fun applyCurrentTheme() {
        val sharedPreferences = getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
        val themeMode = sharedPreferences.getString("theme_mode", "light") ?: "light"
        
        when (themeMode) {
            "tet" -> setTheme(R.style.Theme_ColoringShapes_Tet)
            else -> setTheme(R.style.Theme_ColoringShapes)
        }
    }
    
    private fun setupViewPager() {
        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
        
        val adapter = SimplePagerAdapter(this)
        viewPager.adapter = adapter
    }
    
    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        val viewPager = findViewById<androidx.viewpager2.widget.ViewPager2>(R.id.viewPager)
        
        // Handle bottom navigation item selection
        bottomNavigation.setOnItemSelectedListener { item ->
            HapticManager.buttonClick(this)
            ColoringApplication.soundManager.playButtonClickSound()
            
            // All tabs navigate to ViewPager pages
            when (item.itemId) {
                R.id.nav_tasks -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_history -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_leaderboard -> {
                    viewPager.currentItem = 2
                    true
                }
                R.id.nav_achievements -> {
                    viewPager.currentItem = 3
                    true
                }
                R.id.nav_profile -> {
                    viewPager.currentItem = 4
                    true
                }
                else -> false
            }
        }
        
        // Handle viewpager page changes
        viewPager.registerOnPageChangeCallback(object : androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val menu = bottomNavigation.menu
                
                when (position) {
                    0 -> menu.findItem(R.id.nav_tasks).isChecked = true
                    1 -> menu.findItem(R.id.nav_history).isChecked = true
                    2 -> menu.findItem(R.id.nav_leaderboard).isChecked = true
                    3 -> menu.findItem(R.id.nav_achievements).isChecked = true
                    4 -> menu.findItem(R.id.nav_profile).isChecked = true
                }
            }
        })
    }
    
    private fun setupHeaderButtons() {
        // Setup Chat AI button
        val chatAiContainer = findViewById<android.view.View>(R.id.chatAiContainer)
        chatAiContainer.setOnClickListenerWithSound {
            HapticManager.buttonClick(this)
            openChatAI()
        }
        
        // Setup Management button (admin only)
        val managementContainer = findViewById<android.view.View>(R.id.managementContainer)
        if (isAdmin) {
            managementContainer.visibility = android.view.View.VISIBLE
            managementContainer.setOnClickListenerWithSound {
                HapticManager.buttonClick(this)
                openManagement()
            }
        } else {
            managementContainer.visibility = android.view.View.GONE
        }
    }
    
    private fun openChatAI() {
        val intent = Intent(this, com.uef.coloring_app.ui.chat.ChatAIActivity::class.java)
        startActivity(intent)
    }
    
    private fun openManagement() {
        // Open management activity directly
        val intent = Intent(this, com.uef.coloring_app.ui.admin.AdminDashboardActivity::class.java)
        startActivity(intent)
    }
}

class SimplePagerAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    
    override fun getItemCount(): Int = 5

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> SimpleTaskFragment()
            1 -> SimpleHistoryFragment()
            2 -> SimpleLeaderboardFragment()
            3 -> SimpleAchievementFragment() // Always show achievements for everyone
            4 -> SimpleProfileFragment()
            else -> SimpleTaskFragment()
        }
    }
}