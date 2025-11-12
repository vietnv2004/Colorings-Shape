package com.uef.coloring_app.core.haptic

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.uef.coloring_app.R

/**
 * Extension functions for easy haptic feedback integration
 */

fun Activity.setupHapticFeedback(view: View) {
    view.setOnClickListener {
        HapticManager.triggerSingleTap()
    }
}

fun Activity.setupHapticFeedback(button: Button) {
    button.setOnClickListener {
        HapticManager.triggerSingleTap()
    }
}

fun Activity.setupHapticFeedback(imageView: ImageView) {
    imageView.setOnClickListener {
        HapticManager.triggerSingleTap()
    }
}

fun Activity.setupHapticFeedback(textView: TextView) {
    textView.setOnClickListener {
        HapticManager.triggerSingleTap()
    }
}

fun Activity.setupLongPressHaptic(view: View, action: () -> Unit) {
    view.setOnLongClickListener {
        HapticManager.triggerLongPress()
        action()
        true
    }
}

fun Activity.setupSuccessHaptic(view: View) {
    view.setOnClickListener {
        HapticManager.triggerSuccess()
    }
}

fun Activity.setupErrorHaptic(view: View) {
    view.setOnClickListener {
        HapticManager.triggerError()
    }
}

fun Activity.setupNotificationHaptic(view: View) {
    view.setOnClickListener {
        HapticManager.triggerNotification()
    }
}

fun Activity.setupDoubleTapHaptic(view: View, action: () -> Unit) {
    var lastTapTime = 0L
    val doubleTapDelay = 300L
    
    view.setOnClickListener {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastTapTime < doubleTapDelay) {
            HapticManager.triggerDoubleTap()
            action()
        } else {
            HapticManager.triggerSingleTap()
        }
        lastTapTime = currentTime
    }
}

fun Activity.setupCustomHaptic(view: View, pattern: LongArray, amplitudes: IntArray? = null) {
    view.setOnClickListener {
        HapticManager.triggerCustomPattern(pattern, amplitudes)
    }
}

fun Activity.setupHapticIntensity(intensity: Int) {
    HapticManager.setIntensity(intensity)
}

fun Activity.enableHaptic(enabled: Boolean) {
    HapticManager.setEnabled(enabled)
}

fun Activity.isHapticEnabled(): Boolean = HapticManager.isHapticEnabled()

fun Activity.getHapticIntensity(): Int = HapticManager.getIntensity()
