package com.uef.coloring_app.ui.ai

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class AIFeatureAdapter(private val features: List<AIFeature>) : 
    RecyclerView.Adapter<AIFeatureAdapter.FeatureViewHolder>() {
    
    class FeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconTextView: TextView = itemView.findViewById(R.id.iconTextView)
        val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val actionButton: Button = itemView.findViewById(R.id.actionButton)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_feature, parent, false)
        return FeatureViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        val feature = features[position]
        
        holder.iconTextView.text = feature.icon
        holder.titleTextView.text = feature.title
        holder.descriptionTextView.text = feature.description
        holder.statusTextView.text = feature.status
        
        when (feature.status) {
            "Active" -> {
                holder.statusTextView.setTextColor(holder.itemView.context.getColor(R.color.success_color))
                holder.actionButton.text = "Use"
                holder.actionButton.isEnabled = true
            }
            "Available" -> {
                holder.statusTextView.setTextColor(holder.itemView.context.getColor(R.color.primary_color))
                holder.actionButton.text = "Enable"
                holder.actionButton.isEnabled = true
            }
            else -> {
                holder.statusTextView.setTextColor(holder.itemView.context.getColor(R.color.text_secondary))
                holder.actionButton.text = "Coming Soon"
                holder.actionButton.isEnabled = false
            }
        }
        
        holder.actionButton.setOnClickListener {
            // TODO: Handle feature action
        }
    }
    
    override fun getItemCount(): Int = features.size
}
