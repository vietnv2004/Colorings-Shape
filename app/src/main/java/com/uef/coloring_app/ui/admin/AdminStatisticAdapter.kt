package com.uef.coloring_app.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class AdminStatisticAdapter(
    private val statistics: List<AdminStatistic>
) : RecyclerView.Adapter<AdminStatisticAdapter.StatisticViewHolder>() {

    inner class StatisticViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.statisticTitleTextView)
        val valueTextView: TextView = itemView.findViewById(R.id.statisticValueTextView)
        val iconTextView: TextView = itemView.findViewById(R.id.statisticIconTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatisticViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_admin_statistic, parent, false)
        return StatisticViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatisticViewHolder, position: Int) {
        val statistic = statistics[position]
        
        // Display title and value separately
        holder.titleTextView.text = statistic.title
        holder.valueTextView.text = statistic.value
        holder.iconTextView.text = statistic.icon
    }

    override fun getItemCount() = statistics.size
}
