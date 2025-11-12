package com.uef.coloring_app.ui.drawing

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Path
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.slider.Slider
import com.uef.coloring_app.R
import com.uef.coloring_app.core.service.TimerService
import com.uef.coloring_app.core.achievements.AchievementManager
import com.uef.coloring_app.core.haptic.HapticManager
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.local.entity.TaskAttemptEntity
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class DrawingActivity : BaseActivity() {
    
    private lateinit var taskNameTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var exitButton: ImageButton
    private lateinit var shapeImageView: ImageView
    private lateinit var drawingView: DrawingView
    private lateinit var undoButton: Button
    private lateinit var redoButton: Button
    private lateinit var clearButton: Button
    private lateinit var submitButton: Button
    
    // Color buttons
    private lateinit var colorRed: android.view.View
    private lateinit var colorBlue: android.view.View
    private lateinit var colorGreen: android.view.View
    private lateinit var colorYellow: android.view.View
    private lateinit var colorOrange: android.view.View
    private lateinit var colorPurple: android.view.View
    
    // Color intensity control
    private lateinit var colorIntensitySlider: Slider
    private lateinit var intensityLabel: TextView
    
    private var countDownTimer: CountDownTimer? = null
    private var currentIntensity = 500
    private var currentBaseColor = Color.RED
    
    // Track timing for accurate results
    private var startTime: Long = 0
    private var endTime: Long = 0
    private var timeLimit: Long = 5 * 60 * 1000L // 5 minutes in milliseconds
    
    // Database and repositories
    private lateinit var taskAttemptRepository: TaskAttemptRepository
    private lateinit var taskRepository: com.uef.coloring_app.data.repository.TaskRepository
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var achievementManager: AchievementManager
    
    // Task info
    private var maxPoints: Int = 100 // Mặc định 100 điểm nếu không lấy được
    private var currentTaskId: String = ""
    
    companion object {
        private const val PREFS_NAME = "user_profile"
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drawing)
        
        // Hide action bar
        supportActionBar?.hide()
        
        // Initialize SharedPreferences and database
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val database = ColoringDatabase.getDatabase(this)
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        taskRepository = com.uef.coloring_app.data.repository.TaskRepository(database.taskDao())
        achievementManager = AchievementManager(this)
        
        initViews()
        loadTaskInfo() // Load task info first
        loadShapeImage()
        setupColorPalette()
        setupIntensitySlider()
        setupClickListeners()
        startTimer()
        startTime = System.currentTimeMillis()
    }
    
    private fun initViews() {
        taskNameTextView = findViewById(R.id.taskName)
        timerTextView = findViewById(R.id.timer)
        exitButton = findViewById(R.id.exitButton)
        shapeImageView = findViewById(R.id.shapeImageView)
        drawingView = findViewById(R.id.drawingView)
        undoButton = findViewById(R.id.undoButton)
        redoButton = findViewById(R.id.redoButton)
        clearButton = findViewById(R.id.clearButton)
        submitButton = findViewById(R.id.submitButton)
        
        // Color intensity control
        colorIntensitySlider = findViewById(R.id.colorIntensitySlider)
        intensityLabel = findViewById(R.id.intensityLabel)
        
        // Set task name from intent
        val taskName = intent.getStringExtra("task_name") ?: "Nhiệm vụ tô màu"
        taskNameTextView.text = taskName
    }
    
    /**
     * Load task info from database để lấy maxPoints
     */
    private fun loadTaskInfo() {
        lifecycleScope.launch {
            try {
                // Get task ID from intent
                currentTaskId = intent.getStringExtra("task_id") ?: ""
                
                if (currentTaskId.isNotEmpty()) {
                    val task = taskRepository.getTaskById(currentTaskId)
                    task?.let {
                        maxPoints = it.points // Lấy điểm tối đa từ task
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Giữ giá trị mặc định maxPoints = 100
            }
        }
    }
    
    private fun loadShapeImage() {
        val shapeId = intent.getStringExtra("shape_id") ?: "circle"
        
        val shapeDrawable = when (shapeId.lowercase()) {
            "circle" -> R.drawable.shape_circle_outline
            "square" -> R.drawable.shape_square_outline
            "triangle" -> R.drawable.shape_triangle_outline
            "star" -> R.drawable.shape_star_outline
            "heart" -> R.drawable.shape_heart_outline
            "pentagon" -> R.drawable.shape_pentagon_outline
            "hexagon" -> R.drawable.shape_hexagon_outline
            "diamond" -> R.drawable.shape_diamond_outline
            "octagon" -> R.drawable.shape_octagon_outline
            "rectangle" -> R.drawable.shape_rectangle_outline
            "oval" -> R.drawable.shape_oval_outline
            else -> R.drawable.shape_circle_outline // Default
        }
        
        shapeImageView.setImageResource(shapeDrawable)
        
        // Set shape boundary for drawing restriction
        setShapeBoundary(shapeId)
    }
    
    private fun setShapeBoundary(shapeId: String) {
        // Wait for DrawingView to be measured
        drawingView.post {
            val centerX = drawingView.width / 2f
            val centerY = drawingView.height / 2f
            val radius = minOf(drawingView.width, drawingView.height) / 3f
            
            val shapePath = Path()
            
            when (shapeId.lowercase()) {
                "circle" -> {
                    shapePath.addCircle(centerX, centerY, radius, Path.Direction.CW)
                }
                "square" -> {
                    val size = radius * 1.4f
                    val left = centerX - size / 2
                    val top = centerY - size / 2
                    val right = centerX + size / 2
                    val bottom = centerY + size / 2
                    shapePath.addRect(left, top, right, bottom, Path.Direction.CW)
                }
                "triangle" -> {
                    val height = radius * 1.5f
                    val width = radius * 1.3f
                    shapePath.moveTo(centerX, centerY - height / 2)
                    shapePath.lineTo(centerX - width / 2, centerY + height / 2)
                    shapePath.lineTo(centerX + width / 2, centerY + height / 2)
                    shapePath.close()
                }
                "star" -> {
                    createStarPath(shapePath, centerX, centerY, radius)
                }
                "heart" -> {
                    createHeartPath(shapePath, centerX, centerY, radius)
                }
                "pentagon" -> {
                    createPolygonPath(shapePath, centerX, centerY, radius, 5)
                }
                "hexagon" -> {
                    createPolygonPath(shapePath, centerX, centerY, radius, 6)
                }
                "octagon" -> {
                    createPolygonPath(shapePath, centerX, centerY, radius, 8)
                }
                "oval" -> {
                    val ovalWidth = radius * 1.6f
                    val ovalHeight = radius * 1.2f
                    val left = centerX - ovalWidth / 2
                    val top = centerY - ovalHeight / 2
                    val right = centerX + ovalWidth / 2
                    val bottom = centerY + ovalHeight / 2
                    shapePath.addOval(left, top, right, bottom, Path.Direction.CW)
                }
                "rectangle" -> {
                    val rectWidth = radius * 1.6f
                    val rectHeight = radius * 1.2f
                    val left = centerX - rectWidth / 2
                    val top = centerY - rectHeight / 2
                    val right = centerX + rectWidth / 2
                    val bottom = centerY + rectHeight / 2
                    shapePath.addRect(left, top, right, bottom, Path.Direction.CW)
                }
                "diamond" -> {
                    createDiamondPath(shapePath, centerX, centerY, radius)
                }
                else -> {
                    shapePath.addCircle(centerX, centerY, radius, Path.Direction.CW)
                }
            }
            
            drawingView.setShapeBoundary(shapePath)
        }
    }
    
    private fun createStarPath(path: Path, centerX: Float, centerY: Float, radius: Float) {
        val outerRadius = radius
        val innerRadius = radius * 0.4f
        val angle = Math.PI / 5.0
        
        path.moveTo(centerX, centerY - outerRadius)
        
        for (i in 1..9) {
            val currentRadius = if (i % 2 == 1) outerRadius else innerRadius
            val x = centerX + currentRadius * Math.sin(i * angle).toFloat()
            val y = centerY - currentRadius * Math.cos(i * angle).toFloat()
            path.lineTo(x, y)
        }
        path.close()
    }
    
    private fun createHeartPath(path: Path, centerX: Float, centerY: Float, radius: Float) {
        val scale = radius / 100f
        path.moveTo(centerX, centerY + 20 * scale)
        path.cubicTo(centerX - 30 * scale, centerY - 10 * scale, centerX - 50 * scale, centerY + 10 * scale, centerX, centerY + 50 * scale)
        path.cubicTo(centerX + 50 * scale, centerY + 10 * scale, centerX + 30 * scale, centerY - 10 * scale, centerX, centerY + 20 * scale)
        path.close()
    }
    
    private fun createPolygonPath(path: Path, centerX: Float, centerY: Float, radius: Float, sides: Int) {
        val angle = 2 * Math.PI / sides
        
        path.moveTo(centerX + radius * Math.cos(0.0).toFloat(), centerY + radius * Math.sin(0.0).toFloat())
        
        for (i in 1 until sides) {
            val x = centerX + radius * Math.cos(i * angle).toFloat()
            val y = centerY + radius * Math.sin(i * angle).toFloat()
            path.lineTo(x, y)
        }
        path.close()
    }
    
    private fun createDiamondPath(path: Path, centerX: Float, centerY: Float, radius: Float) {
        // Diamond is a square rotated 45 degrees
        val size = radius * 1.4f
        path.moveTo(centerX, centerY - size / 2)
        path.lineTo(centerX + size / 2, centerY)
        path.lineTo(centerX, centerY + size / 2)
        path.lineTo(centerX - size / 2, centerY)
        path.close()
    }
    
    private fun setupIntensitySlider() {
        colorIntensitySlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                currentIntensity = value.toInt()
                intensityLabel.text = "Mức độ: $currentIntensity"
                updateCurrentColor()
            }
        }
    }
    
    private fun updateCurrentColor() {
        // Apply intensity to the current base color
        val adjustedColor = adjustColorIntensity(currentBaseColor, currentIntensity)
        drawingView.setColor(adjustedColor)
    }
    
    private fun adjustColorIntensity(baseColor: Int, intensity: Int): Int {
        val alpha = when (intensity) {
            50 -> 0.1f
            100 -> 0.2f
            200 -> 0.3f
            300 -> 0.4f
            400 -> 0.5f
            500 -> 0.7f
            600 -> 0.8f
            700 -> 0.9f
            800 -> 0.95f
            900 -> 1.0f
            else -> 0.7f
        }
        
        val red = Color.red(baseColor)
        val green = Color.green(baseColor)
        val blue = Color.blue(baseColor)
        
        return Color.argb((alpha * 255).toInt(), red, green, blue)
    }
    
    private fun setupColorPalette() {
        // Find color views by ID
        colorRed = findViewById(R.id.colorRed)
        colorBlue = findViewById(R.id.colorBlue)
        colorGreen = findViewById(R.id.colorGreen)
        colorYellow = findViewById(R.id.colorYellow)
        colorOrange = findViewById(R.id.colorOrange)
        colorPurple = findViewById(R.id.colorPurple)
        
        // Set click listeners for each color
        colorRed.setOnClickListener {
            currentBaseColor = Color.RED
            updateCurrentColor()
            Toast.makeText(this, getString(R.string.color_red), Toast.LENGTH_SHORT).show()
        }
        
        colorBlue.setOnClickListener {
            currentBaseColor = Color.BLUE
            updateCurrentColor()
            Toast.makeText(this, getString(R.string.color_blue), Toast.LENGTH_SHORT).show()
        }
        
        colorGreen.setOnClickListener {
            currentBaseColor = Color.GREEN
            updateCurrentColor()
            Toast.makeText(this, getString(R.string.color_green), Toast.LENGTH_SHORT).show()
        }
        
        colorYellow.setOnClickListener {
            currentBaseColor = Color.YELLOW
            updateCurrentColor()
            Toast.makeText(this, getString(R.string.color_yellow), Toast.LENGTH_SHORT).show()
        }
        
        colorOrange.setOnClickListener {
            currentBaseColor = Color.rgb(255, 165, 0) // Orange
            updateCurrentColor()
            Toast.makeText(this, getString(R.string.color_orange), Toast.LENGTH_SHORT).show()
        }
        
        colorPurple.setOnClickListener {
            currentBaseColor = Color.rgb(128, 0, 128) // Purple
            updateCurrentColor()
            Toast.makeText(this, getString(R.string.color_purple), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupClickListeners() {
        exitButton.setOnClickListenerWithSound {
            HapticManager.buttonClick(this)
            // Stop timer
            countDownTimer?.cancel()
            
            // Show confirmation dialog
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.exit_confirm))
                .setMessage(getString(R.string.exit_confirm_message))
                .setPositiveButton(getString(R.string.exit)) { _, _ ->
                    finish() // Return to previous activity
                }
                .setNegativeButton(getString(R.string.cancel), null)
                .show()
        }
        
        undoButton.setOnClickListenerWithSound {
            HapticManager.buttonClick(this)
            drawingView.undo()
        }
        
        redoButton.setOnClickListenerWithSound {
            HapticManager.buttonClick(this)
            drawingView.redo()
        }
        
        clearButton.setOnClickListenerWithSound {
            HapticManager.buttonClick(this)
            drawingView.clear()
            Toast.makeText(this, getString(R.string.cleared_all), Toast.LENGTH_SHORT).show()
        }
        
        submitButton.setOnClickListenerWithSound {
            // Rung khi hoàn thành nhiệm vụ
            HapticManager.taskCompleted(this)
            
            // Stop timer
            countDownTimer?.cancel()
            
            // Calculate actual time spent
            endTime = System.currentTimeMillis()
            val actualTimeSpent = endTime - startTime
            
            // Calculate score based on actual performance
            val coverage = drawingView.getColoredCoverage()
            val score = calculateScore(actualTimeSpent, coverage)
            
            // Get task ID from intent
            val taskId = intent.getStringExtra("task_id") ?: UUID.randomUUID().toString()
            
            // Get current user ID
            val currentUserId = sharedPreferences.getString(KEY_CURRENT_USER_ID, null)
            
            // Check if user is logged in
            if (currentUserId == null) {
                Toast.makeText(this@DrawingActivity, "Vui lòng đăng nhập để lưu tiến độ.", Toast.LENGTH_SHORT).show()
                finish()
                return@setOnClickListenerWithSound
            }
            
            // Save attempt to database and calculate rank
            lifecycleScope.launch {
                val userId = currentUserId
                saveAttemptToDatabase(userId, taskId, score, actualTimeSpent)
                
                // Check and unlock achievements based on real data
                val unlockedAchievements = achievementManager.checkAndUnlockAchievements(
                    userId = userId
                )
                
                // Show notifications for unlocked achievements
                if (unlockedAchievements.isNotEmpty()) {
                    achievementManager.showMultipleAchievementsUnlocked(this@DrawingActivity, unlockedAchievements, userId)
                }
                
                // Update user level based on total score
                val newLevel = achievementManager.updateUserLevel(userId)
                val currentLevel = sharedPreferences.getInt("user_level", 1)
                if (newLevel > currentLevel) {
                    sharedPreferences.edit().putInt("user_level", newLevel).apply()
                    // Ghép tên các achievement vừa mở khóa (nếu có) để thông báo
                    val unlockedNames = if (unlockedAchievements.isNotEmpty())
                        unlockedAchievements.joinToString(limit = 3, truncated = "…") { it.name }
                    else null
                    val levelMsg = if (unlockedNames != null) {
                        "🎉 Bạn đã lên Level $newLevel!\nĐã mở: $unlockedNames"
                    } else {
                        "🎉 Bạn đã lên Level $newLevel!"
                    }
                    Toast.makeText(this@DrawingActivity, levelMsg, Toast.LENGTH_LONG).show()
                    // Gửi notification đến máy người dùng
                    com.uef.coloring_app.core.notification.PushNotificationService(this@DrawingActivity)
                        .showLevelUpNotification(newLevel, unlockedNames)
                }
                
                // Calculate rank from leaderboard after saving
                val rank = calculateRealRank()
                
                // Go to result activity with actual data
                val resultIntent = Intent(this@DrawingActivity, com.uef.coloring_app.ui.result.ResultActivity::class.java)
                resultIntent.putExtra("score", score)
                resultIntent.putExtra("time_spent", actualTimeSpent)
                resultIntent.putExtra("rank", rank)
                startActivity(resultIntent)
                finish()
            }
        }
    }
    
    private fun startTimer() {
        // 5 minutes countdown
        val duration = 5 * 60 * 1000L
        
        countDownTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
            }
            
            override fun onFinish() {
                timerTextView.text = "00:00"
                Toast.makeText(this@DrawingActivity, "Hết giờ!", Toast.LENGTH_LONG).show()
                
                // Record end time before auto submit
                endTime = System.currentTimeMillis()
                
                // Auto submit
                submitButton.performClick()
            }
        }.start()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
    
    /**
     * Calculate score based on coverage (how much of the shape is colored)
     * Score = coverage * maxPoints (điểm tối đa do admin quy định)
     */
    private fun calculateScore(timeSpent: Long, coverage: Float): Int {
        // Tính điểm chính dựa trên % coverage
        // Score = % diện tích đã tô * điểm tối đa
        var score = (coverage * maxPoints).toInt()
        
        // Bonus nếu hoàn thành trong thời gian quy định
        if (timeSpent <= timeLimit) {
            // Bonus 10% nếu hoàn thành đúng hạn
            score += (score * 0.1f).toInt()
        }
        
        // Đảm bảo điểm không vượt quá maxPoints
        return score.coerceIn(0, maxPoints)
    }
    
    /**
     * Save attempt to database
     * Returns the attempt ID
     */
    private suspend fun saveAttemptToDatabase(
        userId: String,
        taskId: String,
        score: Int,
        timeSpent: Long
    ): String {
        return try {
            // Lưu hình ảnh vào file
            val imagePath = saveDrawingImage()
            
            val attemptId = UUID.randomUUID().toString()
            val attempt = TaskAttemptEntity(
                id = attemptId,
                userId = userId,
                taskId = taskId,
                score = score,
                timeSpent = timeSpent,
                completedAt = System.currentTimeMillis(),
                shapesColored = drawingView.getStrokeCount(), // Number of strokes used
                accuracy = (score / maxPoints.toFloat()).coerceIn(0f, 1f), // Accuracy based on score/maxPoints
                isCompleted = true,
                imagePath = imagePath
            )
            
            taskAttemptRepository.insertAttempt(attempt)
            
            // Update user score in SharedPreferences
            val currentScore = sharedPreferences.getInt("user_score", 0)
            val updatedScore = currentScore + score
            sharedPreferences.edit().putInt("user_score", updatedScore).apply()
            
            attemptId
        } catch (e: Exception) {
            e.printStackTrace()
            UUID.randomUUID().toString()
        }
    }
    
    /**
     * Calculate real rank from leaderboard database
     */
    private suspend fun calculateRealRank(): Int {
        return try {
            // Find the current attempt
            val currentUserId = sharedPreferences.getString(KEY_CURRENT_USER_ID, null)
            if (currentUserId == null) return 0
            
            // Find rank of user (based on total score across all tasks)
            val database = ColoringDatabase.getDatabase(this)
            val allUserAttempts = database.taskAttemptDao().getTopAttempts(1000) // Get all attempts
                .filter { it.isCompleted && it.userId == currentUserId }
            
            // Calculate total score for current user (value used via ranking aggregation below)
            allUserAttempts.sumOf { it.score }
            
            // Get total scores for all users
            val allAttemptsForRanking = database.taskAttemptDao().getTopAttempts(1000)
                .filter { it.isCompleted }
            val userScores = allAttemptsForRanking.groupBy { it.userId }
                .mapValues { (_, attempts) -> attempts.sumOf { it.score } }
                .entries
                .sortedByDescending { it.value }
            
            // Find rank
            val rank = userScores.indexOfFirst { it.key == currentUserId } + 1
            if (rank == 0) 999 // Not found, rank last
            else rank
        } catch (e: Exception) {
            e.printStackTrace()
            999
        }
    }
    
    /**
     * Lưu hình ảnh drawing vào file
     */
    private fun saveDrawingImage(): String? {
        return try {
            val bitmap = drawingView.getBitmap()
            if (bitmap != null) {
                val imagesDir = File(filesDir, "colored_images")
                if (!imagesDir.exists()) {
                    imagesDir.mkdirs()
                }
                
                val fileName = "task_${UUID.randomUUID()}.jpg"
                val imageFile = File(imagesDir, fileName)
                
                FileOutputStream(imageFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                
                imageFile.absolutePath
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
