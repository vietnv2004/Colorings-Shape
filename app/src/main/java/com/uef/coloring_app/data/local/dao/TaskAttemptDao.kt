package com.uef.coloring_app.data.local.dao

import androidx.room.*
import com.uef.coloring_app.data.local.entity.TaskAttemptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskAttemptDao {
    
    @Query("SELECT * FROM task_attempts WHERE userId = :userId ORDER BY completedAt DESC")
    suspend fun getAttemptsByUser(userId: String): List<TaskAttemptEntity>
    
    @Query("SELECT * FROM task_attempts WHERE taskId = :taskId ORDER BY score DESC")
    suspend fun getAttemptsByTask(taskId: String): List<TaskAttemptEntity>
    
    @Query("SELECT * FROM task_attempts WHERE userId = :userId AND taskId = :taskId")
    suspend fun getUserTaskAttempts(userId: String, taskId: String): List<TaskAttemptEntity>
    
    @Query("SELECT * FROM task_attempts ORDER BY score DESC LIMIT :limit")
    suspend fun getTopAttempts(limit: Int): List<TaskAttemptEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttempt(attempt: TaskAttemptEntity)
    
    @Update
    suspend fun updateAttempt(attempt: TaskAttemptEntity)
    
    @Delete
    suspend fun deleteAttempt(attempt: TaskAttemptEntity)
    
    @Query("SELECT * FROM task_attempts")
    fun getAllAttempts(): Flow<List<TaskAttemptEntity>>

    @Query("DELETE FROM task_attempts")
    suspend fun deleteAll()
}
