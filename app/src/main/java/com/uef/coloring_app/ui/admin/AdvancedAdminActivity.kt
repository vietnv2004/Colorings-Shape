package com.uef.coloring_app.ui.admin

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.AchievementRepository
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.data.repository.TaskRepository
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AdvancedAdminActivity : BaseActivity() {
    
    private lateinit var totalUsersTextView: TextView
    private lateinit var activeTasksTextView: TextView
    private lateinit var completedAttemptsTextView: TextView
    private lateinit var averageScoreTextView: TextView
    private lateinit var reportsRecyclerView: RecyclerView
    
    private lateinit var userRepository: UserRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    private lateinit var achievementRepository: AchievementRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_admin)
        
        // Hide action bar
        supportActionBar?.hide()
        
        // Initialize repositories
        val database = ColoringDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        taskRepository = TaskRepository(database.taskDao())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        achievementRepository = AchievementRepository(database.achievementDao())
        
        initViews()
        loadStatistics()
        loadReports()
    }
    
    private fun initViews() {
        totalUsersTextView = findViewById(R.id.totalUsersTextView)
        activeTasksTextView = findViewById(R.id.activeTasksTextView)
        completedAttemptsTextView = findViewById(R.id.completedAttemptsTextView)
        averageScoreTextView = findViewById(R.id.averageScoreTextView)
        reportsRecyclerView = findViewById(R.id.reportsRecyclerView)
    }
    
    private fun loadStatistics() {
        lifecycleScope.launch {
            try {
                // Get real statistics from database
                val totalUsers = userRepository.getUserCount()
                val activeTasks = taskRepository.getActiveTasks().size
                val allAttempts = taskAttemptRepository.getTopAttempts(1000)
                
                // Calculate statistics
                val completedCount = allAttempts.count { it.isCompleted }
                
                val averageScore = if (allAttempts.isNotEmpty()) {
                    allAttempts.map { it.score }.average()
                } else 0.0
                
                // Update UI
                totalUsersTextView.text = totalUsers.toString()
                activeTasksTextView.text = activeTasks.toString()
                completedAttemptsTextView.text = completedCount.toString()
                averageScoreTextView.text = String.format("%.1f", averageScore)
                
            } catch (e: Exception) {
                e.printStackTrace()
                totalUsersTextView.text = "0"
                activeTasksTextView.text = "0"
                completedAttemptsTextView.text = "0"
                averageScoreTextView.text = "0.0"
            }
        }
    }
    
    private fun loadReports() {
        lifecycleScope.launch {
            try {
                val basicReports = calculateReports()
                val detailedReports = calculateDetailedReports(basicReports)
                
                reportsRecyclerView.layoutManager = LinearLayoutManager(this@AdvancedAdminActivity)
                reportsRecyclerView.adapter = AdminReportAdapter(detailedReports)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to sample data
                val sampleReports = getSampleReports()
                reportsRecyclerView.layoutManager = LinearLayoutManager(this@AdvancedAdminActivity)
                reportsRecyclerView.adapter = AdminReportAdapter(sampleReports)
            }
        }
    }
    
    private suspend fun calculateReports(): List<AdminReport> {
        val allAttempts = taskAttemptRepository.getTopAttempts(1000)
        val allTasks = taskRepository.getAllTasks().first() // Get first value from Flow
        
        // Calculate completion rate
        val totalAttempts = allAttempts.size
        val completedAttempts = allAttempts.count { it.isCompleted }
        val completionRate = if (totalAttempts > 0) {
            ((completedAttempts.toFloat() / totalAttempts) * 100).toInt()
        } else 0
        
        // Calculate average session duration
        val avgSessionDuration = if (allAttempts.isNotEmpty()) {
            allAttempts.map { it.timeSpent }.average() / 1000.0 / 60.0 // Convert to minutes
        } else 0.0
        
        // Find top performing task
        val taskCompletionCounts = allAttempts
            .filter { it.isCompleted }
            .groupBy { it.taskId }
            .mapValues { it.value.size }
        
        val topTaskId = taskCompletionCounts.maxByOrNull { it.value }?.key
        val topTaskName = topTaskId?.let { taskId ->
            allTasks.find { it.id == taskId }?.name ?: getString(R.string.not_available)
        } ?: getString(R.string.no_data)
        
        val topTaskCompletionCount = topTaskId?.let { taskCompletionCounts[it] ?: 0 } ?: 0
        val topTaskPercentage = if (totalAttempts > 0) {
            ((topTaskCompletionCount.toFloat() / totalAttempts) * 100).toInt()
        } else 0
        
        return listOf(
            AdminReport(
                title = getString(R.string.task_completion_rate_title),
                description = getString(R.string.task_completion_desc),
                value = "$completionRate%",
                trend = if (completionRate >= 80) getString(R.string.status_good) else if (completionRate >= 50) getString(R.string.status_average) else getString(R.string.status_need_improve)
            ),
            AdminReport(
                title = getString(R.string.avg_session_duration),
                description = getString(R.string.avg_session_desc),
                value = String.format("%.1f %s", avgSessionDuration, getString(R.string.minutes)),
                trend = if (avgSessionDuration >= 10) getString(R.string.status_active) else getString(R.string.status_stable)
            ),
            AdminReport(
                title = getString(R.string.top_performing_tasks_title),
                description = getString(R.string.top_tasks_desc),
                value = topTaskName,
                trend = getString(R.string.completion_percent, topTaskPercentage)
            ),
            AdminReport(
                title = getString(R.string.total_attempts),
                description = getString(R.string.total_attempts_desc),
                value = "$totalAttempts ${getString(R.string.attempts)}",
                trend = getString(R.string.completed_attempts_trend, completedAttempts)
            )
        )
    }
    
    private suspend fun calculateDetailedReports(basicReports: List<AdminReport>): List<AdminReport> {
        val detailedReports = mutableListOf<AdminReport>()
        detailedReports.addAll(basicReports)
        
        // Lấy dữ liệu từ database
        val allAttempts = taskAttemptRepository.getTopAttempts(1000)
        val allAchievements = achievementRepository.getUnlockedAchievements()
        val allUsers = userRepository.getAllUsers().first()
        
        // 1. Phân tích điểm số
        val scoreRanges = mapOf(
            getString(R.string.score_excellent) to allAttempts.count { it.score >= 90 },
            getString(R.string.score_good) to allAttempts.count { it.score in 70..89 },
            getString(R.string.score_fair) to allAttempts.count { it.score in 50..69 },
            getString(R.string.score_average) to allAttempts.count { it.score < 50 }
        )
        val topScoreRange = scoreRanges.maxByOrNull { it.value }
        detailedReports.add(AdminReport(
            title = getString(R.string.score_distribution),
            description = getString(R.string.score_distribution_desc),
            value = topScoreRange?.let { "${it.value} ${getString(R.string.attempts)} - ${it.key}" } ?: getString(R.string.no_data),
            trend = getString(R.string.total_format, allAttempts.count())
        ))
        
        // 2. Người dùng tích cực nhất
        val userActivity = allAttempts.groupBy { it.userId }
            .mapValues { it.value.size }
        val mostActiveUserId = userActivity.maxByOrNull { it.value }?.key
        val mostActiveUserName = mostActiveUserId?.let { allUsers.find { it.id == mostActiveUserId }?.name ?: getString(R.string.not_available) } ?: getString(R.string.not_available)
        val mostActiveCount = mostActiveUserId?.let { userActivity[it] ?: 0 } ?: 0
        detailedReports.add(AdminReport(
            title = getString(R.string.most_active_user),
            description = getString(R.string.most_active_user_desc),
            value = mostActiveUserName,
            trend = getString(R.string.attempts_completed, mostActiveCount)
        ))
        
        // 3. Thống kê achievements
        val totalAchievements = achievementRepository.getAchievementCount()
        val unlockedCount = allAchievements.size
        detailedReports.add(AdminReport(
            title = getString(R.string.achievements_stat),
            description = getString(R.string.achievements_stat_desc),
            value = "$unlockedCount/$totalAchievements",
            trend = if (totalAchievements > 0) getString(R.string.unlocked_percent, (unlockedCount * 100 / totalAchievements)) else "0%"
        ))
        
        // 4. Tốc độ hoàn thành trung bình
        val avgCompletionTime = if (allAttempts.isNotEmpty()) {
            allAttempts.map { it.timeSpent }.average() / 1000.0 // Convert to seconds
        } else 0.0
        detailedReports.add(AdminReport(
            title = getString(R.string.avg_completion_speed),
            description = getString(R.string.avg_completion_speed_desc),
            value = String.format("%.1f %s", avgCompletionTime, getString(R.string.seconds)),
            trend = if (avgCompletionTime < 120) getString(R.string.status_fast) else if (avgCompletionTime < 240) getString(R.string.status_medium) else getString(R.string.status_slow)
        ))
        
        // 5. Tỷ lệ chưa hoàn thành
        val incompleteRate = if (allAttempts.isNotEmpty()) {
            ((allAttempts.count { !it.isCompleted }.toFloat() / allAttempts.size) * 100).toInt()
        } else 0
        detailedReports.add(AdminReport(
            title = getString(R.string.incomplete_rate),
            description = getString(R.string.incomplete_rate_desc),
            value = "$incompleteRate%",
            trend = if (incompleteRate < 20) getString(R.string.status_good) else if (incompleteRate < 50) getString(R.string.status_average) else getString(R.string.status_high)
        ))
        
        // 6. Tăng trưởng người dùng (7 ngày qua)
        val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        val newUsersLastWeek = allUsers.count { it.createdAt >= sevenDaysAgo }
        detailedReports.add(AdminReport(
            title = getString(R.string.new_users_7days),
            description = getString(R.string.new_users_7days_desc),
            value = getString(R.string.users_count, newUsersLastWeek),
            trend = if (newUsersLastWeek > 0) getString(R.string.status_growth) else getString(R.string.status_no_data)
        ))
        
        return detailedReports
    }
    
    private fun getSampleReports(): List<AdminReport> {
        return listOf(
            AdminReport(
                title = getString(R.string.task_completion_rate_title),
                description = getString(R.string.task_completion_desc),
                value = "0%",
                trend = getString(R.string.no_data)
            ),
            AdminReport(
                title = getString(R.string.avg_session_duration),
                description = getString(R.string.avg_session_desc),
                value = "0.0 " + getString(R.string.minutes),
                trend = getString(R.string.no_data)
            )
        )
    }
}

data class AdminReport(
    val title: String,
    val description: String,
    val value: String,
    val trend: String
)
