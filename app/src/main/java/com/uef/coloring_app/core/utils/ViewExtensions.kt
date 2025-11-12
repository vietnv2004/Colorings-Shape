package com.uef.coloring_app.core.utils

import android.view.View
import com.uef.coloring_app.ColoringApplication
import com.uef.coloring_app.core.haptic.HapticManager

/**
 * Extension function to set a click listener that plays a sound effect and haptic feedback when clicked
 */
fun View.setOnClickListenerWithSound(action: () -> Unit) {
    this.setOnClickListener {
        // Play button click sound
        ColoringApplication.soundManager.playButtonClickSound()
        // Trigger haptic feedback
        HapticManager.triggerLightHaptic()
        // Execute the action
        action()
    }
}

