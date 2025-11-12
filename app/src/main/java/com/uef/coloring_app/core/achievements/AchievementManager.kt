package com.uef.coloring_app.core.achievements

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.uef.coloring_app.ColoringApplication
import com.uef.coloring_app.core.haptic.HapticManager
import com.uef.coloring_app.core.notification.PushNotificationService
import com.uef.coloring_app.core.sounds.SoundManager
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.model.Achievement
import com.uef.coloring_app.data.model.AchievementType
import com.uef.coloring_app.data.repository.AchievementRepository
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.data.repository.TaskRepository
import kotlinx.coroutines.launch

/**
 * Manager để xử lý việc kiểm tra và mở khóa thành tích
 */
class AchievementManager(private val context: Context) {
    
    private val achievementRepository: AchievementRepository
    private val taskAttemptRepository: TaskAttemptRepository
    private val taskRepository: TaskRepository
    private val notificationService = PushNotificationService(context)
    private val soundManager: SoundManager = ColoringApplication.soundManager
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("achievement_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_ACHIEVEMENT_NOTIFIED_PREFIX = "achievement_notified_user_"
        private const val KEY_USER_ACHIEVEMENT_PREFIX = "user_achievement_"
    }
    
    /**
     * Get key for storing user achievement status
     */
    private fun getUserAchievementKey(userId: String, achievementId: String): String {
        return "${KEY_USER_ACHIEVEMENT_PREFIX}${userId}_$achievementId"
    }
    
    /**
     * Get key for storing achievement notification status per user
     */
    private fun getAchievementNotificationKey(userId: String, achievementId: String): String {
        return "${KEY_ACHIEVEMENT_NOTIFIED_PREFIX}${userId}_$achievementId"
    }
    
    init {
        val database = ColoringDatabase.getDatabase(context)
        achievementRepository = AchievementRepository(database.achievementDao())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        taskRepository = TaskRepository(database.taskDao())
    }
    
    /**
     * Kiểm tra xem achievement đã được user này unlock chưa
     */
    private fun isAchievementUnlockedByUser(userId: String, achievementId: String): Boolean {
        val key = getUserAchievementKey(userId, achievementId)
        return sharedPreferences.getBoolean(key, false)
    }
    
    /**
     * Đánh dấu achievement đã được user unlock
     */
    private fun markAchievementUnlockedForUser(userId: String, achievementId: String) {
        val key = getUserAchievementKey(userId, achievementId)
        sharedPreferences.edit().putBoolean(key, true).apply()
    }
    
    /**
     * Kiểm tra và mở khóa thành tích sau khi hoàn thành task
     * Phụ thuộc vào tài khoản người dùng
     */
    suspend fun checkAndUnlockAchievements(
        userId: String
    ): List<Achievement> {
        val unlockedAchievements = mutableListOf<Achievement>()
        
        // Lấy tất cả achievements (không phân biệt locked/unlocked)
        val allAchievements = achievementRepository.getLockedAchievements() + achievementRepository.getUnlockedAchievements()
        
        // Lấy dữ liệu thật từ database của user
        val userAttempts = taskAttemptRepository.getAttemptsByUser(userId)
        val totalScore = userAttempts.sumOf { it.score }
        
        // Kiểm tra từng achievement
        for (achievement in allAchievements) {
            // Bỏ qua nếu achievement này đã được user unlock
            if (isAchievementUnlockedByUser(userId, achievement.id)) {
                continue
            }
            // Tất cả thành tích đều tính theo tổng điểm người dùng
            val shouldUnlock = totalScore >= achievement.requirement
            
            if (shouldUnlock) {
                // Đánh dấu achievement đã được user này unlock (riêng cho tài khoản này)
                markAchievementUnlockedForUser(userId, achievement.id)
                
                // Thêm vào danh sách unlocked với timestamp
                val unlockedAchievement = achievement.copy(
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis()
                )
                unlockedAchievements.add(unlockedAchievement)
            }
        }
        
        return unlockedAchievements
    }
    
    /**
     * Get unlocked achievements của user cụ thể
     */
    suspend fun getUserUnlockedAchievements(userId: String): List<Achievement> {
        // Lấy tất cả achievements từ database (không dùng Flow)
        val allAchievements = achievementRepository.getLockedAchievements() + achievementRepository.getUnlockedAchievements()
        // Tính tổng điểm hiện tại của user
        val totalScore = taskAttemptRepository.getAttemptsByUser(userId).sumOf { it.score }
        // Đánh dấu mở khóa nếu đạt ngưỡng điểm hiện tại (bỏ phụ thuộc flag cũ)
        return allAchievements.filter { achievement ->
            totalScore >= achievement.requirement
        }.map { it.copy(isUnlocked = true) }
    }
    
    /**
     * Get locked achievements của user cụ thể
     */
    suspend fun getUserLockedAchievements(userId: String): List<Achievement> {
        // Lấy tất cả achievements từ database (không dùng Flow)
        val allAchievements = achievementRepository.getLockedAchievements() + achievementRepository.getUnlockedAchievements()
        val totalScore = taskAttemptRepository.getAttemptsByUser(userId).sumOf { it.score }
        // Khóa nếu chưa đạt ngưỡng điểm
        return allAchievements.filter { achievement ->
            totalScore < achievement.requirement
        }.map { it.copy(isUnlocked = false) }
    }
    
    /**
     * Get achievement count của user cụ thể
     */
    suspend fun getUserAchievementCount(userId: String): Int {
        val unlocked = getUserUnlockedAchievements(userId)
        return unlocked.size
    }
    
    /**
     * Hiển thị thông báo và phát âm thanh khi mở khóa thành tích
     * Chỉ thông báo 1 lần cho mỗi achievement của mỗi user
     */
    fun showAchievementUnlocked(context: Context, achievement: Achievement, userId: String) {
        // Kiểm tra xem đã thông báo achievement này cho user này chưa
        val notificationKey = getAchievementNotificationKey(userId, achievement.id)
        val alreadyNotified = sharedPreferences.getBoolean(notificationKey, false)
        
        // Nếu đã thông báo rồi thì không thông báo lại
        if (alreadyNotified) {
            return
        }
        
        // Đánh dấu đã thông báo
        sharedPreferences.edit().putBoolean(notificationKey, true).apply()
        
        // Haptic feedback
        HapticManager.success(context)
        
        // Phát âm thanh chúc mừng (sử dụng CoroutineScope từ lifecycle)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            soundManager.playAchievementUnlockedSound()
        }
        
        // Notification
        notificationService.showAchievementUnlockedNotification(achievement.name)
        
        // Toast message
        val message = "🎉 Hoàn thành: ${achievement.name}!\n${achievement.description}"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    /**
     * Hiển thị thông báo cho nhiều achievements cùng lúc
     * Chỉ thông báo cho các achievement chưa được thông báo trước đó (theo user)
     */
    fun showMultipleAchievementsUnlocked(context: Context, achievements: List<Achievement>, userId: String) {
        if (achievements.isEmpty()) return
        
        // Lọc chỉ những achievement chưa được thông báo cho user này
        val newAchievements = achievements.filter { achievement ->
            val notificationKey = getAchievementNotificationKey(userId, achievement.id)
            !sharedPreferences.getBoolean(notificationKey, false)
        }
        
        // Nếu không có achievement mới nào thì không cần thông báo
        if (newAchievements.isEmpty()) return
        
        // Đánh dấu tất cả các achievement mới là đã thông báo cho user này
        val editor = sharedPreferences.edit()
        newAchievements.forEach { achievement ->
            val notificationKey = getAchievementNotificationKey(userId, achievement.id)
            editor.putBoolean(notificationKey, true)
        }
        editor.apply()
        
        // Haptic feedback
        HapticManager.success(context)
        
        // Phát âm thanh chúc mừng (chỉ phát 1 lần)
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            soundManager.playAchievementUnlockedSound()
        }
        
        // Hiển thị thông báo cho achievement đầu tiên
        notificationService.showAchievementUnlockedNotification(newAchievements[0].name)
        
        // Nếu có nhiều achievements, hiển thị danh sách
        if (newAchievements.size > 1) {
            val count = newAchievements.size - 1
            Toast.makeText(
                context, 
                "🎉 Bạn đã mở khóa ${newAchievements.size} thành tích!\n${newAchievements[0].name} và $count thành tích khác",
                Toast.LENGTH_LONG
            ).show()
        } else {
            // Hiển thị thông báo cho achievement duy nhất
            val message = "🎉 Hoàn thành: ${newAchievements[0].name}!\n${newAchievements[0].description}"
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Cập nhật level của người dùng dựa trên số lượng achievements đã mở khóa
     * 1 achievement = 1 level
     */
    suspend fun updateUserLevel(userId: String): Int {
        // Level tương ứng với số lượng thành tích đã mở khóa cho user này
        val unlockedCount = getUserUnlockedAchievements(userId).size
        val computedLevel = if (unlockedCount > 0) unlockedCount else 1
        sharedPreferences.edit().putInt("user_level", computedLevel).apply()
        return computedLevel
    }
}

