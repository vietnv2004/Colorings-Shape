package com.uef.coloring_app.ui.admin

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.local.entity.TaskEntity
import com.uef.coloring_app.data.repository.TaskRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

class TaskManagementActivity : BaseActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var difficultyFilterSpinner: Spinner
    private lateinit var statusFilterSpinner: Spinner
    private lateinit var taskCountTextView: TextView
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var addTaskFab: com.google.android.material.floatingactionbutton.FloatingActionButton

    private lateinit var taskRepository: TaskRepository
    private lateinit var taskAdapter: TaskAdapter
    private var allTasks: List<TaskEntity> = emptyList()
    
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val SHAPE_PREFS_KEY = "custom_shapes"
    
    data class CustomShape(
        val name: String,
        val id: String,
        val svgCode: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("shape_prefs", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_task_management)
        supportActionBar?.hide()

        val taskDao = ColoringDatabase.getDatabase(applicationContext).taskDao()
        taskRepository = TaskRepository(taskDao)

        initViews()
        setupListeners()
        setupRecyclerView()
        observeTasks()
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        difficultyFilterSpinner = findViewById(R.id.difficultyFilterSpinner)
        statusFilterSpinner = findViewById(R.id.statusFilterSpinner)
        taskCountTextView = findViewById(R.id.taskCountTextView)
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView)
        addTaskFab = findViewById(R.id.addTaskFab)
        
        // Set initial text
        taskCountTextView.text = getString(R.string.total_tasks, 0)
    }

    private fun setupListeners() {
        // Search listener
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterTasks()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Difficulty filter
        val difficultyOptions = arrayOf(
            getString(R.string.all_tasks),
            getString(R.string.easy),
            getString(R.string.medium),
            getString(R.string.hard)
        )
        difficultyFilterSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, difficultyOptions)
        difficultyFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterTasks()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Status filter
        val statusOptions = arrayOf(
            getString(R.string.all_tasks),
            getString(R.string.task_status_active),
            getString(R.string.task_status_inactive)
        )
        statusFilterSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, statusOptions)
        statusFilterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                filterTasks()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Add task button
        addTaskFab.setOnClickListener {
            showAddEditTaskDialog(null)
        }
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            tasks = emptyList(),
            onEditClick = { task -> showAddEditTaskDialog(task) },
            onDeleteClick = { task -> showDeleteConfirmation(task) },
            onToggleActive = { task, isActive ->
                lifecycleScope.launch {
                    val updatedTask = task.copy(isActive = isActive, updatedAt = System.currentTimeMillis())
                    taskRepository.updateTask(updatedTask)
                    Toast.makeText(this@TaskManagementActivity, 
                        if (isActive) getString(R.string.task_activated) else getString(R.string.task_deactivated), 
                        Toast.LENGTH_SHORT).show()
                }
            }
        )
        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksRecyclerView.adapter = taskAdapter
    }

    private fun observeTasks() {
        lifecycleScope.launch {
            taskRepository.getAllTasks().collectLatest { tasks ->
                allTasks = tasks
                filterTasks()
            }
        }
    }

    private fun filterTasks() {
        val searchQuery = searchEditText.text.toString().lowercase()
        val difficultyFilter = when (difficultyFilterSpinner.selectedItemPosition) {
            1 -> "easy"
            2 -> "medium"
            3 -> "hard"
            else -> null
        }
        val statusFilter = when (statusFilterSpinner.selectedItemPosition) {
            1 -> true
            2 -> false
            else -> null
        }

        val filteredTasks = allTasks.filter { task ->
            val matchesSearch = task.name.lowercase().contains(searchQuery) || 
                               task.description.lowercase().contains(searchQuery)
            val matchesDifficulty = difficultyFilter == null || task.difficulty == difficultyFilter
            val matchesStatus = statusFilter == null || task.isActive == statusFilter
            
            matchesSearch && matchesDifficulty && matchesStatus
        }

        taskAdapter.updateTasks(filteredTasks)
        taskCountTextView.text = getString(R.string.total_tasks, filteredTasks.size)
    }

    private fun showAddEditTaskDialog(task: TaskEntity?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null)
        
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val difficultySpinner = dialogView.findViewById<Spinner>(R.id.difficultySpinner)
        val shapeSpinner = dialogView.findViewById<Spinner>(R.id.shapeSpinner)
        val pointsEditText = dialogView.findViewById<EditText>(R.id.pointsEditText)
        val durationEditText = dialogView.findViewById<EditText>(R.id.durationEditText)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        val addNewShapeButton = dialogView.findViewById<Button>(R.id.addNewShapeButton)

        // Setup spinners
        val difficultyOptions = arrayOf(
            getString(R.string.easy),
            getString(R.string.medium),
            getString(R.string.hard)
        )
        difficultySpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, difficultyOptions)

        // Load shapes (default + custom)
        val shapeOptions = getShapeOptions()
        val shapeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, shapeOptions)
        shapeSpinner.adapter = shapeAdapter
        
        // Handle add new shape button
        addNewShapeButton.setOnClickListener {
            showAddShapeDialog { updatedOptions ->
                shapeSpinner.adapter = ArrayAdapter(this@TaskManagementActivity, 
                    android.R.layout.simple_spinner_dropdown_item, updatedOptions)
                dialogView.post { shapeSpinner.setSelection(updatedOptions.size - 1) } // Select newly added shape
            }
        }

        // Fill data if editing
        if (task != null) {
            titleTextView.text = getString(R.string.edit_task)
            nameEditText.setText(task.name)
            descriptionEditText.setText(task.description)
            pointsEditText.setText(task.points.toString())
            durationEditText.setText((task.maxDuration / 60000).toString()) // Convert ms to minutes
            
            difficultySpinner.setSelection(when(task.difficulty) {
                "easy" -> 0
                "medium" -> 1
                "hard" -> 2
                else -> 0
            })
            
            // Find position of shape in the list
            val currentShapeId = task.shapeId.lowercase()
            val shapePosition = findShapePosition(currentShapeId)
            shapeSpinner.setSelection(shapePosition)
        } else {
            titleTextView.text = getString(R.string.add_new_task)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val points = pointsEditText.text.toString().toIntOrNull() ?: 0
            val duration = (durationEditText.text.toString().toIntOrNull() ?: 5) * 60000L // Convert to ms
            
            val difficulty = when(difficultySpinner.selectedItemPosition) {
                0 -> "easy"
                1 -> "medium"
                2 -> "hard"
                else -> "easy"
            }
            
            val shapeId = getShapeIdFromPosition(shapeSpinner.selectedItemPosition)

            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_task_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val taskEntity = TaskEntity(
                    id = task?.id ?: UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    maxDuration = duration,
                    points = points,
                    difficulty = difficulty,
                    shapeId = shapeId,
                    colors = "[]", // Empty JSON array
                    isActive = task?.isActive ?: true,
                    createdAt = task?.createdAt ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                if (task != null) {
                    taskRepository.updateTask(taskEntity)
                    Toast.makeText(this@TaskManagementActivity, getString(R.string.task_updated), Toast.LENGTH_SHORT).show()
                } else {
                    taskRepository.insertTask(taskEntity)
                    Toast.makeText(this@TaskManagementActivity, getString(R.string.task_added), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(task: TaskEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_task_title))
            .setMessage(getString(R.string.delete_task_message, task.name))
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                lifecycleScope.launch {
                    taskRepository.deleteTask(task)
                    Toast.makeText(this@TaskManagementActivity, getString(R.string.task_deleted), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun getShapeOptions(): MutableList<String> {
        val defaultShapes = mutableListOf(
            "Hình tròn", "Hình vuông", "Hình tam giác", "Hình ngôi sao",
            "Hình trái tim", "Hình ngũ giác", "Hình lục giác", "Hình bát giác",
            "Hình oval", "Hình chữ nhật", "Hình thoi"
        )
        
        // Load custom shapes from SharedPreferences
        val customShapesJson = sharedPreferences.getString(SHAPE_PREFS_KEY, null)
        if (customShapesJson != null) {
            val type = object : TypeToken<List<CustomShape>>() {}.type
            val customShapes: List<CustomShape> = gson.fromJson(customShapesJson, type)
            defaultShapes.addAll(customShapes.map { it.name })
        }
        
        return defaultShapes
    }
    
    private fun getShapeIdFromPosition(position: Int): String {
        val defaultIds = listOf("circle", "square", "triangle", "star", "heart", 
            "pentagon", "hexagon", "octagon", "oval", "rectangle", "diamond")
        
        if (position < defaultIds.size) {
            return defaultIds[position]
        }
        
        // Get custom shape ID
        val customShapesJson = sharedPreferences.getString(SHAPE_PREFS_KEY, null)
        if (customShapesJson != null) {
            val type = object : TypeToken<List<CustomShape>>() {}.type
            val customShapes: List<CustomShape> = gson.fromJson(customShapesJson, type)
            val customIndex = position - defaultIds.size
            if (customIndex < customShapes.size) {
                return customShapes[customIndex].id
            }
        }
        
        return "circle"
    }
    
    private fun showAddShapeDialog(onShapeAdded: (MutableList<String>) -> Unit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_shape, null)
        
        val nameEditText = dialogView.findViewById<EditText>(R.id.shapeNameEditText)
        val svgCodeEditText = dialogView.findViewById<EditText>(R.id.svgCodeEditText)
        val generatedIdTextView = dialogView.findViewById<TextView>(R.id.generatedIdTextView)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)
        
        // Generate auto ID
        val autoGeneratedId = generateAutoShapeId()
        generatedIdTextView.text = autoGeneratedId
        
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()
        
        cancelButton.setOnClickListener {
            dialog.dismiss()
        }
        
        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val svgCode = svgCodeEditText.text.toString().trim()
            val shapeId = autoGeneratedId
            
            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_shape_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (svgCode.isEmpty()) {
                Toast.makeText(this, getString(R.string.please_enter_xml_code), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validate basic XML structure
            if (!svgCode.contains("<vector") || !svgCode.contains("</vector>")) {
                Toast.makeText(this, getString(R.string.invalid_xml_format), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Load existing custom shapes
            val customShapesJson = sharedPreferences.getString(SHAPE_PREFS_KEY, null)
            val customShapes = mutableListOf<CustomShape>()
            
            if (customShapesJson != null) {
                val type = object : TypeToken<List<CustomShape>>() {}.type
                customShapes.addAll(gson.fromJson<List<CustomShape>>(customShapesJson, type))
            }
            
            // Add new shape
            customShapes.add(CustomShape(name, shapeId, svgCode))
            
            // Save to SharedPreferences
            val updatedJson = gson.toJson(customShapes)
            sharedPreferences.edit().putString(SHAPE_PREFS_KEY, updatedJson).apply()
            
            Toast.makeText(this, getString(R.string.shape_added, name, shapeId), Toast.LENGTH_SHORT).show()
            
            // Update adapter
            val updatedOptions = getShapeOptions()
            onShapeAdded(updatedOptions)
            
            dialog.dismiss()
        }
        
        dialog.show()
    }
    
    private fun generateAutoShapeId(): String {
        // Load existing custom shapes to count
        val customShapesJson = sharedPreferences.getString(SHAPE_PREFS_KEY, null)
        var count = 1
        
        if (customShapesJson != null) {
            val type = object : TypeToken<List<CustomShape>>() {}.type
            val customShapes: List<CustomShape> = gson.fromJson(customShapesJson, type)
            count = customShapes.size + 1
        }
        
        return "custom_$count"
    }
    
    private fun findShapePosition(shapeId: String): Int {
        val defaultIds = listOf("circle", "square", "triangle", "star", "heart", 
            "pentagon", "hexagon", "octagon", "oval", "rectangle", "diamond")
        
        // Check default shapes
        val defaultIndex = defaultIds.indexOf(shapeId)
        if (defaultIndex >= 0) {
            return defaultIndex
        }
        
        // Check custom shapes
        val customShapesJson = sharedPreferences.getString(SHAPE_PREFS_KEY, null)
        if (customShapesJson != null) {
            val type = object : TypeToken<List<CustomShape>>() {}.type
            val customShapes: List<CustomShape> = gson.fromJson(customShapesJson, type)
            val customIndex = customShapes.indexOfFirst { it.id.lowercase() == shapeId }
            if (customIndex >= 0) {
                return defaultIds.size + customIndex
            }
        }
        
        return 0 // Default to first shape if not found
    }
}


