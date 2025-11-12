package com.uef.coloring_app.ui.leaderboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class LeaderboardAdapter(private val leaderboardItems: List<LeaderboardItem>) : 
    RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder>() {
    
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
        val item = leaderboardItems[position]
        
        holder.rankTextView.text = "#${item.rank}"
        holder.userIdTextView.text = item.userName
        holder.taskIdTextView.text = item.taskName
        holder.scoreTextView.text = holder.itemView.context.getString(R.string.score_points, item.score)
        
        // Format time spent (milliseconds to MM:SS)
        val minutes = (item.timeSpent / 60000).toInt()
        val seconds = ((item.timeSpent % 60000) / 1000).toInt()
        holder.timeSpentTextView.text = String.format("%02d:%02d", minutes, seconds)
        
        // Format date
        val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        holder.dateTextView.text = dateFormat.format(item.completedAt)
    }
    
    override fun getItemCount(): Int = leaderboardItems.size
}
