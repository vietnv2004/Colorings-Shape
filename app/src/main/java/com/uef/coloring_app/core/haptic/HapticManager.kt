package com.uef.coloring_app.core.haptic

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object HapticManager {
    
    private var vibrator: Vibrator? = null
    private var isEnabled = true // Enabled by default for button clicks
    private var intensity = 50 // 0-100
    
    fun initialize(context: Context) {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        loadSettings(context)
    }
    
    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
    
    fun setIntensity(newIntensity: Int) {
        intensity = newIntensity.coerceIn(0, 100)
    }
    
    fun triggerLightHaptic() {
        if (!isEnabled || vibrator == null) return
        
        val duration = (50 * intensity / 100f).toLong()
        val amplitude = (255 * intensity / 100f).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
    
    fun triggerMediumHaptic() {
        if (!isEnabled || vibrator == null) return
        
        val duration = (100 * intensity / 100f).toLong()
        val amplitude = (255 * intensity / 100f).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
    
    fun triggerHeavyHaptic() {
        if (!isEnabled || vibrator == null) return
        
        val duration = (200 * intensity / 100f).toLong()
        val amplitude = (255 * intensity / 100f).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
    
    fun triggerSingleTap() {
        if (!isEnabled || vibrator == null) return
        
        val duration = (50 * intensity / 100f).toLong()
        val amplitude = (255 * intensity / 100f).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
    
    fun triggerDoubleTap() {
        if (!isEnabled || vibrator == null) return
        
        val amplitude = (255 * intensity / 100f).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 50, 100, 50),
                intArrayOf(0, amplitude, 0, amplitude),
                -1
            )
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(200)
        }
    }
    
    fun triggerLongPress() {
        if (!isEnabled || vibrator == null) return
        
        val duration = (200 * intensity / 100f).toLong()
        val amplitude = (255 * intensity / 100f).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(duration, amplitude)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(duration)
        }
    }
    
    fun buttonClick(@Suppress("UNUSED_PARAMETER") context: Context) {
        triggerLightHaptic()
    }
    
    fun success(@Suppress("UNUSED_PARAMETER") context: Context) {
        triggerSuccess()
    }
    
    fun taskCompleted(@Suppress("UNUSED_PARAMETER") context: Context) {
        triggerSuccess()
    }
    
    fun drawing(@Suppress("UNUSED_PARAMETER") context: Context) {
        triggerLightHaptic()
    }
    
    fun triggerSuccess() {
        if (!isEnabled || vibrator == null) return
        
        val amplitude = (255 * intensity / 100f).toInt()
        
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 100, 50, 100),
                intArrayOf(0, amplitude, 0, amplitude),
                -1
            )
            vibrator?.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 100, 50, 100), -1)
        }
    }
    
    fun triggerError() {
        if (!isEnabled || vibrator == null) return
        
        val amplitude = (255 * intensity / 100f).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 200, 100, 200),
                intArrayOf(0, amplitude, 0, amplitude),
                -1
            )
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 200, 100, 200), -1)
        }
    }
    
    fun triggerNotification() {
        if (!isEnabled || vibrator == null) return
        
        val amplitude = (255 * intensity / 100f).toInt()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                longArrayOf(0, 300, 100, 300),
                intArrayOf(0, amplitude, 0, amplitude),
                -1
            )
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(longArrayOf(0, 300, 100, 300), -1)
        }
    }
    
    fun triggerCustomPattern(pattern: LongArray, amplitudes: IntArray? = null) {
        if (!isEnabled || vibrator == null) return
        
        val adjustedAmplitudes = amplitudes?.map { (it * intensity / 100f).toInt() }?.toIntArray()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(pattern, adjustedAmplitudes, -1)
            vibrator?.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, -1)
        }
    }
    
    private fun loadSettings(context: Context) {
        val prefs = context.getSharedPreferences("haptic_settings", Context.MODE_PRIVATE)
        isEnabled = prefs.getBoolean("haptic_enabled", true) // Enabled by default
        intensity = prefs.getInt("haptic_intensity", 50)
    }
    
    fun saveSettings(context: Context) {
        val prefs = context.getSharedPreferences("haptic_settings", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("haptic_enabled", isEnabled)
        editor.putInt("haptic_intensity", intensity)
        editor.apply()
    }
    
    fun isHapticEnabled(): Boolean = isEnabled
    
    fun getIntensity(): Int = intensity
}