package com.uef.coloring_app.ui.gestures

import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class AdvancedGesturesActivity : AppCompatActivity() {
    
    private lateinit var gestureStatusTextView: TextView
    private lateinit var gestureInfoTextView: TextView
    private lateinit var gestureFeaturesRecyclerView: RecyclerView
    private lateinit var testGestureButton: Button
    private lateinit var calibrateGestureButton: Button
    private lateinit var enableGesturesSwitch: Switch
    private lateinit var tapGestureSwitch: Switch
    private lateinit var swipeGestureSwitch: Switch
    private lateinit var pinchGestureSwitch: Switch
    private lateinit var longPressGestureSwitch: Switch
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_advanced_gestures)
            
            initViews()
            setupClickListeners()
            loadGestureFeatures()
        } catch (e: Exception) {
            e.printStackTrace()
            android.widget.Toast.makeText(this, "Error initializing Gestures Activity: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    private fun initViews() {
        gestureStatusTextView = findViewById(R.id.gestureStatusTextView)
        gestureInfoTextView = findViewById(R.id.gestureInfoTextView)
        gestureFeaturesRecyclerView = findViewById(R.id.gestureFeaturesRecyclerView)
        testGestureButton = findViewById(R.id.testGestureButton)
        calibrateGestureButton = findViewById(R.id.calibrateGestureButton)
        enableGesturesSwitch = findViewById(R.id.enableGesturesSwitch)
        tapGestureSwitch = findViewById(R.id.tapGestureSwitch)
        swipeGestureSwitch = findViewById(R.id.swipeGestureSwitch)
        pinchGestureSwitch = findViewById(R.id.pinchGestureSwitch)
        longPressGestureSwitch = findViewById(R.id.longPressGestureSwitch)
    }
    
    private fun setupClickListeners() {
        testGestureButton.setOnClickListener {
            testGesture()
        }
        
        calibrateGestureButton.setOnClickListener {
            calibrateGesture()
        }
        
        enableGesturesSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleGestures(isChecked)
        }
        
        tapGestureSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleTapGesture(isChecked)
        }
        
        swipeGestureSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleSwipeGesture(isChecked)
        }
        
        pinchGestureSwitch.setOnCheckedChangeListener { _, isChecked ->
            togglePinchGesture(isChecked)
        }
        
        longPressGestureSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleLongPressGesture(isChecked)
        }
    }
    
    private fun testGesture() {
        gestureStatusTextView.text = "Cử chỉ: Đang kiểm tra"
        gestureInfoTextView.text = "Kiểm tra nhận diện cử chỉ đã được khởi tạo!"
    }
    
    private fun calibrateGesture() {
        gestureStatusTextView.text = "Cử chỉ: Đang hiệu chuẩn"
        gestureInfoTextView.text = "Hiệu chuẩn cử chỉ đã hoàn thành thành công!"
    }
    
    private fun toggleGestures(isEnabled: Boolean) {
        if (isEnabled) {
            gestureStatusTextView.text = "Cử chỉ: Đã bật"
        } else {
            gestureStatusTextView.text = "Cử chỉ: Đã tắt"
        }
    }
    
    private fun toggleTapGesture(isEnabled: Boolean) {
        if (isEnabled) {
            gestureStatusTextView.text = "Cử chỉ chạm: Đã bật"
        } else {
            gestureStatusTextView.text = "Cử chỉ chạm: Đã tắt"
        }
    }
    
    private fun toggleSwipeGesture(isEnabled: Boolean) {
        if (isEnabled) {
            gestureStatusTextView.text = "Cử chỉ vuốt: Đã bật"
        } else {
            gestureStatusTextView.text = "Cử chỉ vuốt: Đã tắt"
        }
    }
    
    private fun togglePinchGesture(isEnabled: Boolean) {
        if (isEnabled) {
            gestureStatusTextView.text = "Cử chỉ chụm: Đã bật"
        } else {
            gestureStatusTextView.text = "Cử chỉ chụm: Đã tắt"
        }
    }
    
    private fun toggleLongPressGesture(isEnabled: Boolean) {
        if (isEnabled) {
            gestureStatusTextView.text = "Cử chỉ nhấn giữ: Đã bật"
        } else {
            gestureStatusTextView.text = "Cử chỉ nhấn giữ: Đã tắt"
        }
    }
    
    private fun loadGestureFeatures() {
        val gestureFeatures = getGestureFeatures()
        
        gestureFeaturesRecyclerView.layoutManager = LinearLayoutManager(this)
        gestureFeaturesRecyclerView.adapter = GestureFeatureAdapter(gestureFeatures)
    }
    
    private fun getGestureFeatures(): List<GestureFeature> {
        return listOf(
            GestureFeature(
                title = "Cử Chỉ Chạm",
                description = "Nhận diện chạm đơn và chạm đôi",
                status = "Active",
                icon = "👆"
            ),
            GestureFeature(
                title = "Cử Chỉ Vuốt",
                description = "Nhận diện vuốt theo hướng",
                status = "Active",
                icon = "↔️"
            ),
            GestureFeature(
                title = "Cử Chỉ Chụm",
                description = "Phóng to/thu nhỏ bằng cử chỉ chụm",
                status = "Active",
                icon = "🤏"
            ),
            GestureFeature(
                title = "Cử Chỉ Nhấn Giữ",
                description = "Nhấn giữ để mở menu ngữ cảnh",
                status = "Available",
                icon = "👆"
            ),
            GestureFeature(
                title = "Cử Chỉ Đa Chạm",
                description = "Cử chỉ phức tạp với nhiều ngón tay",
                status = "Available",
                icon = "✋"
            ),
            GestureFeature(
                title = "Cử Chỉ Tùy Chỉnh",
                description = "Mẫu cử chỉ do người dùng định nghĩa",
                status = "Available",
                icon = "🎭"
            )
        )
    }
}

data class GestureFeature(
    val title: String,
    val description: String,
    val status: String,
    val icon: String
)
