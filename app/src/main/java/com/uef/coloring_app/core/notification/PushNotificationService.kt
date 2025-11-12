package com.uef.coloring_app.core.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.uef.coloring_app.R
import com.uef.coloring_app.ui.simple.SimpleMainActivity

class PushNotificationService(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
    
    /**
     * Check if notifications are enabled from settings
     */
    private fun areNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("notifications_enabled", true)
    }
    
    /**
     * Check if sound notifications are enabled
     */
    private fun isSoundEnabled(): Boolean {
        return sharedPreferences.getBoolean("sound_notifications", true)
    }
    
    /**
     * Check if vibration is enabled
     */
    private fun isVibrationEnabled(): Boolean {
        return sharedPreferences.getBoolean("vibration_notifications", true)
    }
    
    /**
     * Check if achievement notifications are enabled
     */
    private fun areAchievementNotificationsEnabled(): Boolean {
        return sharedPreferences.getBoolean("achievement_notifications", true)
    }
    
    companion object {
        const val CHANNEL_ID_ACHIEVEMENT = "achievement_channel"
        const val CHANNEL_ID_TIMER = "timer_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"
        const val NOTIFICATION_ID_ACHIEVEMENT = 2001
        const val NOTIFICATION_ID_TIMER = 2002
        const val NOTIFICATION_ID_GENERAL = 2003
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Achievement Channel
            val achievementChannel = NotificationChannel(
                CHANNEL_ID_ACHIEVEMENT,
                "Achievements",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for achievements and rewards"
                enableVibration(true)
                enableLights(true)
            }
            
            // Timer Channel
            val timerChannel = NotificationChannel(
                CHANNEL_ID_TIMER,
                "Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Timer and task notifications"
                enableVibration(true)
                enableLights(true)
            }
            
            // General Channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }
            
            notificationManager.createNotificationChannel(achievementChannel)
            notificationManager.createNotificationChannel(timerChannel)
            notificationManager.createNotificationChannel(generalChannel)
        }
    }
    
    fun showAchievementNotification(title: String, message: String) {
        // Check if notifications are enabled
        if (!areNotificationsEnabled()) {
            return
        }
        
        val intent = Intent(context, SimpleMainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ACHIEVEMENT)
            .setContentTitle("🏆 $title")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_gift)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        
        // Apply sound and vibration settings
        if (isSoundEnabled()) {
            builder.setDefaults(Notification.DEFAULT_SOUND)
        }
        if (isVibrationEnabled()) {
            builder.setDefaults(Notification.DEFAULT_VIBRATE)
        }
        
        notificationManager.notify(NOTIFICATION_ID_ACHIEVEMENT, builder.build())
    }
    
    fun showTimerNotification(title: String, message: String) {
        val intent = Intent(context, SimpleMainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TIMER)
            .setContentTitle("⏰ $title")
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_time)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(Notification.DEFAULT_ALL)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_TIMER, notification)
    }
    
    fun showGeneralNotification(title: String, message: String) {
        val intent = Intent(context, SimpleMainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID_GENERAL)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID_GENERAL, notification)
    }
    
    fun showTimeUpNotification() {
        showTimerNotification(
            "Time's Up!",
            "Your coloring time has ended. Submit your work now!"
        )
    }
    
    fun showAchievementUnlockedNotification(achievementName: String) {
        // Check if achievement notifications are enabled
        if (!areAchievementNotificationsEnabled()) {
            return
        }
        
        showAchievementNotification(
            "Achievement Unlocked!",
            "Congratulations! You've unlocked: $achievementName"
        )
    }

    fun showLevelUpNotification(newLevel: Int, unlockedNames: String?) {
        val title = "Level Up!"
        val message = if (unlockedNames.isNullOrBlank()) {
            "You've reached Level $newLevel. Keep going!"
        } else {
            "You've reached Level $newLevel. Unlocked: $unlockedNames"
        }
        showAchievementNotification(title, message)
    }
    
    fun showNewTaskNotification(taskName: String) {
        showGeneralNotification(
            "New Task Available",
            "A new coloring task '$taskName' is now available!"
        )
    }
    
    fun showLeaderboardNotification(rank: Int) {
        showGeneralNotification(
            "Leaderboard Update",
            "You're now ranked #$rank on the leaderboard!"
        )
    }
}
