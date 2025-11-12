package com.uef.coloring_app.di

import android.content.Context
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.data.repository.TaskRepository
import com.uef.coloring_app.data.repository.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ColoringDatabase {
        return ColoringDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideUserRepository(database: ColoringDatabase): UserRepository {
        return UserRepository(database.userDao())
    }
    
    @Provides
    fun provideTaskRepository(database: ColoringDatabase): TaskRepository {
        return TaskRepository(database.taskDao())
    }
    
    @Provides
    fun provideTaskAttemptRepository(database: ColoringDatabase): TaskAttemptRepository {
        return TaskAttemptRepository(database.taskAttemptDao())
    }
}

