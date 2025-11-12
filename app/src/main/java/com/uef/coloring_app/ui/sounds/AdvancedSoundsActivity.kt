package com.uef.coloring_app.ui.sounds

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.core.sounds.SoundManager
import com.uef.coloring_app.ui.sounds.SoundFeature
import com.uef.coloring_app.ui.sounds.SoundFeatureAdapter
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.launch

class AdvancedSoundsActivity : BaseActivity() {
    
    // Views
    private lateinit var audioStatusText: TextView
    private lateinit var audioInfoText: TextView
    private lateinit var playButton: Button
    private lateinit var stopButton: Button
    private lateinit var volumeSeekBar: SeekBar
    private lateinit var enableSoundSwitch: Switch
    private lateinit var backgroundMusicSwitch: Switch
    private lateinit var soundEffectsSwitch: Switch
    private lateinit var featuresRecyclerView: RecyclerView
    
    // Managers
    private lateinit var soundManager: SoundManager
    private lateinit var audioManager: AudioManager
    private var mediaPlayer: MediaPlayer? = null
    
    // Adapter
    private lateinit var soundFeatureAdapter: SoundFeatureAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_sounds)
        
        initSoundManager()
        initViews()
        setupClickListeners()
        updateAudioStatus()
        setupRecyclerView()
        
        // Tự động phát nhạc nền khi vào màn hình
        lifecycleScope.launch {
            try {
                if (soundManager.isSoundEnabled() && soundManager.isBackgroundMusicEnabled()) {
                    soundManager.playBackgroundMusic()
                }
            } catch (e: Exception) {
                // Handle error silently
            }
        }
        
        // Hoặc sử dụng startAutoPlay
        soundManager.startAutoPlay()
    }
    
    private fun initSoundManager() {
        soundManager = SoundManager.getInstance(this)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    
    private fun initViews() {
        audioStatusText = findViewById(R.id.audioStatusText)
        audioInfoText = findViewById(R.id.audioInfoText)
        playButton = findViewById(R.id.playButton)
        stopButton = findViewById(R.id.stopButton)
        volumeSeekBar = findViewById(R.id.volumeSeekBar)
        enableSoundSwitch = findViewById(R.id.enableSoundSwitch)
        backgroundMusicSwitch = findViewById(R.id.backgroundMusicSwitch)
        soundEffectsSwitch = findViewById(R.id.soundEffectsSwitch)
        featuresRecyclerView = findViewById(R.id.featuresRecyclerView)
    }
    
    private fun setupClickListeners() {
        // Audio Control Buttons
        playButton.setOnClickListenerWithSound {
            playAudio()
        }
        
        stopButton.setOnClickListenerWithSound {
            stopAudio()
        }
        
        // SeekBar Listeners
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    lifecycleScope.launch {
                        soundManager.setVolume(progress)
                    }
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Switch Listeners
        enableSoundSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                soundManager.setSoundEnabled(isChecked)
                updateAudioStatus()
            }
        }
        
        backgroundMusicSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                soundManager.setBackgroundMusicEnabled(isChecked)
            }
        }
        
        soundEffectsSwitch.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                soundManager.setSoundEffectsEnabled(isChecked)
            }
        }
    }
    
    private fun updateAudioStatus() {
        lifecycleScope.launch {
            val isEnabled = soundManager.isSoundEnabled()
            val volume = soundManager.getVolume()
            
            audioStatusText.text = if (isEnabled) {
                "Âm thanh nâng cao đã sẵn sàng"
            } else {
                "Âm thanh đã tắt"
            }
            
            audioInfoText.text = if (isEnabled) {
                "Cấu hình cài đặt âm thanh nâng cao để có trải nghiệm sống động.\nÂm lượng: $volume%"
            } else {
                "Cấu hình cài đặt âm thanh nâng cao để có trải nghiệm sống động."
            }
            
            // Update SeekBars
            volumeSeekBar.progress = volume
            
            // Update Switches
            enableSoundSwitch.isChecked = isEnabled
            backgroundMusicSwitch.isChecked = soundManager.isBackgroundMusicEnabled()
            soundEffectsSwitch.isChecked = soundManager.isSoundEffectsEnabled()
        }
    }
    
    private fun playAudio() {
        lifecycleScope.launch {
            try {
                soundManager.playBackgroundMusic()
                Toast.makeText(this@AdvancedSoundsActivity, "Đang phát âm thanh nền", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedSoundsActivity, "Lỗi phát âm thanh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun stopAudio() {
        lifecycleScope.launch {
            try {
                soundManager.stopBackgroundMusic()
                Toast.makeText(this@AdvancedSoundsActivity, "Đã dừng âm thanh", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedSoundsActivity, "Lỗi dừng âm thanh: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    
    private fun setupRecyclerView() {
        lifecycleScope.launch {
            val features = listOf(
                SoundFeature("🎵", getString(R.string.background_music), getString(R.string.background_music_desc), getString(R.string.active)),
                SoundFeature("🔊", getString(R.string.sound_effects), getString(R.string.sound_effects_desc), getString(R.string.active)),
                SoundFeature("🔔", getString(R.string.notification_sound), getString(R.string.notification_sound_desc), if (soundManager.isNotificationSoundEnabled()) getString(R.string.active) else getString(R.string.inactive))
            )
            
            setupFeaturesRecycler(features)
        }
    }
    
    private fun setupFeaturesRecycler(features: List<SoundFeature>) {
        
        soundFeatureAdapter = SoundFeatureAdapter(features) { feature ->
            lifecycleScope.launch {
                try {
                    when (feature.title) {
                        getString(R.string.background_music) -> {
                            // Luôn hiển thị dialog chọn nhạc nền khi nhấn nút "CHỌN NHẠC"
                            showMusicSelectionDialog()
                        }
                        getString(R.string.sound_effects) -> {
                            // Hiển thị dialog chọn hiệu ứng âm thanh
                            showSoundEffectSelectionDialog()
                        }
                        getString(R.string.notification_sound) -> {
                            // Hiển thị dialog chọn âm thanh thông báo
                            showNotificationSoundSelectionDialog()
                        }
                        else -> {
                            val isActive = feature.status == getString(R.string.active)
                            soundManager.toggleFeature(feature.title)
                            val action = if (isActive) "tắt" else "bật"
                            Toast.makeText(this@AdvancedSoundsActivity, "Đã $action ${feature.title}", Toast.LENGTH_SHORT).show()
                            // Reload features to update status
                            setupRecyclerView()
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AdvancedSoundsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        featuresRecyclerView.layoutManager = LinearLayoutManager(this)
        featuresRecyclerView.adapter = soundFeatureAdapter
    }
    
    private fun showNotificationSoundSelectionDialog() {
        lifecycleScope.launch {
            try {
                val availableSounds = soundManager.getAvailableNotificationSounds()
                val soundNames = availableSounds.map { sound ->
                    when (sound) {
                        "thongbao1" -> "Thông Báo 1"
                        "thongbao2" -> "Thông Báo 2"
                        "thongbao3" -> "Thông Báo 3"
                        "thongbao4" -> "Thông Báo 4"
                        "thongbao5" -> "Thông Báo 5"
                        else -> sound
                    }
                }
                
                val currentSound = soundManager.getSelectedNotificationSound()
                val currentIndex = availableSounds.indexOf(currentSound)
                
                androidx.appcompat.app.AlertDialog.Builder(this@AdvancedSoundsActivity)
                    .setTitle(getString(R.string.select_notification_sound))
                    .setSingleChoiceItems(soundNames.toTypedArray(), currentIndex) { dialog, which ->
                        lifecycleScope.launch {
                            try {
                                soundManager.setSelectedNotificationSound(availableSounds[which])
                                soundManager.playNotificationSound()
                                Toast.makeText(this@AdvancedSoundsActivity, "Đã chọn: ${soundNames[which]}", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@AdvancedSoundsActivity, "Lỗi chọn âm thanh: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedSoundsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showMusicSelectionDialog() {
        lifecycleScope.launch {
            try {
                val availableMusic = soundManager.getAvailableBackgroundMusic()
                val musicNames = availableMusic.map { music ->
                    when (music) {
                        "kid_music" -> "Nhạc Trẻ Em"
                        "happy_birthday" -> "Chúc Mừng Sinh Nhật"
                        "tet_binh_an" -> "Tết Bình An"
                        else -> music
                    }
                }
                
                val currentMusic = soundManager.getSelectedBackgroundMusic()
                val currentIndex = availableMusic.indexOf(currentMusic)
                
                androidx.appcompat.app.AlertDialog.Builder(this@AdvancedSoundsActivity)
                    .setTitle(getString(R.string.select_background_music))
                    .setSingleChoiceItems(musicNames.toTypedArray(), currentIndex) { dialog, which ->
                        lifecycleScope.launch {
                            try {
                                soundManager.setSelectedBackgroundMusic(availableMusic[which])
                                Toast.makeText(this@AdvancedSoundsActivity, "Đã chọn: ${musicNames[which]}", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@AdvancedSoundsActivity, "Lỗi chọn nhạc: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedSoundsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showSoundEffectSelectionDialog() {
        lifecycleScope.launch {
            try {
                val availableEffects = soundManager.getAvailableSoundEffects()
                val effectNames = availableEffects.map { effect ->
                    when (effect) {
                        "hieuung" -> "Hiệu Ứng Mặc Định"
                        "hieuung1" -> "Hiệu Ứng 1"
                        "hieuung2" -> "Hiệu Ứng 2"
                        "hieuung3" -> "Hiệu Ứng 3"
                        "hieuung4" -> "Hiệu Ứng 4"
                        "hieuung5" -> "Hiệu Ứng 5"
                        else -> effect
                    }
                }
                
                val currentEffect = soundManager.getSelectedSoundEffect()
                val currentIndex = availableEffects.indexOf(currentEffect)
                
                androidx.appcompat.app.AlertDialog.Builder(this@AdvancedSoundsActivity)
                    .setTitle(getString(R.string.select_sound_effect))
                    .setSingleChoiceItems(effectNames.toTypedArray(), currentIndex) { dialog, which ->
                        lifecycleScope.launch {
                            try {
                                soundManager.setSelectedSoundEffect(availableEffects[which])
                                soundManager.playButtonClickSound()
                                Toast.makeText(this@AdvancedSoundsActivity, "Đã chọn: ${effectNames[which]}", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this@AdvancedSoundsActivity, "Lỗi chọn hiệu ứng: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@AdvancedSoundsActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Removed onDestroy to prevent stopping background music
    // Background music should continue playing across activities
}
