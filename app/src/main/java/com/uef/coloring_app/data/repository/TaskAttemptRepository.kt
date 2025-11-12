package com.uef.coloring_app.data.repository

import com.uef.coloring_app.data.local.dao.TaskAttemptDao
import com.uef.coloring_app.data.local.entity.TaskAttemptEntity
import kotlinx.coroutines.flow.Flow

class TaskAttemptRepository(private val taskAttemptDao: TaskAttemptDao) {

    fun getAllAttempts(): Flow<List<TaskAttemptEntity>> = taskAttemptDao.getAllAttempts()

    suspend fun getAttemptsByUser(userId: String): List<TaskAttemptEntity> = 
        taskAttemptDao.getAttemptsByUser(userId)

    suspend fun getAttemptsByTask(taskId: String): List<TaskAttemptEntity> = 
        taskAttemptDao.getAttemptsByTask(taskId)

    suspend fun getUserTaskAttempts(userId: String, taskId: String): List<TaskAttemptEntity> = 
        taskAttemptDao.getUserTaskAttempts(userId, taskId)

    suspend fun getTopAttempts(limit: Int): List<TaskAttemptEntity> = 
        taskAttemptDao.getTopAttempts(limit)

    suspend fun insertAttempt(attempt: TaskAttemptEntity) = 
        taskAttemptDao.insertAttempt(attempt)

    suspend fun updateAttempt(attempt: TaskAttemptEntity) = 
        taskAttemptDao.updateAttempt(attempt)

    suspend fun deleteAttempt(attempt: TaskAttemptEntity) = 
        taskAttemptDao.deleteAttempt(attempt)

    suspend fun deleteAllAttempts() =
        taskAttemptDao.deleteAll()
}
