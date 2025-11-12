package com.uef.coloring_app.ui.leaderboard

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.data.repository.TaskRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.launch

class LeaderboardActivity : BaseActivity() {
    
    private lateinit var leaderboardRecyclerView: RecyclerView
    private lateinit var noDataTextView: TextView
    private lateinit var userRepository: UserRepository
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    private lateinit var taskRepository: TaskRepository
    
    // Filter mode
    private var filterMode: FilterMode = FilterMode.TOTAL_SCORE
    
    enum class FilterMode {
        TOTAL_SCORE,    // Điểm tổng
        DRAWING_SCORE   // Điểm hình vẽ
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)
        
        // Hide action bar
        supportActionBar?.hide()
        
        // Initialize repositories
        val database = ColoringDatabase.getDatabase(applicationContext)
        userRepository = UserRepository(database.userDao())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        taskRepository = TaskRepository(database.taskDao())
        
        initViews()
        loadLeaderboard()
    }
    
    private fun initViews() {
        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView)
        noDataTextView = findViewById(R.id.noDataTextView)
        
        leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)
        
        // Setup filter buttons
        val btnTotalScore = findViewById<MaterialButton>(R.id.btnTotalScore)
        val btnDrawingScore = findViewById<MaterialButton>(R.id.btnDrawingScore)
        
        btnTotalScore.setOnClickListener {
            filterMode = FilterMode.TOTAL_SCORE
            updateButtonStates(btnTotalScore, btnDrawingScore)
            loadLeaderboard()
        }
        
        btnDrawingScore.setOnClickListener {
            filterMode = FilterMode.DRAWING_SCORE
            updateButtonStates(btnDrawingScore, btnTotalScore)
            loadLeaderboard()
        }
        
        // Set initial button state
        updateButtonStates(btnTotalScore, btnDrawingScore)
    }
    
    private fun updateButtonStates(selectedButton: MaterialButton, unselectedButton: MaterialButton) {
        selectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.primary_color))
        selectedButton.setTextColor(ContextCompat.getColor(this, android.R.color.white))
        
        unselectedButton.setBackgroundColor(ContextCompat.getColor(this, R.color.card_background))
        unselectedButton.setTextColor(ContextCompat.getColor(this, R.color.primary_color))
    }
    
    private fun loadLeaderboard() {
        lifecycleScope.launch {
            try {
                val leaderboardItems = when (filterMode) {
                    FilterMode.TOTAL_SCORE -> loadTotalScoreLeaderboard()
                    FilterMode.DRAWING_SCORE -> loadDrawingScoreLeaderboard()
                }
                
                // Update UI
                if (leaderboardItems.isEmpty()) {
                    noDataTextView.visibility = TextView.VISIBLE
                    leaderboardRecyclerView.visibility = RecyclerView.GONE
                } else {
                    noDataTextView.visibility = TextView.GONE
                    leaderboardRecyclerView.visibility = RecyclerView.VISIBLE
                    leaderboardRecyclerView.adapter = LeaderboardAdapter(leaderboardItems)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                noDataTextView.visibility = TextView.VISIBLE
                noDataTextView.text = getString(R.string.leaderboard_load_error, e.message ?: getString(R.string.unknown_error))
                leaderboardRecyclerView.visibility = RecyclerView.GONE
            }
        }
    }
    
    /**
     * Load leaderboard by total score (sum of all attempts)
     */
    private suspend fun loadTotalScoreLeaderboard(): List<LeaderboardItem> {
        // Get ALL attempts (không giới hạn)
        val database = ColoringDatabase.getDatabase(this)
        val allAttempts = database.taskAttemptDao().getTopAttempts(10000) // Lấy nhiều để không bị giới hạn
            .filter { it.isCompleted }
        
        // Group by user and calculate total scores
        val userScores = allAttempts.groupBy { it.userId }
            .mapValues { (_, attempts) -> attempts.sumOf { it.score } }
            .entries
            .sortedByDescending { it.value }
        
        // Convert to leaderboard items
        return userScores.mapIndexed { index, (userId, score) ->
            val user = userRepository.getUserById(userId)
            
            val userName = when {
                // 1. Ưu tiên: Tên từ database
                !user?.name.isNullOrEmpty() && user!!.name.trim() != "" -> user.name.trim()
                
                // 2. Fallback: Parse từ User ID (lọc bỏ các từ không cần thiết)
                userId.contains("_") || userId.contains(".") -> {
                    val parts = userId.replace("_", " ").replace(".", " ")
                        .split(" ")
                        .filter { it.isNotEmpty() }
                    
                    // Lọc bỏ các từ không cần thiết
                    val filteredParts = parts.filter { part ->
                        val lowerPart = part.lowercase()
                        !lowerPart.contains("uef") && 
                        !lowerPart.contains("edu") && 
                        !lowerPart.contains("vn") &&
                        lowerPart.length > 1
                    }
                    
                    // Lấy phần đầu tiên còn lại
                    if (filteredParts.isNotEmpty()) {
                        filteredParts.first().replaceFirstChar { it.uppercaseChar() }
                    } else {
                        parts.firstOrNull()?.replaceFirstChar { it.uppercaseChar() } ?: userId.replaceFirstChar { it.uppercaseChar() }
                    }
                }
                
                // 3. Fallback cuối cùng: User ID đơn giản
                else -> userId.replaceFirstChar { it.uppercaseChar() }
            }
            
            LeaderboardItem(
                rank = index + 1,
                userId = userId,
                userName = userName,
                taskId = "",
                taskName = "Điểm tổng",
                score = score,
                timeSpent = 0,
                completedAt = java.util.Date()
            )
        }
    }
    
    /**
     * Load leaderboard by drawing score (best individual attempt)
     */
    private suspend fun loadDrawingScoreLeaderboard(): List<LeaderboardItem> {
        // Get ALL attempts sorted by score
        val database = ColoringDatabase.getDatabase(this)
        val allAttempts = database.taskAttemptDao().getTopAttempts(10000) // Không giới hạn
            .filter { it.isCompleted }
        
        // Convert to leaderboard items
        return allAttempts.mapIndexed { index, attempt ->
            val user = userRepository.getUserById(attempt.userId)
            
            val userName = when {
                // 1. Ưu tiên: Tên từ database
                !user?.name.isNullOrEmpty() && user!!.name.trim() != "" -> user.name.trim()
                
                // 2. Fallback: Parse từ User ID (lọc bỏ các từ không cần thiết)
                attempt.userId.contains("_") || attempt.userId.contains(".") -> {
                    val parts = attempt.userId.replace("_", " ").replace(".", " ")
                        .split(" ")
                        .filter { it.isNotEmpty() }
                    
                    // Lọc bỏ các từ không cần thiết
                    val filteredParts = parts.filter { part ->
                        val lowerPart = part.lowercase()
                        !lowerPart.contains("uef") && 
                        !lowerPart.contains("edu") && 
                        !lowerPart.contains("vn") &&
                        lowerPart.length > 1
                    }
                    
                    // Lấy phần đầu tiên còn lại
                    if (filteredParts.isNotEmpty()) {
                        filteredParts.first().replaceFirstChar { it.uppercaseChar() }
                    } else {
                        parts.firstOrNull()?.replaceFirstChar { it.uppercaseChar() } ?: attempt.userId.replaceFirstChar { it.uppercaseChar() }
                    }
                }
                
                // 3. Fallback cuối cùng: User ID đơn giản
                else -> attempt.userId.replaceFirstChar { it.uppercaseChar() }
            }
            
            val task = taskRepository.getTaskById(attempt.taskId)
            val taskName = task?.name ?: getString(R.string.task_prefix, attempt.taskId.take(8))
            
            LeaderboardItem(
                rank = index + 1,
                userId = attempt.userId,
                userName = userName,
                taskId = attempt.taskId,
                taskName = taskName,
                score = attempt.score,
                timeSpent = attempt.timeSpent,
                completedAt = java.util.Date(attempt.completedAt)
            )
        }
    }
}

data class LeaderboardItem(
    val rank: Int,
    val userId: String,
    val userName: String,
    val taskId: String,
    val taskName: String,
    val score: Int,
    val timeSpent: Long,
    val completedAt: java.util.Date
)