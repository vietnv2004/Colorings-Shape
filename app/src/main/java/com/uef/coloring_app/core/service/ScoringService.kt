package com.uef.coloring_app.core.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.uef.coloring_app.core.utils.ErrorLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Service for calculating and managing scores
 */
class ScoringService : Service() {
    
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        /**
         * Calculate score based on accuracy, time, and difficulty
         */
        fun calculateScore(
            accuracy: Float,
            timeSpent: Long,
            timeLimit: Long,
            difficulty: String
        ): Int {
            try {
                // Base score from accuracy (0-100)
                val accuracyScore = (accuracy * 100).toInt()
                
                // Time bonus (faster completion = higher bonus)
                val timeRatio = if (timeLimit > 0) {
                    1.0f - (timeSpent.toFloat() / timeLimit.toFloat())
                } else {
                    0f
                }
                val timeBonus = (timeRatio * 50).toInt().coerceIn(0, 50)
                
                // Difficulty multiplier
                val difficultyMultiplier = when (difficulty.lowercase()) {
                    "easy" -> 1.0f
                    "medium" -> 1.5f
                    "hard" -> 2.0f
                    else -> 1.0f
                }
                
                // Calculate final score
                val baseScore = accuracyScore + timeBonus
                val finalScore = (baseScore * difficultyMultiplier).toInt()
                
                return finalScore.coerceIn(0, 1000)
            } catch (e: Exception) {
                ErrorLogger.logError("Error calculating score", e)
                return 0
            }
        }
        
        /**
         * Calculate accuracy based on colored pixels
         */
        fun calculateAccuracy(
            coloredPixels: Int,
            totalPixels: Int
        ): Float {
            return if (totalPixels > 0) {
                (coloredPixels.toFloat() / totalPixels.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle scoring service commands
        return START_NOT_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}


