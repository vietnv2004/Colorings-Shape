package com.uef.coloring_app.data

import android.content.Context
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.local.entity.TaskAttemptEntity
import com.uef.coloring_app.data.local.entity.TaskEntity
import com.uef.coloring_app.data.local.entity.UserEntity
import com.uef.coloring_app.data.model.Achievement
import com.uef.coloring_app.data.model.AchievementType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

object DatabaseInitializer {

    fun initializeSampleData(context: Context) {
        val database = ColoringDatabase.getDatabase(context)
        val taskDao = database.taskDao()
        val achievementDao = database.achievementDao()

        CoroutineScope(Dispatchers.IO).launch {
            // Chỉ thêm tasks và achievements mẫu, không thêm users mẫu
            val activeTasks = taskDao.getActiveTasks()
            
            if (activeTasks.isEmpty()) {
                // Database trống, chỉ thêm tasks và achievements
                insertSampleTasks(taskDao)
                insertSampleAchievements(achievementDao)
                // Không thêm users và task attempts mẫu
            }
        }
    }

    // Phương thức để xóa và khởi tạo lại database (chỉ dùng khi cần)
    fun reinitializeDatabase(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Xóa database hiện tại
                context.deleteDatabase("coloring_database")
                
                // Khởi tạo lại
                initializeSampleData(context)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun insertSampleUsers(userDao: com.uef.coloring_app.data.local.dao.UserDao): List<UserEntity> {
        val users = listOf(
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "admin@coloring.com",
                name = "Quản trị viên",
                password = "admin123",
                birthYear = 1990,
                gender = "Nam",
                role = "admin",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user1@coloring.com",
                name = "Nguyễn Văn A",
                password = "user123",
                birthYear = 2000,
                gender = "Nam",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user2@coloring.com",
                name = "Trần Thị B",
                password = "user123",
                birthYear = 2001,
                gender = "Nữ",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user3@coloring.com",
                name = "Lê Văn C",
                password = "user123",
                birthYear = 1999,
                gender = "Nam",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user4@coloring.com",
                name = "Phạm Thị D",
                password = "user123",
                birthYear = 2002,
                gender = "Nữ",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user5@coloring.com",
                name = "Hoàng Văn E",
                password = "user123",
                birthYear = 1998,
                gender = "Nam",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user6@coloring.com",
                name = "Võ Thị F",
                password = "user123",
                birthYear = 2003,
                gender = "Nữ",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user7@coloring.com",
                name = "Đặng Văn G",
                password = "user123",
                birthYear = 1997,
                gender = "Nam",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user8@coloring.com",
                name = "Bùi Thị H",
                password = "user123",
                birthYear = 2000,
                gender = "Nữ",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            UserEntity(
                id = UUID.randomUUID().toString(),
                email = "user9@coloring.com",
                name = "Dương Văn I",
                password = "user123",
                birthYear = 2001,
                gender = "Nam",
                role = "participant",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        users.forEach { user ->
            userDao.insertUser(user)
        }
        return users
    }

    private suspend fun insertSampleTasks(taskDao: com.uef.coloring_app.data.local.dao.TaskDao): List<TaskEntity> {
        val tasks = listOf(
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu hình tròn",
                description = "Tô màu vào hình tròn với màu yêu thích của bạn",
                maxDuration = 5 * 60 * 1000L,
                points = 100,
                difficulty = "easy",
                shapeId = "circle",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu hình vuông",
                description = "Tô màu vào hình vuông với màu yêu thích của bạn",
                maxDuration = 5 * 60 * 1000L,
                points = 150,
                difficulty = "medium",
                shapeId = "square",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu hình tam giác",
                description = "Tô màu vào hình tam giác với màu yêu thích của bạn",
                maxDuration = 5 * 60 * 1000L,
                points = 200,
                difficulty = "hard",
                shapeId = "triangle",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu ngôi sao",
                description = "Tô màu vào ngôi sao với nhiều màu sắc",
                maxDuration = 6 * 60 * 1000L,
                points = 180,
                difficulty = "medium",
                shapeId = "star",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu trái tim",
                description = "Tô màu vào trái tim với màu sắc tình yêu",
                maxDuration = 6 * 60 * 1000L,
                points = 220,
                difficulty = "hard",
                shapeId = "heart",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu hình ngũ giác",
                description = "Tô màu vào hình ngũ giác với màu yêu thích",
                maxDuration = 7 * 60 * 1000L,
                points = 160,
                difficulty = "medium",
                shapeId = "pentagon",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu hình lục giác",
                description = "Tô màu vào hình lục giác với nhiều màu",
                maxDuration = 7 * 60 * 1000L,
                points = 190,
                difficulty = "medium",
                shapeId = "hexagon",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu kim cương",
                description = "Tô màu vào hình kim cương lấp lánh",
                maxDuration = 6 * 60 * 1000L,
                points = 210,
                difficulty = "hard",
                shapeId = "diamond",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu hình bát giác",
                description = "Tô màu vào hình bát giác phức tạp",
                maxDuration = 8 * 60 * 1000L,
                points = 250,
                difficulty = "hard",
                shapeId = "octagon",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu hình chữ nhật",
                description = "Tô màu vào hình chữ nhật đơn giản",
                maxDuration = 4 * 60 * 1000L,
                points = 120,
                difficulty = "easy",
                shapeId = "rectangle",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            ),
            TaskEntity(
                id = UUID.randomUUID().toString(),
                name = "Tô màu hình oval",
                description = "Tô màu vào hình oval mượt mà",
                maxDuration = 5 * 60 * 1000L,
                points = 140,
                difficulty = "easy",
                shapeId = "oval",
                colors = "[]",
                isActive = true,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )

        tasks.forEach { task ->
            taskDao.insertTask(task)
        }
        return tasks
    }

    private suspend fun insertSampleAchievements(achievementDao: com.uef.coloring_app.data.local.dao.AchievementDao) {
        val achievements = listOf(
            Achievement(
                id = UUID.randomUUID().toString(),
                name = "Chuyên gia tô màu",
                description = "Hoàn thành 10 nhiệm vụ tô màu",
                icon = "🏆",
                type = AchievementType.TASK_COMPLETION,
                requirement = 10,
                points = 200,
                isUnlocked = false,
                unlockedAt = null
            ),
            Achievement(
                id = UUID.randomUUID().toString(),
                name = "Điểm cao",
                description = "Đạt 90+ điểm trong một nhiệm vụ",
                icon = "⭐",
                type = AchievementType.TOP_SCORER,
                requirement = 90,
                points = 100,
                isUnlocked = false,
                unlockedAt = null
            ),
            Achievement(
                id = UUID.randomUUID().toString(),
                name = "Chuỗi chiến thắng",
                description = "Hoàn thành 5 nhiệm vụ liên tiếp",
                icon = "🔥",
                type = AchievementType.STREAK_MASTER,
                requirement = 5,
                points = 250,
                isUnlocked = false,
                unlockedAt = null
            ),
            Achievement(
                id = UUID.randomUUID().toString(),
                name = "Bậc thầy màu sắc",
                description = "Hoàn thành 50 nhiệm vụ tô màu",
                icon = "🎭",
                type = AchievementType.TASK_COMPLETION,
                requirement = 50,
                points = 500,
                isUnlocked = false,
                unlockedAt = null
            )
        )

        achievements.forEach { achievement ->
            achievementDao.insertAchievement(achievement)
        }
    }

    // Đã xóa hàm insertSampleTaskAttempts - chỉ sử dụng dữ liệu thật từ user
}

