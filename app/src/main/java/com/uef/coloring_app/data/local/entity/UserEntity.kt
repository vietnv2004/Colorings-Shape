package com.uef.coloring_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val password: String,
    val birthYear: Int,
    val gender: String,
    val role: String = "participant",
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val maxDuration: Long,
    val points: Int,
    val difficulty: String,
    val shapeId: String,
    val colors: String,
    val isActive: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long
)