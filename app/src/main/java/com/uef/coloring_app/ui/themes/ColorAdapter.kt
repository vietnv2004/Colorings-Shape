package com.uef.coloring_app.ui.themes

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class ColorAdapter(
    private val colors: List<ColorItem>,
    private val onColorSelected: (ColorItem) -> Unit
) : RecyclerView.Adapter<ColorAdapter.ColorViewHolder>() {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_color_picker, parent, false)
        return ColorViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val colorItem = colors[position]
        holder.bind(colorItem, position == selectedPosition)
        
        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val previousPosition = selectedPosition
                selectedPosition = currentPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onColorSelected(colorItem)
            }
        }
    }

    override fun getItemCount(): Int = colors.size

    class ColorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val colorView: ImageView = itemView.findViewById(R.id.colorView)
        private val checkIcon: ImageView = itemView.findViewById(R.id.checkIcon)

        fun bind(colorItem: ColorItem, isSelected: Boolean) {
            colorView.setColorFilter(Color.parseColor(colorItem.colorHex))
            checkIcon.visibility = if (isSelected) View.VISIBLE else View.GONE
            
            // Add border for selected item
            if (isSelected) {
                colorView.setBackgroundResource(R.drawable.color_selected_background)
            } else {
                colorView.setBackgroundResource(R.drawable.color_background)
            }
        }
    }
}

data class ColorItem(
    val name: String,
    val colorHex: String,
    val colorRes: Int
)
