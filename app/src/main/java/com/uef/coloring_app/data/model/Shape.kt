package com.uef.coloring_app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "shapes")
data class Shape(
    @PrimaryKey
    val id: String,
    val name: String,
    val category: String,
    val svgPath: String,
    val createdAt: Date,
    val createdBy: String
)