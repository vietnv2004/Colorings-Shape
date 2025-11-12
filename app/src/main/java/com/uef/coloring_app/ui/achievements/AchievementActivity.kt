package com.uef.coloring_app.ui.achievements

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.AchievementRepository
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.launch

class AchievementActivity : BaseActivity() {
    
    private lateinit var totalAchievementsTextView: TextView
    private lateinit var unlockedAchievementsTextView: TextView
    private lateinit var achievementsRecyclerView: RecyclerView
    private lateinit var achievementRepository: AchievementRepository
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        private const val PREFS_NAME = "user_profile"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievements)
        
        // Hide action bar to avoid duplicate title
        supportActionBar?.hide()
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Initialize repositories
        val database = ColoringDatabase.getDatabase(this)
        achievementRepository = AchievementRepository(database.achievementDao())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        
        initViews()
        loadAchievements()
    }
    
    private fun initViews() {
        totalAchievementsTextView = findViewById(R.id.totalAchievementsTextView)
        unlockedAchievementsTextView = findViewById(R.id.unlockedAchievementsTextView)
        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView)
    }
    
    private fun loadAchievements() {
        lifecycleScope.launch {
            try {
                // Get current user ID
                val userId = sharedPreferences.getString(KEY_CURRENT_USER_ID, null)
                
                // Check if user is logged in
                if (userId == null) {
                    totalAchievementsTextView.text = getString(R.string.total_achievements, 0)
                    unlockedAchievementsTextView.text = getString(R.string.unlocked_achievements, 0)
                    achievementsRecyclerView.layoutManager = LinearLayoutManager(this@AchievementActivity)
                    achievementsRecyclerView.adapter = AchievementAdapter(emptyList())
                    Toast.makeText(this@AchievementActivity, "Vui lòng đăng nhập để xem thành tích.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Get achievement manager để lấy achievements theo user
                val achievementManager = com.uef.coloring_app.core.achievements.AchievementManager(this@AchievementActivity)
                
                // Get real data from database theo user này
                val totalCount = achievementRepository.getAchievementCount()
                val unlockedAchievements = achievementManager.getUserUnlockedAchievements(userId)
                val lockedAchievements = achievementManager.getUserLockedAchievements(userId)
                
                // Combine all achievements (unlocked first, then locked)
                val allAchievements = unlockedAchievements + lockedAchievements
                
                // Update UI with real data
                totalAchievementsTextView.text = getString(R.string.total_achievements, totalCount)
                unlockedAchievementsTextView.text = getString(R.string.unlocked_achievements, unlockedAchievements.size)
                
                // Set up RecyclerView (display achievement points, not user total score)
                achievementsRecyclerView.layoutManager = LinearLayoutManager(this@AchievementActivity)
                achievementsRecyclerView.adapter = AchievementAdapter(allAchievements)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to default values if there's an error
                totalAchievementsTextView.text = getString(R.string.total_achievements, 0)
                unlockedAchievementsTextView.text = getString(R.string.unlocked_achievements, 0)
            }
        }
    }
}
