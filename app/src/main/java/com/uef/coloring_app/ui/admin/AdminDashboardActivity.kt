package com.uef.coloring_app.ui.admin

import android.content.Intent
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.data.repository.TaskRepository
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminDashboardActivity : BaseActivity() {
    
    private lateinit var welcomeTextView: TextView
    private lateinit var taskManagementButton: View
    private lateinit var achievementManagementButton: View
    private lateinit var leaderboardManagementButton: View
    private lateinit var userManagementButton: View
    private lateinit var reportsButton: View
    private lateinit var statisticsRecyclerView: RecyclerView
    
    private lateinit var userRepository: UserRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Guard: only admin can access
        val sharedPreferences: SharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        val role = sharedPreferences.getString("user_role", "participant")
        if (role != "admin") {
            Toast.makeText(this, getString(R.string.access_denied), Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContentView(R.layout.activity_admin_dashboard)
        
        // Initialize repositories
        val database = ColoringDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        taskRepository = TaskRepository(database.taskDao())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        
        initViews()
        setupClickListeners()
        loadStatistics()
    }
    
    private fun initViews() {
        welcomeTextView = findViewById(R.id.welcomeTextView)
        taskManagementButton = findViewById(R.id.taskManagementButton)
        achievementManagementButton = findViewById(R.id.achievementManagementButton)
        leaderboardManagementButton = findViewById(R.id.leaderboardManagementButton)
        userManagementButton = findViewById(R.id.userManagementButton)
        reportsButton = findViewById(R.id.reportsButton)
        statisticsRecyclerView = findViewById(R.id.statisticsRecyclerView)
    }
    
    private fun setupClickListeners() {
        taskManagementButton.setOnClickListenerWithSound {
            startActivity(Intent(this, TaskManagementActivity::class.java))
        }
        
        achievementManagementButton.setOnClickListenerWithSound {
            startActivity(Intent(this, AchievementManagementActivity::class.java))
        }
        
        leaderboardManagementButton.setOnClickListenerWithSound {
            startActivity(Intent(this, LeaderboardManagementActivity::class.java))
        }
        
        userManagementButton.setOnClickListenerWithSound {
            startActivity(Intent(this, UserManagementActivity::class.java))
        }
        
        reportsButton.setOnClickListenerWithSound {
            startActivity(Intent(this, AdvancedAdminActivity::class.java))
        }
    }
    
    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                // Load real statistics from database
                val totalUsers = userRepository.getUserCount()
                val activeTasks = taskRepository.getActiveTasks().size
                
                // Load attempts để tính lượt hoàn thành và điểm trung bình
                val allAttempts = taskAttemptRepository.getTopAttempts(1000)
                val completedAttempts = allAttempts.count { it.isCompleted }
                
                val averageScore = if (allAttempts.isNotEmpty()) {
                    allAttempts.map { it.score }.average().toInt()
                } else {
                    0
                }
                
                // Hiển thị tất cả 4 thống kê
                val statistics = listOf(
                    AdminStatistic(
                        getString(R.string.total_users_label),
                        getString(R.string.users_count_format, totalUsers),
                        "👥",
                        R.color.primary_color
                    ),
                    AdminStatistic(
                        getString(R.string.active_tasks_label),
                        getString(R.string.tasks_count_format, activeTasks),
                        "📋",
                        R.color.warning_color
                    ),
                    AdminStatistic(
                        getString(R.string.completed_attempts_label),
                        getString(R.string.attempts_count_format, completedAttempts),
                        "✅",
                        R.color.success_color
                    ),
                    AdminStatistic(
                        getString(R.string.average_score_label),
                        getString(R.string.points_count_format, averageScore),
                        "⭐",
                        R.color.info_color
                    )
                )
                
                withContext(Dispatchers.Main) {
                    statisticsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this@AdminDashboardActivity)
                    statisticsRecyclerView.adapter = AdminStatisticAdapter(statistics)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminDashboardActivity, getString(R.string.error_loading_statistics), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

data class AdminStatistic(
    val title: String,
    val value: String,
    val icon: String,
    val colorRes: Int
)
