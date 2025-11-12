package com.uef.coloring_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val email: String,
    val name: String,
    val password: String,
    val birthYear: Int,
    val gender: String,
    val role: String = "participant", // "participant" or "admin"
    val isActive: Boolean = true,
    val createdAt: Date,
    val updatedAt: Date
)