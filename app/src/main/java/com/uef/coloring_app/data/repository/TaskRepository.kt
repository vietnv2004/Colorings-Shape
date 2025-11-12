package com.uef.coloring_app.data.repository

import com.uef.coloring_app.data.local.dao.TaskDao
import com.uef.coloring_app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {

    fun getAllTasks(): Flow<List<TaskEntity>> = taskDao.getAllTasks()

    suspend fun getActiveTasks(): List<TaskEntity> = taskDao.getActiveTasks()

    suspend fun getTaskById(taskId: String): TaskEntity? = taskDao.getTaskById(taskId)

    suspend fun getTasksByDifficulty(difficulty: String): List<TaskEntity> = 
        taskDao.getTasksByDifficulty(difficulty)

    suspend fun insertTask(task: TaskEntity) = taskDao.insertTask(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskEntity) = taskDao.deleteTask(task)

    suspend fun deactivateTask(taskId: String) = taskDao.deactivateTask(taskId)
}


