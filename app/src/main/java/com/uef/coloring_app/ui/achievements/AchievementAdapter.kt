package com.uef.coloring_app.ui.achievements

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.model.Achievement

class AchievementAdapter(private val achievements: List<Achievement>) : 
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {
    
    class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconTextView: TextView = itemView.findViewById(R.id.iconTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        val progressTextView: TextView = itemView.findViewById(R.id.progressTextView)
        val pointsTextView: TextView = itemView.findViewById(R.id.pointsTextView)
        val statusImageView: ImageView = itemView.findViewById(R.id.statusImageView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_achievement, parent, false)
        return AchievementViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        val context = holder.itemView.context
        
        holder.iconTextView.text = achievement.icon
        // Đồng bộ: tất cả achievement hiển thị dạng "Điểm cao" theo yêu cầu
        holder.nameTextView.text = "Điểm cao"
        holder.descriptionTextView.text = "Đạt ${achievement.requirement} điểm"
        
        // Hiển thị nhãn yêu cầu và số điểm ngưỡng mở khóa
        holder.pointsTextView.text = "Yêu cầu: ${achievement.requirement} điểm"
        
        if (achievement.isUnlocked) {
            holder.statusImageView.setImageResource(R.drawable.ic_check_circle)
            holder.progressBar.progress = 100
            holder.progressTextView.text = context.getString(R.string.status_unlocked)
        } else {
            holder.statusImageView.setImageResource(R.drawable.ic_lock)
            holder.progressBar.progress = 0
            holder.progressTextView.text = context.getString(R.string.status_locked)
        }
    }
    
    override fun getItemCount(): Int = achievements.size
}
