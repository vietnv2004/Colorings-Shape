package com.uef.coloring_app.ui.sounds

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class SoundFeatureAdapter(
    private val features: List<SoundFeature>,
    private val onFeatureClick: (SoundFeature) -> Unit
) : RecyclerView.Adapter<SoundFeatureAdapter.SoundFeatureViewHolder>() {

    class SoundFeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iconText: TextView = itemView.findViewById(R.id.iconText)
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val descriptionText: TextView = itemView.findViewById(R.id.descriptionText)
        val statusText: TextView = itemView.findViewById(R.id.statusText)
        val useButton: Button = itemView.findViewById(R.id.useButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundFeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sound_feature, parent, false)
        return SoundFeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoundFeatureViewHolder, position: Int) {
        val feature = features[position]
        
        holder.iconText.text = feature.icon
        holder.titleText.text = feature.title
        holder.descriptionText.text = feature.description
        holder.statusText.text = feature.status
        
        // Set status color
        val isActive = feature.status == holder.itemView.context.getString(R.string.active)
        val statusColor = if (isActive) {
            holder.itemView.context.getColor(R.color.success_color)
        } else {
            holder.itemView.context.getColor(R.color.text_secondary)
        }
        holder.statusText.setTextColor(statusColor)
        
        // Set button text based on feature type and status
        holder.useButton.text = when {
            feature.title == holder.itemView.context.getString(R.string.background_music) -> holder.itemView.context.getString(R.string.select_music)
            feature.title == holder.itemView.context.getString(R.string.sound_effects) -> holder.itemView.context.getString(R.string.select)
            feature.title == holder.itemView.context.getString(R.string.notification_sound) -> holder.itemView.context.getString(R.string.select)
            isActive -> holder.itemView.context.getString(R.string.turn_off)
            else -> holder.itemView.context.getString(R.string.turn_on)
        }
        
        holder.useButton.setOnClickListener {
            onFeatureClick(feature)
        }
    }

    override fun getItemCount(): Int = features.size
}