package com.uef.coloring_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val maxDuration: Long, // in milliseconds
    val points: Int,
    val difficulty: String, // "easy", "medium", "hard"
    val shapeId: String,
    val colors: String, // JSON array of colors
    val isActive: Boolean = true,
    val createdAt: Date,
    val updatedAt: Date
)