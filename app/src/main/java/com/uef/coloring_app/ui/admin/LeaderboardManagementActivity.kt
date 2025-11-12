package com.uef.coloring_app.ui.admin

import android.os.Bundle
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.local.entity.TaskAttemptEntity
import com.uef.coloring_app.data.local.entity.UserEntity
import com.uef.coloring_app.data.local.entity.TaskEntity
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.data.repository.TaskRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LeaderboardManagementActivity : BaseActivity() {

    private lateinit var filterSpinner: Spinner
    private lateinit var shapeFilterSpinner: Spinner
    private lateinit var metricSpinner: Spinner
    private lateinit var totalAttemptsTextView: TextView
    private lateinit var leaderboardRecyclerView: RecyclerView
    private lateinit var exportButton: com.google.android.material.button.MaterialButton
    private lateinit var deleteAllButton: com.google.android.material.button.MaterialButton

    private lateinit var attemptRepository: TaskAttemptRepository
    private lateinit var userRepository: UserRepository
    private lateinit var taskRepository: TaskRepository
    private lateinit var leaderboardAdapter: LeaderboardAdapter
    private var allAttempts: List<TaskAttemptEntity> = emptyList()
    private var allUsers: Map<String, UserEntity> = emptyMap()
    private var allTasks: Map<String, TaskEntity> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard_management)
        supportActionBar?.hide()

        val database = ColoringDatabase.getDatabase(applicationContext)
        attemptRepository = TaskAttemptRepository(database.taskAttemptDao())
        userRepository = UserRepository(database.userDao())
        taskRepository = TaskRepository(database.taskDao())

        initViews()
        setupListeners()
        setupRecyclerView()
        loadUsersAndTasks()
        observeAttempts()
    }

    private fun initViews() {
        filterSpinner = findViewById(R.id.filterSpinner)
        shapeFilterSpinner = findViewById(R.id.shapeFilterSpinner)
        metricSpinner = findViewById(R.id.metricSpinner)
        totalAttemptsTextView = findViewById(R.id.totalAttemptsTextView)
        leaderboardRecyclerView = findViewById(R.id.leaderboardRecyclerView)
        exportButton = findViewById(R.id.exportButton)
        deleteAllButton = findViewById(R.id.deleteAllButton)
    }

    private fun setupListeners() {
        val filterOptions = arrayOf(
            getString(R.string.filter_all),
            getString(R.string.filter_top_10),
            getString(R.string.filter_top_50),
            getString(R.string.filter_top_100)
        )
        filterSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filterOptions)
        
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterAttempts(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Shape filter spinner
        val shapeOptions = arrayOf(
            getString(R.string.all_tasks),
            "Hình tròn", "Hình vuông", "Hình tam giác", "Hình ngôi sao",
            "Hình trái tim", "Hình ngũ giác", "Hình lục giác", "Hình bát giác",
            "Hình oval", "Hình chữ nhật", "Hình thoi"
        )
        shapeFilterSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, shapeOptions)
        shapeFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterAttempts(filterSpinner.selectedItemPosition)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Metric spinner: điểm từng lượt hoặc tổng điểm theo user
        val metricOptions = arrayOf(
            getString(R.string.metric_single_score),
            getString(R.string.metric_total_score)
        )
        metricSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, metricOptions)
        metricSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterAttempts(filterSpinner.selectedItemPosition)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        exportButton.setOnClickListener { exportCurrentToCsv() }
        deleteAllButton.setOnClickListener { confirmDeleteAll() }
    }

    private fun setupRecyclerView() {
        leaderboardAdapter = LeaderboardAdapter(emptyList(), emptyMap(), emptyMap())
        leaderboardRecyclerView.layoutManager = LinearLayoutManager(this)
        leaderboardRecyclerView.adapter = leaderboardAdapter
    }
    
    private fun loadUsersAndTasks() {
        lifecycleScope.launch {
            userRepository.getAllUsers().collectLatest { users ->
                allUsers = users.associateBy { it.id }
                updateAdapter()
            }
        }
        
        lifecycleScope.launch {
            taskRepository.getAllTasks().collectLatest { tasks ->
                allTasks = tasks.associateBy { it.id }
                updateAdapter()
            }
        }
    }
    
    private fun updateAdapter() {
        leaderboardAdapter = LeaderboardAdapter(allAttempts, allUsers, allTasks)
        leaderboardRecyclerView.adapter = leaderboardAdapter
    }

    private fun observeAttempts() {
        lifecycleScope.launch {
            attemptRepository.getAllAttempts().collectLatest { attempts ->
                allAttempts = attempts.sortedByDescending { it.score }
                filterAttempts(filterSpinner.selectedItemPosition)
            }
        }
    }

    private fun filterAttempts(filterPosition: Int) {
        // Lọc theo hình
        val selectedShapeId = when (shapeFilterSpinner.selectedItemPosition) {
            1 -> "circle"
            2 -> "square"
            3 -> "triangle"
            4 -> "star"
            5 -> "heart"
            6 -> "pentagon"
            7 -> "hexagon"
            8 -> "octagon"
            9 -> "oval"
            10 -> "rectangle"
            11 -> "diamond"
            else -> null
        }

        var filteredBase = if (selectedShapeId == null) {
            allAttempts
        } else {
            val taskIds = allTasks.values.filter { it.shapeId.equals(selectedShapeId, true) }.map { it.id }.toSet()
            allAttempts.filter { it.taskId in taskIds }
        }

        // Lọc theo metric
        val totalMetric = metricSpinner.selectedItemPosition == 1
        val computedList = if (totalMetric) {
            filteredBase.groupBy { it.userId }.map { (userId, list) ->
                val sum = list.sumOf { it.score }
                val latest = list.maxByOrNull { it.completedAt }
                TaskAttemptEntity(
                    id = "total_$userId",
                    userId = userId,
                    taskId = "__total__",
                    score = sum,
                    timeSpent = 0,
                    completedAt = latest?.completedAt ?: System.currentTimeMillis(),
                    shapesColored = 0,
                    accuracy = 0f,
                    isCompleted = true,
                    imagePath = null
                )
            }.sortedByDescending { it.score }
        } else {
            filteredBase.sortedByDescending { it.score }
        }

        val limited = when (filterPosition) {
            1 -> computedList.take(10)
            2 -> computedList.take(50)
            3 -> computedList.take(100)
            else -> computedList
        }

        // Update adapter
        displayedAttempts = limited
        leaderboardAdapter = LeaderboardAdapter(displayedAttempts, allUsers, allTasks)
        leaderboardRecyclerView.adapter = leaderboardAdapter
        totalAttemptsTextView.text = getString(R.string.total_attempts_text, displayedAttempts.size)
    }

    // Lưu danh sách đang hiển thị để export
    private var displayedAttempts: List<TaskAttemptEntity> = emptyList()

    private fun exportCurrentToCsv() {
        val sb = StringBuilder()
        sb.append("Rank,User,Task,Score,Time,Date\n")
        val dateFmt = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        displayedAttempts.forEachIndexed { index, a ->
            val userName = allUsers[a.userId]?.name ?: a.userId
            val taskName = if (a.taskId == "__total__") getString(R.string.total_points_label) else (allTasks[a.taskId]?.name ?: a.taskId)
            val timeMin = a.timeSpent / 60000
            val timeSec = (a.timeSpent % 60000) / 1000
            val timeStr = if (a.taskId == "__total__") "--:--" else String.format("%02d:%02d", timeMin, timeSec)
            val dateStr = dateFmt.format(java.util.Date(a.completedAt))
            sb.append("${index + 1},${userName},${taskName},${a.score},${timeStr},${dateStr}\n")
        }

        try {
            val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS)
            if (dir != null && !dir.exists()) dir.mkdirs()
            val fileName = "leaderboard_export_" + java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault()).format(java.util.Date()) + ".csv"
            val file = java.io.File(dir, fileName)
            java.io.FileOutputStream(file).use { it.write(sb.toString().toByteArray()) }
            android.widget.Toast.makeText(this, getString(R.string.export_success, file.absolutePath), android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, getString(R.string.export_failed), android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteAll() {
        android.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_all))
            .setMessage(getString(R.string.confirm_delete_all))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launch {
                    attemptRepository.deleteAllAttempts()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}


