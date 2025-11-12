package com.uef.coloring_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val type: AchievementType,
    val requirement: Int,
    val points: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

enum class AchievementType {
    FIRST_TASK,
    TOP_SCORER,
    STREAK_MASTER,
    SPEED_DEMON,
    PERFECTIONIST,
    SOCIAL_BUTTERFLY,
    TASK_COMPLETION,
    TIME_BASED,
    SCORE_BASED
}
