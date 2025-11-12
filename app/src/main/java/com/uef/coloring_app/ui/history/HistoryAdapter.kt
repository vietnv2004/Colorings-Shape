package com.uef.coloring_app.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class HistoryAdapter(private val historyItems: List<HistoryItem>) : 
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskNameTextView: TextView = itemView.findViewById(R.id.taskNameTextView)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyItems[position]
        
        holder.taskNameTextView.text = item.taskName
        holder.scoreTextView.text = "Score: ${item.score}"
        holder.timeTextView.text = "Time: ${item.timeSpent}"
        holder.dateTextView.text = item.date
    }
    
    override fun getItemCount(): Int = historyItems.size
}
