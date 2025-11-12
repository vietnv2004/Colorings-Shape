package com.uef.coloring_app.ui.simple

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import java.io.File

class SimpleHistoryAdapter(private val historyItems: List<SimpleHistoryItem>) : 
    RecyclerView.Adapter<SimpleHistoryAdapter.HistoryViewHolder>() {
    
    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val shapeImageView: ImageView = itemView.findViewById(R.id.shapeImageView)
        val taskNameTextView: TextView = itemView.findViewById(R.id.taskNameTextView)
        val scoreTextView: TextView = itemView.findViewById(R.id.scoreTextView)
        val timeTextView: TextView = itemView.findViewById(R.id.timeTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val rankTextView: TextView? = itemView.findViewById(R.id.rankTextView)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_simple_history, parent, false)
        return HistoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = historyItems[position]
        
        // Set task name
        holder.taskNameTextView.text = item.taskName
        
        // Set image - ưu tiên hình ảnh thực, nếu không có thì dùng shape icon
        if (item.imagePath != null && item.imagePath.isNotEmpty()) {
            val imageFile = File(item.imagePath)
            if (imageFile.exists()) {
                try {
                    val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                    bitmap?.let {
                        // Set scale type to fit center for better display
                        holder.shapeImageView.scaleType = android.widget.ImageView.ScaleType.FIT_CENTER
                        holder.shapeImageView.setImageBitmap(it)
                    } ?: run {
                        setShapeIcon(holder, item.shapeType)
                    }
                } catch (e: Exception) {
                    setShapeIcon(holder, item.shapeType)
                }
            } else {
                setShapeIcon(holder, item.shapeType)
            }
        } else {
            setShapeIcon(holder, item.shapeType)
        }
        
        // Set score with color based on performance
        holder.scoreTextView.text = "Score: ${item.score}"
        val scoreColor = when {
            item.score >= 500 -> ContextCompat.getColor(holder.itemView.context, R.color.gold_color)
            item.score >= 300 -> ContextCompat.getColor(holder.itemView.context, R.color.success_color_on_white)
            item.score >= 200 -> ContextCompat.getColor(holder.itemView.context, R.color.primary_color)
            else -> ContextCompat.getColor(holder.itemView.context, R.color.text_secondary_on_light)
        }
        holder.scoreTextView.setTextColor(scoreColor)
        
        // Set time
        holder.timeTextView.text = "Time: ${item.timeSpent}"
        
        // Set date
        holder.dateTextView.text = item.date
        
        // Set rank if available
        holder.rankTextView?.text = if (item.rank > 0) "#${item.rank}" else ""
    }
    
    override fun getItemCount(): Int = historyItems.size
    
    /**
     * Set shape icon for fallback
     */
    private fun setShapeIcon(holder: HistoryViewHolder, shapeType: String) {
        val shapeIcon = when(shapeType.lowercase()) {
            "circle" -> R.drawable.shape_circle_outline
            "square" -> R.drawable.shape_square_outline
            "triangle" -> R.drawable.shape_triangle_outline
            "star" -> R.drawable.shape_star_outline
            "heart" -> R.drawable.shape_heart_outline
            "pentagon" -> R.drawable.shape_pentagon_outline
            "hexagon" -> R.drawable.shape_hexagon_outline
            "rectangle" -> R.drawable.shape_rectangle_outline
            "oval" -> R.drawable.shape_oval_outline
            "diamond" -> R.drawable.shape_diamond_outline
            else -> R.drawable.shape_circle_outline
        }
        holder.shapeImageView.setImageResource(shapeIcon)
    }
}
