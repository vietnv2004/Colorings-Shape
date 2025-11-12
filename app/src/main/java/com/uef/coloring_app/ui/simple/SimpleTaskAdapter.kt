package com.uef.coloring_app.ui.simple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.core.haptic.HapticManager

class SimpleTaskAdapter(private val tasks: List<SimpleTask>) : RecyclerView.Adapter<SimpleTaskAdapter.TaskViewHolder>() {
    
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shapeImageView: ImageView = itemView.findViewById(R.id.shapeImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val difficultyTextView: TextView = itemView.findViewById(R.id.difficultyTextView)
        val pointsTextView: TextView = itemView.findViewById(R.id.pointsTextView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task_grid, parent, false)
        return TaskViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val context = holder.itemView.context
        
        // Set task image based on shape
        val shapeDrawable = when (task.shapeId.lowercase()) {
            "circle" -> R.drawable.shape_circle_outline
            "square" -> R.drawable.shape_square_outline
            "triangle" -> R.drawable.shape_triangle_outline
            "star" -> R.drawable.shape_star_outline
            "heart" -> R.drawable.shape_heart_outline
            "pentagon" -> R.drawable.shape_pentagon_outline
            "hexagon" -> R.drawable.shape_hexagon_outline
            "diamond" -> R.drawable.shape_diamond_outline
            "octagon" -> R.drawable.shape_octagon_outline
            "oval" -> R.drawable.shape_oval_outline
            else -> R.drawable.shape_circle_outline // Default
        }
        
        holder.shapeImageView.setImageResource(shapeDrawable)
        holder.nameTextView.text = task.name
        holder.difficultyTextView.text = task.difficulty
        holder.pointsTextView.text = context.getString(R.string.points_with_unit, task.points)
        
        // Set difficulty background color
        val difficultyBackground = when (task.difficulty.lowercase()) {
            "dễ" -> R.drawable.bg_difficulty_easy
            "trung bình" -> R.drawable.bg_difficulty_medium
            "khó" -> R.drawable.bg_difficulty_hard
            else -> R.drawable.bg_difficulty_easy
        }
        holder.difficultyTextView.setBackgroundResource(difficultyBackground)
        
        // Set click listener on entire item
        holder.itemView.setOnClickListener {
            HapticManager.buttonClick(context)
            val intent = android.content.Intent(context, com.uef.coloring_app.ui.drawing.DrawingActivity::class.java)
            intent.putExtra("task_id", task.id)
            intent.putExtra("task_name", task.name)
            intent.putExtra("shape_id", task.shapeId)
            context.startActivity(intent)
        }
    }
    
    override fun getItemCount(): Int = tasks.size
}