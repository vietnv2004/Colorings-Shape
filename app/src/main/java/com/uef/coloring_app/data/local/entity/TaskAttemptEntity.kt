package com.uef.coloring_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_attempts")
data class TaskAttemptEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val taskId: String,
    val score: Int,
    val timeSpent: Long,
    val completedAt: Long,
    val shapesColored: Int,
    val accuracy: Float,
    val isCompleted: Boolean,
    val imagePath: String? = null // Đường dẫn hình ảnh đã tô màu
)
