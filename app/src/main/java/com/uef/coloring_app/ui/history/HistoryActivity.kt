package com.uef.coloring_app.ui.history

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var historyRecyclerView: RecyclerView
    private lateinit var noHistoryTextView: TextView
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    private lateinit var taskRepository: TaskRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        // Hide action bar
        supportActionBar?.hide()
        
        // Initialize repositories
        val database = ColoringDatabase.getDatabase(applicationContext)
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        taskRepository = TaskRepository(database.taskDao())
        
        initViews()
        loadHistory()
    }
    
    private fun initViews() {
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        noHistoryTextView = findViewById(R.id.noHistoryTextView)
        
        historyRecyclerView.layoutManager = LinearLayoutManager(this)
    }
    
    private fun loadHistory() {
        lifecycleScope.launch {
            try {
                // Get all attempts from database (sorted by date, newest first)
                val attempts = taskAttemptRepository.getTopAttempts(100) // Get all with limit 100
                
                // Convert to HistoryItem with task names
                val historyItems = attempts.map { attempt ->
                    val task = taskRepository.getTaskById(attempt.taskId)
                    val taskName = task?.name ?: "Nhiệm vụ #${attempt.taskId.take(8)}"
                    
                    val timeInMinutes = (attempt.timeSpent / 1000 / 60).toInt()
                    val timeInSeconds = ((attempt.timeSpent / 1000) % 60).toInt()
                    val timeSpent = String.format("%d:%02d", timeInMinutes, timeInSeconds)
                    
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val date = dateFormat.format(Date(attempt.completedAt))
                    
                    HistoryItem(
                        id = attempt.id,
                        taskName = taskName,
                        score = attempt.score,
                        timeSpent = timeSpent,
                        date = date
                    )
                }
                
                // Update UI
                if (historyItems.isEmpty()) {
                    noHistoryTextView.visibility = TextView.VISIBLE
                    historyRecyclerView.visibility = RecyclerView.GONE
                } else {
                    noHistoryTextView.visibility = TextView.GONE
                    historyRecyclerView.visibility = RecyclerView.VISIBLE
                    historyRecyclerView.adapter = HistoryAdapter(historyItems)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                noHistoryTextView.visibility = TextView.VISIBLE
                noHistoryTextView.text = "Lỗi tải lịch sử: ${e.message}"
                historyRecyclerView.visibility = RecyclerView.GONE
            }
        }
    }
}

data class HistoryItem(
    val id: String = "",
    val taskName: String,
    val score: Int,
    val timeSpent: String,
    val date: String
)