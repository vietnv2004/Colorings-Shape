package com.uef.coloring_app.ui.drawing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class ColorPaletteAdapter(private val colors: List<ColorItem>) : 
    RecyclerView.Adapter<ColorPaletteAdapter.ColorViewHolder>() {
    
    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorView: View = itemView.findViewById(R.id.colorView)
        val colorNameTextView: TextView = itemView.findViewById(R.id.colorNameTextView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_palette, parent, false)
        return ColorViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val color = colors[position]
        
        holder.colorNameTextView.text = color.colorName
        holder.colorView.setBackgroundColor(android.graphics.Color.parseColor(color.colorCode))
        
        holder.itemView.setOnClickListener {
            // TODO: Handle color selection
        }
    }
    
    override fun getItemCount(): Int = colors.size
}
