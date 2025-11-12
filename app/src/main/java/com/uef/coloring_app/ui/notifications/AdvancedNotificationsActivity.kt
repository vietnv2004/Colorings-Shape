package com.uef.coloring_app.ui.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import com.uef.coloring_app.ColoringApplication
import com.uef.coloring_app.R
import com.uef.coloring_app.core.haptic.HapticManager
import com.uef.coloring_app.core.notification.PushNotificationService
import com.uef.coloring_app.core.sounds.SoundManager
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.ui.common.BaseActivity
import com.uef.coloring_app.ui.simple.SimpleMainActivity
import kotlinx.coroutines.launch

class AdvancedNotificationsActivity : BaseActivity() {
    
    private lateinit var notificationStatusTextView: TextView
    private lateinit var notificationInfoTextView: TextView
    private lateinit var testNotificationButton: Button
    private lateinit var enableNotificationsSwitch: Switch
    private lateinit var soundNotificationsSwitch: Switch
    private lateinit var vibrationNotificationsSwitch: Switch
    private lateinit var achievementNotificationsSwitch: Switch
    
    private lateinit var notificationManager: NotificationManager
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var notificationService: PushNotificationService
    private val soundManager: SoundManager = ColoringApplication.soundManager
    
    private val CHANNEL_ID = "coloring_app_notifications"
    private val CHANNEL_NAME = "Thông báo Coloring Shapes"
    
    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_SOUND_NOTIFICATIONS = "sound_notifications"
        private const val KEY_VIBRATION_NOTIFICATIONS = "vibration_notifications"
        private const val KEY_ACHIEVEMENT_NOTIFICATIONS = "achievement_notifications"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_notifications)
        
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        // Initialize services
        notificationService = PushNotificationService(this)
        
        initNotificationManager()
        initViews()
        loadSettings()
        setupClickListeners()
    }
    
    private fun initNotificationManager() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo từ ứng dụng Coloring Shapes"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun initViews() {
        notificationStatusTextView = findViewById(R.id.notificationStatusTextView)
        notificationInfoTextView = findViewById(R.id.notificationInfoTextView)
        testNotificationButton = findViewById(R.id.testNotificationButton)
        enableNotificationsSwitch = findViewById(R.id.enableNotificationsSwitch)
        soundNotificationsSwitch = findViewById(R.id.soundNotificationsSwitch)
        vibrationNotificationsSwitch = findViewById(R.id.vibrationNotificationsSwitch)
        achievementNotificationsSwitch = findViewById(R.id.achievementNotificationsSwitch)
    }
    
    private fun setupClickListeners() {
        testNotificationButton.setOnClickListenerWithSound {
            HapticManager.buttonClick(this)
            testNotification()
        }
        
        enableNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleNotifications(isChecked)
        }
        
        soundNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleSoundNotifications(isChecked)
        }
        
        vibrationNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleVibrationNotifications(isChecked)
        }
        
        achievementNotificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleAchievementNotifications(isChecked)
        }
    }
    
    /**
     * Load settings from SharedPreferences
     */
    private fun loadSettings() {
        val notificationsEnabled = sharedPreferences.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        val soundEnabled = sharedPreferences.getBoolean(KEY_SOUND_NOTIFICATIONS, true)
        val vibrationEnabled = sharedPreferences.getBoolean(KEY_VIBRATION_NOTIFICATIONS, true)
        val achievementEnabled = sharedPreferences.getBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, true)
        
        enableNotificationsSwitch.isChecked = notificationsEnabled
        soundNotificationsSwitch.isChecked = soundEnabled
        vibrationNotificationsSwitch.isChecked = vibrationEnabled
        achievementNotificationsSwitch.isChecked = achievementEnabled
        
        updateStatusText()
    }
    
    /**
     * Save settings to SharedPreferences
     */
    private fun saveSettings() {
        sharedPreferences.edit()
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, enableNotificationsSwitch.isChecked)
            .putBoolean(KEY_SOUND_NOTIFICATIONS, soundNotificationsSwitch.isChecked)
            .putBoolean(KEY_VIBRATION_NOTIFICATIONS, vibrationNotificationsSwitch.isChecked)
            .putBoolean(KEY_ACHIEVEMENT_NOTIFICATIONS, achievementNotificationsSwitch.isChecked)
            .apply()
    }
    
    /**
     * Update status text based on current settings
     */
    private fun updateStatusText() {
        val status = when {
            !enableNotificationsSwitch.isChecked -> "Thông báo đã tắt"
            enableNotificationsSwitch.isChecked && !soundNotificationsSwitch.isChecked && !vibrationNotificationsSwitch.isChecked -> 
                "Thông báo im lặng"
            enableNotificationsSwitch.isChecked -> "Thông báo đã sẵn sàng"
            else -> "Thông báo đã sẵn sàng"
        }
        
        notificationStatusTextView.text = status
        
        val info = if (enableNotificationsSwitch.isChecked) {
            "Cấu hình cài đặt thông báo nâng cao để luôn được cập nhật."
        } else {
            "Thông báo đã bị tắt. Bật thông báo để nhận cập nhật."
        }
        
        notificationInfoTextView.text = info
    }
    
    private fun testNotification() {
        // Kiểm tra xem thông báo có được bật không
        if (!enableNotificationsSwitch.isChecked) {
            Toast.makeText(this, "Vui lòng bật thông báo trước khi kiểm tra", Toast.LENGTH_LONG).show()
            return
        }
        
        // Sử dụng PushNotificationService để gửi thông báo kiểm tra
        notificationService.showAchievementNotification(
            "Thông báo kiểm tra",
            "Thông báo kiểm tra đã được gửi thành công!"
        )
        
        // Update UI
        notificationStatusTextView.text = "Thông báo: Đang kiểm tra"
        notificationInfoTextView.text = "Thông báo kiểm tra đã được gửi thành công!"
        
        Toast.makeText(this, "Đã gửi thông báo kiểm tra", Toast.LENGTH_SHORT).show()
        
        // Haptic feedback
        HapticManager.success(this)
    }
    
    private fun toggleNotifications(isEnabled: Boolean) {
        // Lưu setting
        saveSettings()
        
        // Update UI
        updateStatusText()
        
        // Haptic feedback
        if (isEnabled) {
            HapticManager.success(this)
            Toast.makeText(this, "Thông báo đã bật", Toast.LENGTH_SHORT).show()
        } else {
            HapticManager.buttonClick(this)
            Toast.makeText(this, "Thông báo đã tắt", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleSoundNotifications(isEnabled: Boolean) {
        // Lưu setting
        saveSettings()
        
        // Update UI
        updateStatusText()
        
        // Cập nhật SoundManager
        lifecycleScope.launch {
            soundManager.setNotificationSoundEnabled(isEnabled)
        }
        
        // Haptic feedback
        if (isEnabled) {
            HapticManager.success(this)
            Toast.makeText(this, "Thông báo âm thanh đã bật", Toast.LENGTH_SHORT).show()
        } else {
            HapticManager.buttonClick(this)
            Toast.makeText(this, "Thông báo âm thanh đã tắt", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleVibrationNotifications(isEnabled: Boolean) {
        // Lưu setting
        saveSettings()
        
        // Update UI
        updateStatusText()
        
        // Haptic feedback
        if (isEnabled) {
            HapticManager.success(this)
            Toast.makeText(this, "Thông báo rung đã bật", Toast.LENGTH_SHORT).show()
        } else {
            HapticManager.buttonClick(this)
            Toast.makeText(this, "Thông báo rung đã tắt", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun toggleAchievementNotifications(isEnabled: Boolean) {
        // Lưu setting
        saveSettings()
        
        // Update UI
        updateStatusText()
        
        // Haptic feedback
        if (isEnabled) {
            HapticManager.success(this)
            Toast.makeText(this, "Thông báo thành tích đã bật", Toast.LENGTH_SHORT).show()
        } else {
            HapticManager.buttonClick(this)
            Toast.makeText(this, "Thông báo thành tích đã tắt", Toast.LENGTH_SHORT).show()
        }
    }
    
}
