package com.uef.coloring_app.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.entity.TaskEntity

class TaskAdapter(
    private var tasks: List<TaskEntity>,
    private val onEditClick: (TaskEntity) -> Unit,
    private val onDeleteClick: (TaskEntity) -> Unit,
    private val onToggleActive: (TaskEntity, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shapeImageView: ImageView = itemView.findViewById(R.id.shapeImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val difficultyTextView: TextView = itemView.findViewById(R.id.difficultyTextView)
        val pointsTextView: TextView = itemView.findViewById(R.id.pointsTextView)
        val durationTextView: TextView = itemView.findViewById(R.id.durationTextView)
        val statusSwitch: Switch = itemView.findViewById(R.id.statusSwitch)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        holder.nameTextView.text = task.name
        holder.descriptionTextView.text = task.description
        holder.pointsTextView.text = "${task.points} điểm"
        holder.durationTextView.text = "${task.maxDuration / 60000} phút"
        
        // Set difficulty
        val difficultyText = when(task.difficulty) {
            "easy" -> "Dễ"
            "medium" -> "Trung bình"
            "hard" -> "Khó"
            else -> task.difficulty
        }
        holder.difficultyTextView.text = difficultyText
        
        val difficultyBg = when(task.difficulty) {
            "easy" -> R.drawable.bg_difficulty_easy
            "medium" -> R.drawable.role_badge_background
            "hard" -> R.drawable.role_badge_background_admin
            else -> R.drawable.bg_difficulty_easy
        }
        holder.difficultyTextView.setBackgroundResource(difficultyBg)
        
        // Set shape icon
        val shapeIcon = when(task.shapeId) {
            "circle" -> R.drawable.shape_circle_outline
            "square" -> R.drawable.shape_square_outline
            "triangle" -> R.drawable.shape_triangle_outline
            else -> R.drawable.ic_shape_placeholder
        }
        holder.shapeImageView.setImageResource(shapeIcon)
        
        // Set status
        holder.statusSwitch.isChecked = task.isActive
        holder.statusSwitch.setOnCheckedChangeListener { _, isChecked ->
            onToggleActive(task, isChecked)
        }
        
        // Set click listeners
        holder.editButton.setOnClickListener { onEditClick(task) }
        holder.deleteButton.setOnClickListener { onDeleteClick(task) }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<TaskEntity>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}

