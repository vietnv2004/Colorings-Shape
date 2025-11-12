package com.uef.coloring_app.ui.analytics

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
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : BaseActivity() {
    
    private lateinit var totalTasksTextView: TextView
    private lateinit var averageScoreTextView: TextView
    private lateinit var completionRateTextView: TextView
    private lateinit var timeSpentTextView: TextView
    private lateinit var analyticsRecyclerView: RecyclerView
    
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    private lateinit var sharedPreferences: SharedPreferences
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        
        // Hide action bar to avoid duplicate title
        supportActionBar?.hide()
        
        // Initialize database and repositories
        val database = ColoringDatabase.getDatabase(this)
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        
        initViews()
        loadAnalyticsData()
    }
    
    private fun initViews() {
        totalTasksTextView = findViewById(R.id.totalTasksTextView)
        averageScoreTextView = findViewById(R.id.averageScoreTextView)
        completionRateTextView = findViewById(R.id.completionRateTextView)
        timeSpentTextView = findViewById(R.id.timeSpentTextView)
        analyticsRecyclerView = findViewById(R.id.analyticsRecyclerView)
    }
    
    private fun loadAnalyticsData() {
        lifecycleScope.launch {
            try {
                // Get current user ID from SharedPreferences
                val currentUserId = sharedPreferences.getString("current_user_id", null)
                
                // Check if user is logged in
                if (currentUserId == null) {
                    Toast.makeText(this@AnalyticsActivity, "Vui lòng đăng nhập để xem phân tích.", Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
                
                // Get all attempts for the current user
                val attempts = taskAttemptRepository.getAttemptsByUser(currentUserId)
                
                if (attempts.isEmpty()) {
                    // No data available, show default values
                    loadDefaultData()
                    return@launch
                }
                
                // Calculate statistics
                val totalTasks = attempts.size
                val completedTasks = attempts.count { it.isCompleted }
                val totalScore = attempts.sumOf { it.score }.toFloat()
                val averageScore = if (attempts.isNotEmpty()) totalScore / attempts.size else 0f
                val completionRate = if (attempts.isNotEmpty()) (completedTasks.toFloat() / totalTasks) * 100f else 0f
                
                val totalTimeSpent = attempts.sumOf { it.timeSpent }
                val hours = totalTimeSpent / (1000 * 60 * 60)
                val minutes = (totalTimeSpent % (1000 * 60 * 60)) / (1000 * 60)
                val timeSpentString = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
                
                // Display statistics
                totalTasksTextView.text = totalTasks.toString()
                averageScoreTextView.text = String.format("%.1f", averageScore)
                completionRateTextView.text = String.format("%.0f%%", completionRate)
                timeSpentTextView.text = timeSpentString
                
                // Calculate detailed analytics
                val analyticsData = calculateDetailedAnalytics(attempts)
                
                analyticsRecyclerView.layoutManager = LinearLayoutManager(this@AnalyticsActivity)
                analyticsRecyclerView.adapter = AnalyticsAdapter(analyticsData)
                
            } catch (e: Exception) {
                // Error loading data, show default
                loadDefaultData()
            }
        }
    }
    
    private fun loadDefaultData() {
        totalTasksTextView.text = "0"
        averageScoreTextView.text = "0.0"
        completionRateTextView.text = "0%"
        timeSpentTextView.text = "0m"
        
        val analyticsData = getSampleAnalyticsData()
        analyticsRecyclerView.layoutManager = LinearLayoutManager(this)
        analyticsRecyclerView.adapter = AnalyticsAdapter(analyticsData)
    }
    
    private fun calculateDetailedAnalytics(attempts: List<com.uef.coloring_app.data.local.entity.TaskAttemptEntity>): List<AnalyticsItem> {
        // Calculate daily progress
        val completedAttempts = attempts.filter { it.isCompleted }
        val firstAttempt = completedAttempts.minByOrNull { it.completedAt }
        val lastAttempt = completedAttempts.maxByOrNull { it.completedAt }
        
        val daysDiff = if (firstAttempt != null && lastAttempt != null) {
            val diff = lastAttempt.completedAt - firstAttempt.completedAt
            maxOf(diff / (1000 * 60 * 60 * 24), 1)
        } else {
            1
        }
        val dailyProgress = completedAttempts.size.toFloat() / daysDiff.toInt()
        
        // Calculate accuracy rate
        val averageAccuracy = completedAttempts.mapNotNull { it.accuracy }.average().toFloat()
        
        // Calculate speed improvement (dummy for now)
        val speedImprovement = "12"
        
        // Calculate streak days
        val streakDays = calculateStreakDays(completedAttempts)
        
        // Favorite shape (dummy for now)
        val favoriteShape = getString(R.string.circle)
        
        // Peak performance time
        val peakPerformance = calculatePeakPerformanceTime(completedAttempts)
        
        return listOf(
            AnalyticsItem(
                title = getString(R.string.daily_progress),
                description = getString(R.string.daily_progress_desc),
                value = String.format("%.1f", dailyProgress),
                unit = getString(R.string.tasks_per_day),
                trend = "+15%"
            ),
            AnalyticsItem(
                title = getString(R.string.accuracy_rate),
                description = getString(R.string.accuracy_rate_desc),
                value = String.format("%.1f", averageAccuracy * 100),
                unit = "%",
                trend = "+5%"
            ),
            AnalyticsItem(
                title = getString(R.string.speed_improvement),
                description = getString(R.string.speed_improvement_desc),
                value = speedImprovement,
                unit = getString(R.string.minutes),
                trend = "+8%"
            ),
            AnalyticsItem(
                title = getString(R.string.streak_days),
                description = getString(R.string.streak_days_desc),
                value = streakDays.toString(),
                unit = getString(R.string.days),
                trend = "🔥"
            ),
            AnalyticsItem(
                title = getString(R.string.favorite_shape),
                description = getString(R.string.favorite_shape_desc),
                value = favoriteShape,
                unit = "",
                trend = "⭐"
            ),
            AnalyticsItem(
                title = getString(R.string.peak_performance),
                description = getString(R.string.peak_performance_desc),
                value = peakPerformance,
                unit = "",
                trend = "📈"
            )
        )
    }
    
    private fun calculateStreakDays(attempts: List<com.uef.coloring_app.data.local.entity.TaskAttemptEntity>): Int {
        if (attempts.isEmpty()) return 0
        
        val attemptsByDay = attempts.groupBy {
            Date(it.completedAt).let { date ->
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            }
        }
        
        var streak = 1
        val sortedDays = attemptsByDay.keys.sortedDescending()
        
        for (i in 1 until sortedDays.size) {
            val prevDay = sortedDays[i - 1]
            val currentDay = sortedDays[i]
            
            if (areConsecutiveDays(prevDay, currentDay)) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }
    
    private fun areConsecutiveDays(date1: String, date2: String): Boolean {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val day1 = sdf.parse(date1)
            val day2 = sdf.parse(date2)
            if (day1 == null || day2 == null) return false
            val diffInDays = (day1.time - day2.time) / (1000 * 60 * 60 * 24)
            return diffInDays == 1L
        } catch (e: Exception) {
            return false
        }
    }
    
    private fun calculatePeakPerformanceTime(attempts: List<com.uef.coloring_app.data.local.entity.TaskAttemptEntity>): String {
        if (attempts.isEmpty()) return "N/A"
        
        val attemptsByHour = attempts.groupBy {
            Date(it.completedAt).let { date ->
                SimpleDateFormat("HH", Locale.getDefault()).format(date)
            }
        }
        
        val hourWithMostAttempts = attemptsByHour.maxByOrNull { it.value.size }?.key ?: "14"
        val hour = hourWithMostAttempts.toInt()
        val format = SimpleDateFormat("h:mm a", Locale.getDefault())
        
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, 0)
            format.format(calendar.time)
        } catch (e: Exception) {
            "2:00 PM"
        }
    }
    
    private fun getSampleAnalyticsData(): List<AnalyticsItem> {
        return listOf(
            AnalyticsItem(
                title = getString(R.string.daily_progress),
                description = getString(R.string.daily_progress_desc),
                value = "3.2",
                unit = getString(R.string.tasks_per_day),
                trend = "+15%"
            ),
            AnalyticsItem(
                title = getString(R.string.accuracy_rate),
                description = getString(R.string.accuracy_rate_desc),
                value = "89.3",
                unit = "%",
                trend = "+5%"
            ),
            AnalyticsItem(
                title = getString(R.string.speed_improvement),
                description = getString(R.string.speed_improvement_desc),
                value = "12",
                unit = getString(R.string.minutes),
                trend = "+8%"
            ),
            AnalyticsItem(
                title = getString(R.string.streak_days),
                description = getString(R.string.streak_days_desc),
                value = "7",
                unit = getString(R.string.days),
                trend = "🔥"
            ),
            AnalyticsItem(
                title = getString(R.string.favorite_shape),
                description = getString(R.string.favorite_shape_desc),
                value = getString(R.string.circle),
                unit = "",
                trend = "⭐"
            ),
            AnalyticsItem(
                title = getString(R.string.peak_performance),
                description = getString(R.string.peak_performance_desc),
                value = "2:00 PM",
                unit = "",
                trend = "📈"
            )
        )
    }
}

data class AnalyticsItem(
    val title: String,
    val description: String,
    val value: String,
    val unit: String,
    val trend: String
)
