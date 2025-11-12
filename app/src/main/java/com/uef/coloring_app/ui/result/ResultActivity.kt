package com.uef.coloring_app.ui.result

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.uef.coloring_app.ColoringApplication
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.setOnClickListenerWithSound
import com.uef.coloring_app.ui.history.HistoryActivity
import com.uef.coloring_app.ui.common.BaseActivity
import com.uef.coloring_app.ui.simple.SimpleMainActivity

class ResultActivity : BaseActivity() {
    
    private lateinit var congratulationsIcon: ImageView
    private lateinit var congratulationsTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var timeTextView: TextView
    private lateinit var rankTextView: TextView
    private lateinit var homeButton: Button
    private lateinit var historyButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        
        initViews()
        setupClickListeners()
        displayResults()
        playCongratulationsAnimation()
        playCongratulationsSound()
    }
    
    private fun initViews() {
        congratulationsIcon = findViewById(R.id.congratulationsIcon)
        congratulationsTextView = findViewById(R.id.congratulationsTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        timeTextView = findViewById(R.id.timeTextView)
        rankTextView = findViewById(R.id.rankTextView)
        homeButton = findViewById(R.id.homeButton)
        historyButton = findViewById(R.id.historyButton)
    }
    
    private fun setupClickListeners() {
        homeButton.setOnClickListenerWithSound {
            val intent = Intent(this, SimpleMainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
        
        historyButton.setOnClickListenerWithSound {
            val intent = Intent(this, HistoryActivity::class.java)
            startActivity(intent)
        }
    }
    
    private fun displayResults() {
        val score = intent.getIntExtra("score", 0)
        val timeSpent = intent.getLongExtra("time_spent", 0)
        val rank = intent.getIntExtra("rank", 0)
        
        scoreTextView.text = score.toString()
        timeTextView.text = formatTime(timeSpent)
        rankTextView.text = "#$rank"
        
        // Show congratulations message based on rank
        when {
            rank == 1 -> {
                congratulationsTextView.text = "🥇 CHIẾN THẮNG!"
                congratulationsTextView.setTextColor(ContextCompat.getColor(this, R.color.gold_color))
            }
            rank == 2 -> {
                congratulationsTextView.text = "🥈 XUẤT SẮC!"
                congratulationsTextView.setTextColor(ContextCompat.getColor(this, R.color.silver_color))
            }
            rank == 3 -> {
                congratulationsTextView.text = "🥉 TUYỆT VỜI!"
                congratulationsTextView.setTextColor(ContextCompat.getColor(this, R.color.bronze_color))
            }
            else -> {
                congratulationsTextView.text = "✨ LÀM TỐT LẮM!"
                congratulationsTextView.setTextColor(ContextCompat.getColor(this, R.color.primary_color))
            }
        }
    }
    
    private fun formatTime(timeInMillis: Long): String {
        val minutes = timeInMillis / 60000
        val seconds = (timeInMillis % 60000) / 1000
        return String.format("%02d:%02d", minutes, seconds)
    }
    
    private fun playCongratulationsAnimation() {
        // Scale animation for icon
        val scaleX = ObjectAnimator.ofFloat(congratulationsIcon, View.SCALE_X, 0f, 1f)
        val scaleY = ObjectAnimator.ofFloat(congratulationsIcon, View.SCALE_Y, 0f, 1f)
        
        // Rotation animation
        val rotation = ObjectAnimator.ofFloat(congratulationsIcon, View.ROTATION, 0f, 360f)
        
        // Fade in animation for text
        val fadeIn = ObjectAnimator.ofFloat(congratulationsTextView, View.ALPHA, 0f, 1f)
        
        // Combined animation set
        AnimatorSet().apply {
            playTogether(scaleX, scaleY, rotation)
            play(fadeIn).after(200)
            duration = 800
            start()
        }
    }
    
    private fun playCongratulationsSound() {
        ColoringApplication.soundManager.playCongratulationsSound()
    }
}
