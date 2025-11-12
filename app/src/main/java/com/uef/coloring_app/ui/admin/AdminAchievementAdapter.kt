package com.uef.coloring_app.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.model.Achievement

class AdminAchievementAdapter(
    private var achievements: List<Achievement>,
    private val onEditClick: (Achievement) -> Unit,
    private val onDeleteClick: (Achievement) -> Unit
) : RecyclerView.Adapter<AdminAchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconTextView: TextView = itemView.findViewById(R.id.iconTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val typeTextView: TextView = itemView.findViewById(R.id.typeTextView)
        val requirementTextView: TextView = itemView.findViewById(R.id.requirementTextView)
        val pointsTextView: TextView = itemView.findViewById(R.id.pointsTextView)
        val editButton: Button = itemView.findViewById(R.id.editButton)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_achievement, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        
        holder.iconTextView.text = achievement.icon
        // Hiển thị tất cả ở dạng "Đạt điểm cao"
        holder.nameTextView.text = "Điểm cao"
        holder.descriptionTextView.text = "Đạt ${achievement.requirement} điểm"
        // Ẩn nhãn loại; hiển thị yêu cầu điểm
        holder.typeTextView.visibility = View.GONE
        holder.requirementTextView.visibility = View.VISIBLE
        holder.requirementTextView.text = "Yêu cầu: ${achievement.requirement} điểm"
        // Ẩn điểm thưởng khỏi UI quản trị
        holder.pointsTextView.visibility = View.GONE
        holder.pointsTextView.text = "${achievement.points} điểm"
        
        holder.editButton.setOnClickListener { onEditClick(achievement) }
        holder.deleteButton.setOnClickListener { onDeleteClick(achievement) }
    }

    override fun getItemCount(): Int = achievements.size

    fun updateAchievements(newAchievements: List<Achievement>) {
        achievements = newAchievements
        notifyDataSetChanged()
    }
}


