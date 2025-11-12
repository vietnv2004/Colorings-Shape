package com.uef.coloring_app.ui.simple

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.DatabaseInitializer
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.repository.TaskRepository
import kotlinx.coroutines.launch

class SimpleTaskFragment : Fragment() {
    
    private lateinit var taskRepository: TaskRepository
    private lateinit var recyclerView: RecyclerView
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_simple_task, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        
        // Initialize database and repository
        val taskDao = ColoringDatabase.getDatabase(requireContext()).taskDao()
        taskRepository = TaskRepository(taskDao)
        
        // Initialize sample data if needed
        DatabaseInitializer.initializeSampleData(requireContext())
        
        // Load tasks from database with a small delay to ensure initialization completes
        loadTasksWithDelay()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload tasks when fragment becomes visible again
        loadTasks()
    }
    
    private fun loadTasksWithDelay() {
        lifecycleScope.launch {
            // Wait for database initialization
            kotlinx.coroutines.delay(500)
            loadTasks()
        }
    }
    
    private fun loadTasks() {
        lifecycleScope.launch {
            try {
                // Get active tasks from database
                val taskEntities = taskRepository.getActiveTasks()
                
                // Convert to SimpleTask
                val tasks = taskEntities.map { entity ->
                    SimpleTask(
                        id = entity.id,
                        name = entity.name,
                        description = entity.description,
                        difficulty = when(entity.difficulty) {
                            "easy" -> getString(R.string.easy)
                            "medium" -> getString(R.string.medium)
                            "hard" -> getString(R.string.hard)
                            else -> entity.difficulty
                        },
                        points = entity.points,
                        shapeId = entity.shapeId
                    )
                }
                
                // Update adapter
                recyclerView.adapter = SimpleTaskAdapter(tasks)
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback to hardcoded tasks if database fails
                loadFallbackTasks()
            }
        }
    }
    
    private fun loadFallbackTasks() {
        val tasks = listOf(
            SimpleTask(
                id = "",
                name = getString(R.string.task_color_circle), 
                description = getString(R.string.task_desc_circle), 
                difficulty = getString(R.string.easy), 
                points = 100,
                shapeId = "circle"
            ),
            SimpleTask(
                id = "",
                name = getString(R.string.task_color_square), 
                description = getString(R.string.task_desc_square), 
                difficulty = getString(R.string.medium), 
                points = 150,
                shapeId = "square"
            ),
            SimpleTask(
                id = "",
                name = getString(R.string.task_color_triangle), 
                description = getString(R.string.task_desc_triangle), 
                difficulty = getString(R.string.hard), 
                points = 200,
                shapeId = "triangle"
            )
        )
        
        recyclerView.adapter = SimpleTaskAdapter(tasks)
    }
}

data class SimpleTask(
    val id: String,
    val name: String,
    val description: String,
    val difficulty: String,
    val points: Int,
    val shapeId: String
)