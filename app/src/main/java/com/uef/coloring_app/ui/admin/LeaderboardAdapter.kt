package com.uef.coloring_app.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.entity.TaskAttemptEntity
import com.uef.coloring_app.data.local.entity.UserEntity
import com.uef.coloring_app.data.local.entity.TaskEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LeaderboardAdapter(
    private var attempts: List<TaskAttemptEntity>,
    private val users: Map<String, UserEntity>,
    private val tasks: Map<String, TaskEntity>
) : RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {

    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankTextView: TextView = itemView.findViewById(R.id.rankTextView)
        val userIdTextView: TextView = itemView.findViewById(R.id.userIdTextView)
        val taskIdTextView: TextView = itemView.findViewById(R.id.taskIdTextView)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
        val timeSpentTextView: TextView = itemView.findViewById(R.id.timeSpentTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }

    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val attempt = attempts[position]
        
        holder.rankTextView.text = "#${position + 1}"
        
        // Get user name from users map
        val user = users[attempt.userId]
        val userName = user?.name ?: holder.itemView.context.getString(R.string.user_prefix, attempt.userId.take(8))
        holder.userIdTextView.text = userName
        
        // Get task name from tasks map (or hiển thị Tổng điểm nếu là tổng hợp)
        val taskName = if (attempt.taskId == "__total__") {
            holder.itemView.context.getString(R.string.total_points_label)
        } else {
            val task = tasks[attempt.taskId]
            task?.name ?: holder.itemView.context.getString(R.string.task_prefix, attempt.taskId.take(8))
        }
        holder.taskIdTextView.text = taskName
        
        holder.scoreTextView.text = holder.itemView.context.getString(R.string.score_points, attempt.score)
        
        val timeMinutes = attempt.timeSpent / 60000
        val timeSeconds = (attempt.timeSpent % 60000) / 1000
        holder.timeSpentTextView.text = if (attempt.taskId == "__total__") "--:--" else String.format("%02d:%02d", timeMinutes, timeSeconds)
        
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.dateTextView.text = dateFormat.format(Date(attempt.completedAt))
    }

    override fun getItemCount(): Int = attempts.size

    fun updateAttempts(newAttempts: List<TaskAttemptEntity>) {
        attempts = newAttempts
        notifyDataSetChanged()
    }
}


