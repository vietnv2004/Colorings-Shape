package com.uef.coloring_app.ui.simple

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.data.repository.TaskRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SimpleHistoryFragment : Fragment() {
    
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_history, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Initialize repositories and SharedPreferences
        val database = ColoringDatabase.getDatabase(requireContext())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        taskRepository = TaskRepository(database.taskDao())
        sharedPreferences = requireContext().getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        
        // Load recent attempts
        loadRecentAttempts()
    }
    
    private fun loadRecentAttempts() {
        lifecycleScope.launch {
            try {
                // Get current user ID from SharedPreferences
                val currentUserId = sharedPreferences.getString("current_user_id", null)
                
                // Check if user is logged in
                if (currentUserId == null) {
                    Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem lịch sử.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Get all attempts for the current user
                val allUserAttempts = taskAttemptRepository.getAttemptsByUser(currentUserId)
                
                // Filter only completed attempts and sort by completedAt (most recent first)
                val completedAttempts = allUserAttempts
                    .filter { it.isCompleted }
                    .sortedByDescending { it.completedAt }
                
                // Convert to SimpleHistoryItem with actual task information
                val historyItems = completedAttempts.map { attempt ->
                    // Get task information
                    val task = taskRepository.getTaskById(attempt.taskId)
                    val taskName = task?.name ?: "Nhiệm vụ #${attempt.taskId.take(8)}"
                    val shapeType = task?.shapeId ?: "circle"
                    
                    val timeInMinutes = (attempt.timeSpent / 1000 / 60).toInt()
                    val timeInSeconds = ((attempt.timeSpent / 1000) % 60).toInt()
                    val timeSpent = String.format("%d:%02d", timeInMinutes, timeInSeconds)
                    
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateFormat.format(Date(attempt.completedAt))
                    
                    // Calculate real rank from leaderboard
                    val rank = calculateUserRank(attempt.userId)
                    
                    SimpleHistoryItem(
                        taskName = taskName,
                        shapeType = shapeType,
                        score = attempt.score,
                        timeSpent = timeSpent,
                        date = date,
                        rank = rank,
                        imagePath = attempt.imagePath
                    )
                }
                
                // Update adapter
                if (historyItems.isNotEmpty()) {
                    recyclerView.adapter = SimpleHistoryAdapter(historyItems)
                } else {
                    // No history, show empty message
                    recyclerView.adapter = null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                recyclerView.adapter = null
            }
        }
    }
    
    /**
     * Tính rank thật của user từ leaderboard
     */
    private suspend fun calculateUserRank(userId: String): Int {
        return try {
            // Get all completed attempts
            val allAttempts = taskAttemptRepository.getTopAttempts(1000)
                .filter { it.isCompleted }
            
            // Group by user and calculate total scores
            val userScores = allAttempts.groupBy { it.userId }
                .mapValues { (_, attempts) -> attempts.sumOf { it.score } }
                .entries
                .sortedByDescending { it.value }
            
            // Find user's rank
            val rank = userScores.indexOfFirst { it.key == userId } + 1
            if (rank == 0) 999 else rank
        } catch (e: Exception) {
            e.printStackTrace()
            999
        }
    }
}

data class SimpleHistoryItem(
    val taskName: String,
    val shapeType: String = "circle",
    val score: Int,
    val timeSpent: String,
    val date: String,
    val rank: Int = 0,
    val imagePath: String? = null
)