package com.uef.coloring_app.ui.drawing

import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.ui.common.BaseActivity

class AdvancedDrawingActivity : BaseActivity() {
    
    private lateinit var brushSizeSeekBar: SeekBar
    private lateinit var brushSizeTextView: TextView
    private lateinit var opacitySeekBar: SeekBar
    private lateinit var opacityTextView: TextView
    private lateinit var colorPaletteRecyclerView: RecyclerView
    private lateinit var undoButton: Button
    private lateinit var redoButton: Button
    private lateinit var clearButton: Button
    private lateinit var saveButton: Button
    private lateinit var shareButton: Button
    
    private var currentBrushSize = 10
    private var currentOpacity = 100
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_drawing)
        
        initViews()
        setupClickListeners()
        setupSeekBars()
        loadColorPalette()
    }
    
    private fun initViews() {
        brushSizeSeekBar = findViewById(R.id.brushSizeSeekBar)
        brushSizeTextView = findViewById(R.id.brushSizeTextView)
        opacitySeekBar = findViewById(R.id.opacitySeekBar)
        opacityTextView = findViewById(R.id.opacityTextView)
        colorPaletteRecyclerView = findViewById(R.id.colorPaletteRecyclerView)
        undoButton = findViewById(R.id.undoButton)
        redoButton = findViewById(R.id.redoButton)
        clearButton = findViewById(R.id.clearButton)
        saveButton = findViewById(R.id.saveButton)
        shareButton = findViewById(R.id.shareButton)
    }
    
    private fun setupClickListeners() {
        undoButton.setOnClickListener {
            // TODO: Implement undo functionality
        }
        
        redoButton.setOnClickListener {
            // TODO: Implement redo functionality
        }
        
        clearButton.setOnClickListener {
            // TODO: Implement clear canvas functionality
        }
        
        saveButton.setOnClickListener {
            // TODO: Implement save functionality
        }
        
        shareButton.setOnClickListener {
            // TODO: Implement share functionality
        }
    }
    
    private fun setupSeekBars() {
        brushSizeSeekBar.max = 50
        brushSizeSeekBar.progress = currentBrushSize
        brushSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentBrushSize = progress
                brushSizeTextView.text = "Brush Size: $currentBrushSize"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        opacitySeekBar.max = 100
        opacitySeekBar.progress = currentOpacity
        opacitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentOpacity = progress
                opacityTextView.text = "Opacity: $currentOpacity%"
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun loadColorPalette() {
        val colors = getColorPalette()
        
        colorPaletteRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        colorPaletteRecyclerView.adapter = ColorPaletteAdapter(colors)
    }
    
    private fun getColorPalette(): List<ColorItem> {
        return listOf(
            ColorItem("#FF0000", "Red"),
            ColorItem("#00FF00", "Green"),
            ColorItem("#0000FF", "Blue"),
            ColorItem("#FFFF00", "Yellow"),
            ColorItem("#FF00FF", "Magenta"),
            ColorItem("#00FFFF", "Cyan"),
            ColorItem("#FFA500", "Orange"),
            ColorItem("#800080", "Purple"),
            ColorItem("#000000", "Black"),
            ColorItem("#FFFFFF", "White"),
            ColorItem("#808080", "Gray"),
            ColorItem("#8B4513", "Brown")
        )
    }
}

data class ColorItem(
    val colorCode: String,
    val colorName: String
)
