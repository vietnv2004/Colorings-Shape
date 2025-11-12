package com.uef.coloring_app.ui.themes

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.google.android.material.slider.Slider
import com.google.android.material.button.MaterialButton
import android.widget.TextView
import android.widget.Toast
import com.uef.coloring_app.ui.common.BaseActivity

class ColorPickerActivity : BaseActivity() {
    
    private lateinit var primaryColorRecyclerView: RecyclerView
    private lateinit var secondaryColorRecyclerView: RecyclerView
    private lateinit var intensitySlider: Slider
    private lateinit var intensityLabel: TextView
    private lateinit var previewButton: MaterialButton
    private lateinit var resetButton: MaterialButton
    private lateinit var applyButton: MaterialButton
    
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedPrimaryColor: ColorItem? = null
    private var selectedSecondaryColor: ColorItem? = null
    private var currentIntensity = 500
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_picker)
        
        sharedPreferences = getSharedPreferences("color_preferences", Context.MODE_PRIVATE)
        
        initViews()
        setupRecyclerViews()
        setupSlider()
        setupButtons()
        loadSavedColors()
    }
    
    private fun initViews() {
        primaryColorRecyclerView = findViewById(R.id.primaryColorRecyclerView)
        secondaryColorRecyclerView = findViewById(R.id.secondaryColorRecyclerView)
        intensitySlider = findViewById(R.id.intensitySlider)
        intensityLabel = findViewById(R.id.intensityLabel)
        previewButton = findViewById(R.id.previewButton)
        resetButton = findViewById(R.id.resetButton)
        applyButton = findViewById(R.id.applyButton)
    }
    
    private fun setupRecyclerViews() {
        // Primary colors
        val primaryColors = listOf(
            ColorItem("Coral", "#FF6B47", R.color.coral_500),
            ColorItem("Blue", "#2196F3", R.color.blue_500),
            ColorItem("Green", "#4CAF50", R.color.green_500),
            ColorItem("Purple", "#9C27B0", R.color.purple_500),
            ColorItem("Pink", "#E91E63", R.color.pink_500),
            ColorItem("Orange", "#FF9800", R.color.orange_500),
            ColorItem("Red", "#F44336", R.color.red_500),
            ColorItem("Teal", "#009688", R.color.teal_500),
            ColorItem("Indigo", "#3F51B5", R.color.indigo_500),
            ColorItem("Cyan", "#00BCD4", R.color.cyan_500),
            ColorItem("Lime", "#CDDC39", R.color.lime_500),
            ColorItem("Amber", "#FFC107", R.color.amber_500)
        )
        
        val primaryAdapter = ColorAdapter(primaryColors) { colorItem ->
            selectedPrimaryColor = colorItem
            updatePreview()
        }
        
        primaryColorRecyclerView.layoutManager = GridLayoutManager(this, 6)
        primaryColorRecyclerView.adapter = primaryAdapter
        
        // Secondary colors
        val secondaryColors = listOf(
            ColorItem("Green", "#4CAF50", R.color.green_500),
            ColorItem("Blue", "#2196F3", R.color.blue_500),
            ColorItem("Purple", "#9C27B0", R.color.purple_500),
            ColorItem("Pink", "#E91E63", R.color.pink_500),
            ColorItem("Orange", "#FF9800", R.color.orange_500),
            ColorItem("Teal", "#009688", R.color.teal_500),
            ColorItem("Indigo", "#3F51B5", R.color.indigo_500),
            ColorItem("Cyan", "#00BCD4", R.color.cyan_500),
            ColorItem("Lime", "#CDDC39", R.color.lime_500),
            ColorItem("Amber", "#FFC107", R.color.amber_500),
            ColorItem("Red", "#F44336", R.color.red_500),
            ColorItem("Coral", "#FF6B47", R.color.coral_500)
        )
        
        val secondaryAdapter = ColorAdapter(secondaryColors) { colorItem ->
            selectedSecondaryColor = colorItem
            updatePreview()
        }
        
        secondaryColorRecyclerView.layoutManager = GridLayoutManager(this, 6)
        secondaryColorRecyclerView.adapter = secondaryAdapter
    }
    
    private fun setupSlider() {
        intensitySlider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                currentIntensity = value.toInt()
                intensityLabel.text = "Mức độ: $currentIntensity"
                updatePreview()
            }
        }
    }
    
    private fun setupButtons() {
        previewButton.setOnClickListener {
            updatePreview()
            Toast.makeText(this, "Đã cập nhật màu sắc!", Toast.LENGTH_SHORT).show()
        }
        
        resetButton.setOnClickListener {
            resetToDefault()
        }
        
        applyButton.setOnClickListener {
            saveColors()
            Toast.makeText(this, "Đã áp dụng màu sắc mới!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun updatePreview() {
        // Update preview button color
        selectedPrimaryColor?.let { color ->
            val colorHex = getColorWithIntensity(color.colorHex, currentIntensity)
            previewButton.setBackgroundColor(android.graphics.Color.parseColor(colorHex))
        }
    }
    
    private fun getColorWithIntensity(baseColor: String, intensity: Int): String {
        // This is a simplified version - in a real app you'd have proper color intensity calculation
        return when (intensity) {
            50 -> baseColor.replace("#", "#0D")
            100 -> baseColor.replace("#", "#1A")
            200 -> baseColor.replace("#", "#33")
            300 -> baseColor.replace("#", "#4D")
            400 -> baseColor.replace("#", "#66")
            500 -> baseColor
            600 -> baseColor.replace("#", "#99")
            700 -> baseColor.replace("#", "#B3")
            800 -> baseColor.replace("#", "#CC")
            900 -> baseColor.replace("#", "#E6")
            else -> baseColor
        }
    }
    
    private fun resetToDefault() {
        selectedPrimaryColor = ColorItem("Coral", "#FF6B47", R.color.coral_500)
        selectedSecondaryColor = ColorItem("Green", "#4CAF50", R.color.green_500)
        currentIntensity = 500
        intensitySlider.value = 500f
        intensityLabel.text = "Mức độ: 500"
        updatePreview()
    }
    
    private fun saveColors() {
        selectedPrimaryColor?.let { color ->
            sharedPreferences.edit()
                .putString("primary_color", color.colorHex)
                .putString("primary_color_name", color.name)
                .putInt("primary_color_res", color.colorRes)
                .apply()
        }
        
        selectedSecondaryColor?.let { color ->
            sharedPreferences.edit()
                .putString("secondary_color", color.colorHex)
                .putString("secondary_color_name", color.name)
                .putInt("secondary_color_res", color.colorRes)
                .apply()
        }
        
        sharedPreferences.edit()
            .putInt("color_intensity", currentIntensity)
            .apply()
    }
    
    private fun loadSavedColors() {
        val intensity = sharedPreferences.getInt("color_intensity", 500)
        
        // Set slider value
        intensitySlider.value = intensity.toFloat()
        intensityLabel.text = "Mức độ: $intensity"
        currentIntensity = intensity
        
        // You would need to find and select the corresponding colors in the adapters
        // This is simplified for now - in a real implementation, you would:
        // 1. Find the color items that match primaryColorHex and secondaryColorHex
        // 2. Set them as selected in the adapters
        // 3. Update the UI accordingly
        
        updatePreview()
    }
}
