package com.uef.coloring_app.data.local.dao

import androidx.room.*
import com.uef.coloring_app.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    
    @Query("SELECT * FROM achievements")
    fun getAllAchievements(): Flow<List<Achievement>>
    
    @Query("SELECT * FROM achievements WHERE id = :achievementId")
    suspend fun getAchievementById(achievementId: String): Achievement?
    
    @Query("SELECT * FROM achievements WHERE isUnlocked = 1")
    suspend fun getUnlockedAchievements(): List<Achievement>
    
    @Query("SELECT * FROM achievements WHERE isUnlocked = 0")
    suspend fun getLockedAchievements(): List<Achievement>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievement(achievement: Achievement)
    
    @Update
    suspend fun updateAchievement(achievement: Achievement)
    
    @Delete
    suspend fun deleteAchievement(achievement: Achievement)
    
    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getAchievementCount(): Int
}


