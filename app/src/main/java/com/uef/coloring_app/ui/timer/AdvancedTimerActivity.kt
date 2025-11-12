package com.uef.coloring_app.ui.timer

import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class AdvancedTimerActivity : AppCompatActivity() {
    
    private lateinit var timerStatusTextView: TextView
    private lateinit var timerDisplayTextView: TextView
    private lateinit var timerFeaturesRecyclerView: RecyclerView
    private lateinit var startTimerButton: Button
    private lateinit var pauseTimerButton: Button
    private lateinit var resetTimerButton: Button
    private lateinit var timerSeekBar: SeekBar
    private lateinit var timerModeTextView: TextView
    
    private var countDownTimer: CountDownTimer? = null
    private var timeLeftInMillis: Long = 0
    private var isTimerRunning = false
    private var currentMode = "Normal"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_timer)
        
        initViews()
        setupClickListeners()
        setupSeekBar()
        loadTimerFeatures()
    }
    
    private fun initViews() {
        timerStatusTextView = findViewById(R.id.timerStatusTextView)
        timerDisplayTextView = findViewById(R.id.timerDisplayTextView)
        timerFeaturesRecyclerView = findViewById(R.id.timerFeaturesRecyclerView)
        startTimerButton = findViewById(R.id.startTimerButton)
        pauseTimerButton = findViewById(R.id.pauseTimerButton)
        resetTimerButton = findViewById(R.id.resetTimerButton)
        timerSeekBar = findViewById(R.id.timerSeekBar)
        timerModeTextView = findViewById(R.id.timerModeTextView)
    }
    
    private fun setupClickListeners() {
        startTimerButton.setOnClickListener {
            startTimer()
        }
        
        pauseTimerButton.setOnClickListener {
            pauseTimer()
        }
        
        resetTimerButton.setOnClickListener {
            resetTimer()
        }
    }
    
    private fun setupSeekBar() {
        timerSeekBar.max = 3600 // 60 minutes in seconds
        timerSeekBar.progress = 300 // 5 minutes default
        timerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timeLeftInMillis = progress * 1000L
                updateTimerDisplay()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun startTimer() {
        if (!isTimerRunning) {
            countDownTimer = object : CountDownTimer(timeLeftInMillis, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    timeLeftInMillis = millisUntilFinished
                    updateTimerDisplay()
                }
                
                override fun onFinish() {
                    timerStatusTextView.text = "Timer: Finished"
                    isTimerRunning = false
                    // TODO: Show notification or play sound
                }
            }.start()
            
            isTimerRunning = true
            timerStatusTextView.text = "Timer: Running"
        }
    }
    
    private fun pauseTimer() {
        if (isTimerRunning) {
            countDownTimer?.cancel()
            isTimerRunning = false
            timerStatusTextView.text = "Timer: Paused"
        }
    }
    
    private fun resetTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        timeLeftInMillis = timerSeekBar.progress * 1000L
        updateTimerDisplay()
        timerStatusTextView.text = "Timer: Reset"
    }
    
    private fun updateTimerDisplay() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        val timeString = String.format("%02d:%02d", minutes, seconds)
        timerDisplayTextView.text = timeString
    }
    
    private fun loadTimerFeatures() {
        val timerFeatures = getTimerFeatures()
        
        timerFeaturesRecyclerView.layoutManager = LinearLayoutManager(this)
        timerFeaturesRecyclerView.adapter = TimerFeatureAdapter(timerFeatures)
    }
    
    private fun getTimerFeatures(): List<TimerFeature> {
        return listOf(
            TimerFeature(
                title = "Pomodoro Timer",
                description = "25 minutes work, 5 minutes break",
                status = "Active",
                icon = "🍅"
            ),
            TimerFeature(
                title = "Custom Timer",
                description = "Set your own timer duration",
                status = "Active",
                icon = "⏰"
            ),
            TimerFeature(
                title = "Interval Timer",
                description = "Multiple intervals with breaks",
                status = "Available",
                icon = "🔄"
            ),
            TimerFeature(
                title = "Sound Alerts",
                description = "Audio notifications when timer ends",
                status = "Active",
                icon = "🔊"
            ),
            TimerFeature(
                title = "Visual Alerts",
                description = "Screen flash when timer ends",
                status = "Active",
                icon = "💡"
            ),
            TimerFeature(
                title = "Vibration Alerts",
                description = "Device vibration when timer ends",
                status = "Available",
                icon = "📳"
            )
        )
    }
    
    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}

data class TimerFeature(
    val title: String,
    val description: String,
    val status: String,
    val icon: String
)
