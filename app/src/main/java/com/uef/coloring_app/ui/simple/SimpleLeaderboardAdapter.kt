package com.uef.coloring_app.ui.simple

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class SimpleLeaderboardAdapter(private val leaderboardItems: List<SimpleLeaderboardItem>) : 
    RecyclerView.Adapter<SimpleLeaderboardAdapter.LeaderboardViewHolder>() {
    
    class LeaderboardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rankTextView: TextView = itemView.findViewById(R.id.rankTextView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LeaderboardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_leaderboard, parent, false)
        return LeaderboardViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: LeaderboardViewHolder, position: Int) {
        val item = leaderboardItems[position]
        
        holder.rankTextView.text = item.rank
        holder.nameTextView.text = item.name
        holder.scoreTextView.text = item.score.toString()
    }
    
    override fun getItemCount(): Int = leaderboardItems.size
}
