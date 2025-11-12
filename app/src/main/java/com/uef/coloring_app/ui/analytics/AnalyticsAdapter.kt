package com.uef.coloring_app.ui.analytics

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class AnalyticsAdapter(private val analyticsItems: List<AnalyticsItem>) : 
    RecyclerView.Adapter<AnalyticsAdapter.AnalyticsViewHolder>() {
    
    class AnalyticsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val valueTextView: TextView = itemView.findViewById(R.id.valueTextView)
        val unitTextView: TextView = itemView.findViewById(R.id.unitTextView)
        val trendTextView: TextView = itemView.findViewById(R.id.trendTextView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnalyticsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_analytics, parent, false)
        return AnalyticsViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AnalyticsViewHolder, position: Int) {
        val item = analyticsItems[position]
        
        holder.titleTextView.text = item.title
        holder.descriptionTextView.text = item.description
        holder.valueTextView.text = item.value
        holder.unitTextView.text = item.unit
        holder.trendTextView.text = item.trend
    }
    
    override fun getItemCount(): Int = analyticsItems.size
}
