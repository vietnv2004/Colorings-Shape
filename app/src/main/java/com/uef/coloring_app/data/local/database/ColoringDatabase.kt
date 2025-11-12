package com.uef.coloring_app.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.uef.coloring_app.data.local.dao.AchievementDao
import com.uef.coloring_app.data.local.dao.TaskAttemptDao
import com.uef.coloring_app.data.local.dao.TaskDao
import com.uef.coloring_app.data.local.dao.UserDao
import com.uef.coloring_app.data.local.entity.TaskAttemptEntity
import com.uef.coloring_app.data.local.entity.TaskEntity
import com.uef.coloring_app.data.local.entity.UserEntity
import com.uef.coloring_app.data.model.Achievement

@Database(
    entities = [
        UserEntity::class,
        TaskEntity::class,
        TaskAttemptEntity::class,
        Achievement::class
    ],
    version = 4,
    exportSchema = false
)
abstract class ColoringDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun taskDao(): TaskDao
    abstract fun taskAttemptDao(): TaskAttemptDao
    abstract fun achievementDao(): AchievementDao
    
    companion object {
        @Volatile
        private var INSTANCE: ColoringDatabase? = null
        
        fun getDatabase(context: Context): ColoringDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ColoringDatabase::class.java,
                    "coloring_database"
                )
                .fallbackToDestructiveMigration() // Allow destructive migration for simplicity
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}