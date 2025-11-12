package com.uef.coloring_app.ui.simple

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.ui.leaderboard.LeaderboardActivity
import kotlinx.coroutines.launch

class SimpleLeaderboardFragment : Fragment() {
    
    private lateinit var userRepository: UserRepository
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_leaderboard, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        val viewAllButton = view.findViewById<Button>(R.id.viewAllButton)
        
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        // Initialize repositories
        val database = ColoringDatabase.getDatabase(requireContext())
        userRepository = UserRepository(database.userDao())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        
        // Load leaderboard
        loadLeaderboard()
        
        viewAllButton.setOnClickListener {
            startActivity(Intent(requireContext(), LeaderboardActivity::class.java))
        }
    }
    
    private fun loadLeaderboard() {
        lifecycleScope.launch {
            try {
                // Get ALL attempts từ database (chỉ dùng dữ liệu thật)
                // Lấy từ database trực tiếp
                val database = ColoringDatabase.getDatabase(requireContext())
                val allAttempts = database.taskAttemptDao().getTopAttempts(100)
                    .filter { it.isCompleted } // Chỉ lấy attempts đã hoàn thành
                
                if (allAttempts.isEmpty()) {
                    // Không có dữ liệu thật
                    loadFallbackLeaderboard()
                    return@launch
                }
                
                // Group by user and calculate total scores
                val userScores = allAttempts.groupBy { it.userId }
                    .mapValues { (_, attempts) -> attempts.sumOf { it.score } }
                    .entries
                    .sortedByDescending { it.value }
                    .take(3)
                
                // Get user names
                val leaderboardItems = userScores.mapIndexed { index, (userId, score) ->
                    val user = userRepository.getUserById(userId)
                    
                    val rankEmoji = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        2 -> "🥉"
                        else -> "${index + 1}"
                    }
                    
                    // Lấy tên user - chỉ hiển thị tên, không hiển thị email
                    val displayName = when {
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
                    
                    SimpleLeaderboardItem(
                        rank = rankEmoji,
                        name = displayName,
                        score = score
                    )
                }
                
                // Update adapter
                if (leaderboardItems.isNotEmpty()) {
                    recyclerView.adapter = SimpleLeaderboardAdapter(leaderboardItems)
                } else {
                    loadFallbackLeaderboard()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                loadFallbackLeaderboard()
            }
        }
    }
    
    private fun loadFallbackLeaderboard() {
        val noDataText = getString(R.string.no_data)
        val leaderboardItems = listOf(
            SimpleLeaderboardItem("🥇", noDataText, 0),
            SimpleLeaderboardItem("🥈", noDataText, 0),
            SimpleLeaderboardItem("🥉", noDataText, 0)
        )
        recyclerView.adapter = SimpleLeaderboardAdapter(leaderboardItems)
    }
}

data class SimpleLeaderboardItem(
    val rank: String,
    val name: String,
    val score: Int
)