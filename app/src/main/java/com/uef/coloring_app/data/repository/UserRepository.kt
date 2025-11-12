package com.uef.coloring_app.data.repository

import com.uef.coloring_app.data.local.dao.UserDao
import com.uef.coloring_app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    
    fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    
    suspend fun getUserById(userId: String): UserEntity? {
        return userDao.getUserById(userId)
    }
    
    suspend fun getUserByEmail(email: String): UserEntity? {
        return userDao.getUserByEmail(email)
    }
    
    suspend fun getParticipantUsers(): List<UserEntity> {
        return userDao.getParticipantUsers()
    }
    
    suspend fun getAdminUsers(): List<UserEntity> {
        return userDao.getAdminUsers()
    }
    
    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }
    
    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }
    
    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }
    
    suspend fun getUserCount(): Int {
        return userDao.getUserCount()
    }
}


