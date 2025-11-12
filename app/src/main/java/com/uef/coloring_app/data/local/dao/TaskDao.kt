package com.uef.coloring_app.data.local.dao

import androidx.room.*
import com.uef.coloring_app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    
    @Query("SELECT * FROM tasks WHERE isActive = 1")
    suspend fun getActiveTasks(): List<TaskEntity>
    
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: String): TaskEntity?
    
    @Query("SELECT * FROM tasks WHERE difficulty = :difficulty")
    suspend fun getTasksByDifficulty(difficulty: String): List<TaskEntity>
    
    @Query("SELECT * FROM tasks")
    fun getAllTasks(): Flow<List<TaskEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)
    
    @Update
    suspend fun updateTask(task: TaskEntity)
    
    @Delete
    suspend fun deleteTask(task: TaskEntity)
    
    @Query("UPDATE tasks SET isActive = 0 WHERE id = :taskId")
    suspend fun deactivateTask(taskId: String)
}
