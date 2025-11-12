package com.uef.coloring_app.core.service

import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.local.entity.UserEntity
import com.uef.coloring_app.data.local.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to initialize database with sample data
 */
@Singleton
class DatabaseInitializationService @Inject constructor(
    private val database: ColoringDatabase
) {
    
    fun initializeDatabase(scope: CoroutineScope) {
        scope.launch(Dispatchers.IO) {
            try {
                // Check if database is already initialized
                val userCount = database.userDao().getUserCount()
                if (userCount > 0) return@launch
                
                val currentTime = System.currentTimeMillis()
                
                // Initialize with admin user
                val adminUser = UserEntity(
                    id = "admin_001",
                    email = "admin@uef.edu.vn",
                    name = "Administrator",
                    password = hashPassword("admin123"),
                    birthYear = 1990,
                    gender = "Other",
                    role = "admin",
                    isActive = true,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                
                // Initialize with sample student user
                val studentUser = UserEntity(
                    id = "student_001",
                    email = "student1@uef.edu.vn",
                    name = "Student One",
                    password = hashPassword("student123"),
                    birthYear = 2000,
                    gender = "Male",
                    role = "participant",
                    isActive = true,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
                
                database.userDao().insert(adminUser)
                database.userDao().insert(studentUser)
                
            } catch (e: Exception) {
                // Log error but don't crash the app
                e.printStackTrace()
            }
        }
    }
    
    private fun hashPassword(password: String): String {
        // Simple hash for demo - in production use bcrypt or similar
        return password.hashCode().toString()
    }
}