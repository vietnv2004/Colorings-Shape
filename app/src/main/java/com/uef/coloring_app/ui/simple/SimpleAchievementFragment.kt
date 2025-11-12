package com.uef.coloring_app.ui.simple

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.AchievementRepository
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.ui.achievements.AchievementAdapter
import kotlinx.coroutines.launch

class SimpleAchievementFragment : Fragment() {
    
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
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_achievements, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Initialize repositories
        val database = ColoringDatabase.getDatabase(requireContext())
        achievementRepository = AchievementRepository(database.achievementDao())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        
        initViews(view)
        loadAchievements()
    }
    
    private fun initViews(view: View) {
        totalAchievementsTextView = view.findViewById(R.id.totalAchievementsTextView)
        unlockedAchievementsTextView = view.findViewById(R.id.unlockedAchievementsTextView)
        achievementsRecyclerView = view.findViewById(R.id.achievementsRecyclerView)
    }
    
    /**
     * Load all achievements from database and display them
     */
    private fun loadAchievements() {
        lifecycleScope.launch {
            try {
                // Get current user ID
                val userId = sharedPreferences.getString(KEY_CURRENT_USER_ID, null)
                
                // Check if user is logged in
                if (userId == null) {
                    totalAchievementsTextView.text = getString(R.string.total_achievements, 0)
                    unlockedAchievementsTextView.text = getString(R.string.unlocked_achievements, 0)
                    achievementsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    achievementsRecyclerView.adapter = AchievementAdapter(emptyList())
                    Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem thành tích.", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                
                // Get achievement manager để lấy achievements theo user
                val achievementManager = com.uef.coloring_app.core.achievements.AchievementManager(requireContext())
                
                // Get total count and unlocked count from database theo user này
                val totalCount = achievementRepository.getAchievementCount()
                val unlockedAchievements = achievementManager.getUserUnlockedAchievements(userId)
                val lockedAchievements = achievementManager.getUserLockedAchievements(userId)
                val unlockedCount = unlockedAchievements.size
                
                // Update UI with real data
                totalAchievementsTextView.text = getString(R.string.total_achievements, totalCount)
                unlockedAchievementsTextView.text = getString(R.string.unlocked_achievements, unlockedCount)
                
                // Combine all achievements (unlocked first, then locked)
                val allAchievements = unlockedAchievements + lockedAchievements
                
                // Set up RecyclerView (display achievement points, not user total score)
                achievementsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                achievementsRecyclerView.adapter = AchievementAdapter(allAchievements)
                
            } catch (e: Exception) {
                // Fallback to default values if there's an error
                e.printStackTrace()
                totalAchievementsTextView.text = getString(R.string.total_achievements, 0)
                unlockedAchievementsTextView.text = getString(R.string.unlocked_achievements, 0)
            }
        }
    }
}
