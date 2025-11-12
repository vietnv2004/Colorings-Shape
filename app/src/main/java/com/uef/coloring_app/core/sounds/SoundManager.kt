package com.uef.coloring_app.core.sounds

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Build
import com.uef.coloring_app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class SoundManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: SoundManager? = null
        
        fun getInstance(context: Context): SoundManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SoundManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val sharedPreferences = context.getSharedPreferences("sound_prefs", Context.MODE_PRIVATE)
    
    private var mediaPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private val soundMap = ConcurrentHashMap<String, Int>()
    private var isPlayingMusic = false // Theo dõi trạng thái phát nhạc
    private var isCreatingPlayer = false // Lock để tránh tạo nhiều MediaPlayer cùng lúc
    private var hasStartedMusic = false // Đánh dấu đã bắt đầu phát nhạc lần đầu
    
    // Sound settings
    private var currentVolume = 50
    private var soundEnabled = true
    private var backgroundMusicEnabled = true
    private var soundEffectsEnabled = true
    private var notificationSoundEnabled = true
    private var selectedBackgroundMusic = "kid_music" // Default music
    private var selectedSoundEffect = "hieuung" // Default sound effect
    private var selectedNotificationSound = "thongbao1" // Default notification sound
    
    init {
        loadSettings()
        initSoundPool()
        loadButtonSound()
        loadCongratulationsSound()
        
        // Update button click sound after all settings are loaded
        soundMap["button_click"] = soundMap[selectedSoundEffect] ?: soundMap["hieuung"] ?: 0
    }
    
    private fun initSoundPool() {
        soundPool = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build())
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }
    }
    
    private fun loadButtonSound() {
        try {
            soundPool?.let { pool ->
                // Load all sound effect files
                val effects = listOf(
                    Pair("hieuung", R.raw.hieuung),
                    Pair("hieuung1", R.raw.hieuung1),
                    Pair("hieuung2", R.raw.hieuung2),
                    Pair("hieuung3", R.raw.hieuung3),
                    Pair("hieuung4", R.raw.hieuung4),
                    Pair("hieuung5", R.raw.hieuung5)
                )
                
                effects.forEach { (name, resource) ->
                    try {
                        val soundId = pool.load(context, resource, 1)
                        soundMap[name] = soundId
                    } catch (e: Exception) {
                        // Handle error silently
                    }
                }
                
                // Load notification sounds
                val notifications = listOf(
                    Pair("thongbao1", R.raw.thongbao1),
                    Pair("thongbao2", R.raw.thongbao2),
                    Pair("thongbao3", R.raw.thongbao3),
                    Pair("thongbao4", R.raw.thongbao4),
                    Pair("thongbao5", R.raw.thongbao5)
                )
                
                notifications.forEach { (name, resource) ->
                    try {
                        val soundId = pool.load(context, resource, 1)
                        soundMap[name] = soundId
                    } catch (e: Exception) {
                        // Handle error silently
                    }
                }
                
                // Set default button click sound
                soundMap["button_click"] = soundMap[selectedSoundEffect] ?: soundMap["hieuung"] ?: 0
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    private fun loadCongratulationsSound() {
        try {
            soundPool?.let { pool ->
                val soundId = pool.load(context, R.raw.hieuung_chuc_mung, 1)
                soundMap["congratulations"] = soundId
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    private fun loadSettings() {
        currentVolume = sharedPreferences.getInt("volume", 50)
        soundEnabled = sharedPreferences.getBoolean("sound_enabled", true)
        backgroundMusicEnabled = sharedPreferences.getBoolean("background_music_enabled", true)
        soundEffectsEnabled = sharedPreferences.getBoolean("sound_effects_enabled", true)

        // Kiểm tra xem đây có phải lần đầu vào app không
        val isFirstTime = !sharedPreferences.contains("selected_background_music")
        selectedBackgroundMusic = if (isFirstTime) {
            "kid_music" // Lần đầu vào app phát nhạc trẻ em
        } else {
            sharedPreferences.getString("selected_background_music", "kid_music") ?: "kid_music"
        }
        
        selectedSoundEffect = sharedPreferences.getString("selected_sound_effect", "hieuung") ?: "hieuung"
        
        notificationSoundEnabled = sharedPreferences.getBoolean("notification_sound_enabled", true)
        selectedNotificationSound = sharedPreferences.getString("selected_notification_sound", "thongbao1") ?: "thongbao1"
        
        // Load trạng thái nhạc đã phát
        hasStartedMusic = sharedPreferences.getBoolean("has_started_music", false)
    }
    
    private fun saveSettings() {
        sharedPreferences.edit()
            .putInt("volume", currentVolume)
            .putBoolean("sound_enabled", soundEnabled)
            .putBoolean("background_music_enabled", backgroundMusicEnabled)
            .putBoolean("sound_effects_enabled", soundEffectsEnabled)
            .putBoolean("notification_sound_enabled", notificationSoundEnabled)
            .putString("selected_background_music", selectedBackgroundMusic)
            .putString("selected_sound_effect", selectedSoundEffect)
            .putString("selected_notification_sound", selectedNotificationSound)
            .putBoolean("has_started_music", hasStartedMusic)
            .apply()
    }
    
    suspend fun isSoundEnabled(): Boolean = withContext(Dispatchers.IO) {
        soundEnabled
    }
    
    suspend fun setSoundEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        soundEnabled = enabled
        saveSettings()
        
        if (!enabled) {
            stopBackgroundMusic()
        }
    }
    
    suspend fun getVolume(): Int = withContext(Dispatchers.IO) {
        currentVolume
    }
    
    suspend fun setVolume(volume: Int) = withContext(Dispatchers.IO) {
        currentVolume = volume.coerceIn(0, 100)
        saveSettings()
        
        // Apply volume to audio manager
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val volumeLevel = (maxVolume * currentVolume / 100f).toInt()
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volumeLevel, 0)
    }
    
    suspend fun isBackgroundMusicEnabled(): Boolean = withContext(Dispatchers.IO) {
        backgroundMusicEnabled
    }
    
    suspend fun setBackgroundMusicEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        backgroundMusicEnabled = enabled
        saveSettings()
        
        if (!enabled) {
            stopBackgroundMusic()
        }
    }
    
    suspend fun isSoundEffectsEnabled(): Boolean = withContext(Dispatchers.IO) {
        soundEffectsEnabled
    }
    
    suspend fun setSoundEffectsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        soundEffectsEnabled = enabled
        saveSettings()
    }
    
    suspend fun setSelectedBackgroundMusic(musicName: String) = withContext(Dispatchers.IO) {
        // FORCE STOP tất cả nhạc để đảm bảo không còn nhạc nào phát
        forceStopAllMusic()
        
        // Cập nhật nhạc đã chọn
        selectedBackgroundMusic = musicName
        saveSettings() // Lưu nhạc đã chọn
        
        // Phát nhạc mới ngay lập tức
        if (backgroundMusicEnabled && soundEnabled) {
            playBackgroundMusic()
        }
    }
    
    suspend fun getSelectedBackgroundMusic(): String = withContext(Dispatchers.IO) {
        selectedBackgroundMusic
    }
    
    suspend fun getAvailableBackgroundMusic(): List<String> = withContext(Dispatchers.IO) {
        listOf("kid_music", "happy_birthday", "tet_binh_an")
    }
    
    suspend fun getAvailableSoundEffects(): List<String> = withContext(Dispatchers.IO) {
        listOf("hieuung", "hieuung1", "hieuung2", "hieuung3", "hieuung4", "hieuung5")
    }
    
    suspend fun getAvailableNotificationSounds(): List<String> = withContext(Dispatchers.IO) {
        listOf("thongbao1", "thongbao2", "thongbao3", "thongbao4", "thongbao5")
    }
    
    suspend fun setSelectedNotificationSound(soundName: String) = withContext(Dispatchers.IO) {
        selectedNotificationSound = soundName
        saveSettings()
    }
    
    suspend fun getSelectedNotificationSound(): String = withContext(Dispatchers.IO) {
        selectedNotificationSound
    }
    
    suspend fun isNotificationSoundEnabled(): Boolean = withContext(Dispatchers.IO) {
        notificationSoundEnabled
    }
    
    suspend fun setNotificationSoundEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        notificationSoundEnabled = enabled
        saveSettings()
    }
    
    suspend fun playNotificationSound() = withContext(Dispatchers.IO) {
        if (soundEnabled && notificationSoundEnabled) {
            try {
                val soundId = soundMap[selectedNotificationSound]
                soundId?.let {
                    soundPool?.play(it, 0.5f, 0.5f, 1, 0, 1.0f)
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    suspend fun playAchievementUnlockedSound() = withContext(Dispatchers.IO) {
        if (soundEnabled && soundEffectsEnabled) {
            try {
                // Phát âm thanh chúc mừng khi mở khóa thành tích
                val soundId = soundMap["congratulations"]
                soundId?.let {
                    soundPool?.play(it, 0.8f, 0.8f, 1, 0, 1.0f)
                } ?: run {
                    // Fallback: phát notification sound nếu không có âm thanh chúc mừng
                    playNotificationSound()
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
    }
    
    suspend fun getSelectedSoundEffect(): String = withContext(Dispatchers.IO) {
        selectedSoundEffect
    }
    
    suspend fun setSelectedSoundEffect(effectName: String) = withContext(Dispatchers.IO) {
        selectedSoundEffect = effectName
        saveSettings()
        
        // Update button click sound
        soundMap["button_click"] = soundMap[selectedSoundEffect] ?: soundMap["hieuung"] ?: 0
    }
    
    // Hàm kiểm tra trạng thái nhạc nền để debug
    suspend fun getMusicStatus(): String = withContext(Dispatchers.IO) {
        "isPlayingMusic: $isPlayingMusic, isCreatingPlayer: $isCreatingPlayer, mediaPlayer: ${mediaPlayer != null}, isPlaying: ${mediaPlayer?.isPlaying}, selectedMusic: $selectedBackgroundMusic"
    }
    
    // Hàm kiểm tra xem có phải lần đầu vào app không
    suspend fun isFirstTimeApp(): Boolean = withContext(Dispatchers.IO) {
        !sharedPreferences.contains("selected_background_music")
    }
    
    // Hàm kiểm tra xem nhạc có đang phát không
    fun isMusicCurrentlyPlaying(): Boolean {
        return isPlayingMusic && mediaPlayer?.isPlaying == true
    }
    
    // Hàm force stop tất cả MediaPlayer để đảm bảo không còn nhạc nào phát
    suspend fun forceStopAllMusic() = withContext(Dispatchers.IO) {
        try {
            isPlayingMusic = false
            isCreatingPlayer = false // Reset lock
            
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.reset()
                    player.release()
                } catch (e: Exception) {
                    // Handle error silently
                }
            }
            mediaPlayer = null
            
            // Đợi lâu để đảm bảo hoàn toàn dừng
            delay(500)
        } catch (e: Exception) {
            isPlayingMusic = false
            isCreatingPlayer = false
            mediaPlayer = null
        }
    }
    
    fun startAutoPlay() {
        // Tự động phát nhạc nền ngay khi mở app
        // CHỈ phát nếu chưa có nhạc đang phát và chưa từng phát
        if (backgroundMusicEnabled && soundEnabled && !isMusicCurrentlyPlaying() && !hasStartedMusic) {
            hasStartedMusic = true // Đánh dấu đã bắt đầu phát nhạc
            saveSettings() // Lưu trạng thái
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch {
                // Phát nhạc ngay lập tức, không cần delay
                playBackgroundMusic()
            }
        } else if (backgroundMusicEnabled && soundEnabled && hasStartedMusic && !isMusicCurrentlyPlaying()) {
            // Nếu đã từng phát nhạc nhưng hiện tại không phát, tiếp tục phát
            @Suppress("OPT_IN_USAGE")
            GlobalScope.launch {
                playBackgroundMusic()
            }
        }
    }
    
    suspend fun playBackgroundMusic() = withContext(Dispatchers.IO) {
        if (!soundEnabled || !backgroundMusicEnabled) return@withContext
        
        // Kiểm tra lock để tránh tạo nhiều MediaPlayer cùng lúc
        if (isCreatingPlayer) {
            return@withContext
        }
        
        try {
            // Đánh dấu đang tạo MediaPlayer
            isCreatingPlayer = true
            
            // LUÔN dừng nhạc cũ trước khi phát nhạc mới
            stopBackgroundMusic()
            
            // Đợi một chút để đảm bảo MediaPlayer cũ đã được release hoàn toàn
            delay(200)
            
            // Kiểm tra lại sau khi dừng
            if (!soundEnabled || !backgroundMusicEnabled) {
                isCreatingPlayer = false
                return@withContext
            }
            
            // Đánh dấu đang phát nhạc
            isPlayingMusic = true
            
            // Tạo MediaPlayer mới
            mediaPlayer = MediaPlayer().apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setAudioAttributes(android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build())
                } else {
                    @Suppress("DEPRECATION")
                    setAudioStreamType(AudioManager.STREAM_MUSIC)
                }
                setVolume(currentVolume / 100f, currentVolume / 100f)
                
                // Load actual background music
                try {
                    val resourceId = when (selectedBackgroundMusic) {
                        "kid_music" -> R.raw.kid_music
                        "happy_birthday" -> R.raw.happy_birthday
                        "tet_binh_an" -> R.raw.tet_binh_an
                        else -> R.raw.kid_music
                    }
                    val afd = context.resources.openRawResourceFd(resourceId)
                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                    afd.close()
                    
                    prepare()
                    start()
                    
                    setOnCompletionListener {
                        // Chỉ loop nếu vẫn enabled và đây vẫn là MediaPlayer hiện tại
                        if (backgroundMusicEnabled && soundEnabled && this@apply == mediaPlayer && isPlayingMusic) {
                            start() // Loop
                        }
                    }
                } catch (e: Exception) {
                    // Handle error silently
                    isPlayingMusic = false
                }
            }
        } catch (e: Exception) {
            // Handle error silently
            isPlayingMusic = false
        } finally {
            // Giải phóng lock
            isCreatingPlayer = false
        }
    }
    
    suspend fun stopBackgroundMusic() = withContext(Dispatchers.IO) {
        try {
            // Đánh dấu không phát nhạc NGAY LẬP TỨC
            isPlayingMusic = false
            
            mediaPlayer?.let { player ->
                try {
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.reset() // Reset MediaPlayer về trạng thái ban đầu
                    player.release()
                } catch (e: Exception) {
                    // Handle error silently
                }
            }
            mediaPlayer = null
            
            // Đợi thêm một chút để đảm bảo MediaPlayer đã được release hoàn toàn
            delay(200)
        } catch (e: Exception) {
            // Handle error silently
            isPlayingMusic = false
            mediaPlayer = null
        }
    }
    
    suspend fun playTestSound() = withContext(Dispatchers.IO) {
        if (!soundEnabled) return@withContext
        
        try {
            soundPool?.let { _ ->
                // Play a test sound
                soundMap.getOrPut("test_sound") {
                    // Load a test sound (you would load actual sound files)
                    // pool.load(context, R.raw.test_sound, 1)
                    1 // Placeholder
                }

                // Simulate sound playing
                delay(1000)
            }
        } catch (e: Exception) {
            // Handle error silently for demo
        }
    }
    
    suspend fun playSoundEffect(@Suppress("UNUSED_PARAMETER") effectName: String) = withContext(Dispatchers.IO) {
        if (!soundEnabled || !soundEffectsEnabled) return@withContext
        
        try {
            soundPool?.let { pool ->
                val soundId = soundMap["button_click"]
                if (soundId != null) {
                    pool.play(soundId, currentVolume / 100f, currentVolume / 100f, 1, 0, 1f)
                }
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    // Non-suspend function for playing button sound effect
    fun playButtonClickSound() {
        if (!soundEnabled || !soundEffectsEnabled) return
        
        try {
            soundPool?.let { pool ->
                val soundId = soundMap["button_click"]
                if (soundId != null) {
                    pool.play(soundId, currentVolume / 100f, currentVolume / 100f, 1, 0, 1f)
                }
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    // Non-suspend function for playing congratulations sound effect
    fun playCongratulationsSound() {
        if (!soundEnabled || !soundEffectsEnabled) return
        
        try {
            soundPool?.let { pool ->
                val soundId = soundMap["congratulations"]
                if (soundId != null) {
                    pool.play(soundId, currentVolume / 100f, currentVolume / 100f, 1, 0, 1f)
                }
            }
        } catch (e: Exception) {
            // Handle error silently
        }
    }
    
    suspend fun toggleFeature(featureName: String) = withContext(Dispatchers.IO) {
        when (featureName) {
            "Nhạc Nền" -> {
                backgroundMusicEnabled = !backgroundMusicEnabled
                saveSettings()
                if (!backgroundMusicEnabled) {
                    stopBackgroundMusic()
                }
            }
            "Hiệu Ứng Âm Thanh" -> {
                soundEffectsEnabled = !soundEffectsEnabled
                saveSettings()
            }
            "Âm Thanh Thông Báo" -> {
                notificationSoundEnabled = !notificationSoundEnabled
                saveSettings()
            }
            else -> {
                // Handle other features
            }
        }
    }
    
    fun cleanup() {
        try {
            // Đánh dấu không phát nhạc
            isPlayingMusic = false
            isCreatingPlayer = false // Reset lock
            
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            mediaPlayer = null
            
            soundPool?.release()
            soundPool = null
            soundMap.clear()
        } catch (e: Exception) {
            // Handle error silently
            isPlayingMusic = false
            isCreatingPlayer = false
        }
    }
    
    // Reset để app có thể phát nhạc lại từ đầu khi khởi động lại
    fun resetMusicState() {
        // CHỈ reset khi app được khởi động lại hoàn toàn
        // Không reset khi chuyển activity
        hasStartedMusic = false
        isPlayingMusic = false
        isCreatingPlayer = false
        saveSettings()
    }
}
