package com.uef.coloring_app.data.repository

import com.uef.coloring_app.data.local.dao.AchievementDao
import com.uef.coloring_app.data.model.Achievement
import kotlinx.coroutines.flow.Flow

class AchievementRepository(private val achievementDao: AchievementDao) {

    fun getAllAchievements(): Flow<List<Achievement>> = achievementDao.getAllAchievements()

    suspend fun getAchievementById(achievementId: String): Achievement? = 
        achievementDao.getAchievementById(achievementId)

    suspend fun getUnlockedAchievements(): List<Achievement> = 
        achievementDao.getUnlockedAchievements()

    suspend fun getLockedAchievements(): List<Achievement> = 
        achievementDao.getLockedAchievements()

    suspend fun insertAchievement(achievement: Achievement) = 
        achievementDao.insertAchievement(achievement)

    suspend fun updateAchievement(achievement: Achievement) = 
        achievementDao.updateAchievement(achievement)

    suspend fun deleteAchievement(achievement: Achievement) = 
        achievementDao.deleteAchievement(achievement)

    suspend fun getAchievementCount(): Int = achievementDao.getAchievementCount()
}


