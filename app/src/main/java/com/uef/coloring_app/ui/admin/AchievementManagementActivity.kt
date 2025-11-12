package com.uef.coloring_app.ui.admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.model.Achievement
import com.uef.coloring_app.data.model.AchievementType
import com.uef.coloring_app.data.repository.AchievementRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.uef.coloring_app.ui.common.BaseActivity
import java.util.UUID

class AchievementManagementActivity : BaseActivity() {

    private lateinit var achievementCountTextView: TextView
    private lateinit var achievementsRecyclerView: RecyclerView
    private lateinit var addAchievementFab: com.google.android.material.floatingactionbutton.FloatingActionButton

    private lateinit var achievementRepository: AchievementRepository
    private lateinit var achievementAdapter: AdminAchievementAdapter
    private var allAchievements: List<Achievement> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_achievement_management)
        supportActionBar?.hide()

        val achievementDao = ColoringDatabase.getDatabase(applicationContext).achievementDao()
        achievementRepository = AchievementRepository(achievementDao)

        initViews()
        setupListeners()
        setupRecyclerView()
        observeAchievements()
    }

    private fun initViews() {
        achievementCountTextView = findViewById(R.id.achievementCountTextView)
        achievementsRecyclerView = findViewById(R.id.achievementsRecyclerView)
        addAchievementFab = findViewById(R.id.addAchievementFab)
    }

    private fun setupListeners() {
        addAchievementFab.setOnClickListener {
            showAddEditAchievementDialog(null)
        }
    }

    private fun setupRecyclerView() {
        achievementAdapter = AdminAchievementAdapter(
            achievements = emptyList(),
            onEditClick = { achievement -> showAddEditAchievementDialog(achievement) },
            onDeleteClick = { achievement -> showDeleteConfirmation(achievement) }
        )
        achievementsRecyclerView.layoutManager = LinearLayoutManager(this)
        achievementsRecyclerView.adapter = achievementAdapter
    }

    private fun observeAchievements() {
        lifecycleScope.launch {
            achievementRepository.getAllAchievements().collectLatest { achievements ->
                allAchievements = achievements
                achievementAdapter.updateAchievements(achievements)
                achievementCountTextView.text = getString(R.string.total_achievements_text, achievements.size)
            }
        }
    }

    private fun showAddEditAchievementDialog(achievement: Achievement?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_achievement, null)
        
        val titleTextView = dialogView.findViewById<TextView>(R.id.dialogTitle)
        val nameEditText = dialogView.findViewById<EditText>(R.id.nameEditText)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.descriptionEditText)
        val iconEditText = dialogView.findViewById<EditText>(R.id.iconEditText)
        val requirementEditText = dialogView.findViewById<EditText>(R.id.requirementEditText)
        val pointsEditText = dialogView.findViewById<EditText>(R.id.pointsEditText)
        // Ẩn trường điểm thưởng trong runtime để chắc chắn không hiển thị
        pointsEditText.visibility = View.GONE
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<Button>(R.id.saveButton)

        // Fill data if editing
        if (achievement != null) {
            titleTextView.text = getString(R.string.edit_achievement_title)
            nameEditText.setText(achievement.name)
            descriptionEditText.setText(achievement.description)
            iconEditText.setText(achievement.icon)
            requirementEditText.setText(achievement.requirement.toString())
            // Không còn cho chỉnh điểm thưởng
        } else {
            titleTextView.text = getString(R.string.add_new_achievement)
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val name = nameEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val icon = iconEditText.text.toString().trim()
            val requirement = requirementEditText.text.toString().toIntOrNull() ?: 0
            // Điểm thưởng không dùng trong UI; gán bằng requirement để tránh giá trị rỗng
            val points = requirement
            val type = achievement?.type ?: AchievementType.FIRST_TASK

            if (name.isEmpty()) {
                Toast.makeText(this, getString(R.string.enter_achievement_name), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val achievementEntity = Achievement(
                    id = achievement?.id ?: UUID.randomUUID().toString(),
                    name = name,
                    description = description,
                    icon = icon,
                    type = type,
                    requirement = requirement,
                    points = points,
                    isUnlocked = achievement?.isUnlocked ?: false,
                    unlockedAt = achievement?.unlockedAt
                )

                if (achievement != null) {
                    achievementRepository.updateAchievement(achievementEntity)
                    Toast.makeText(this@AchievementManagementActivity, getString(R.string.achievement_updated), Toast.LENGTH_SHORT).show()
                } else {
                    achievementRepository.insertAchievement(achievementEntity)
                    Toast.makeText(this@AchievementManagementActivity, getString(R.string.achievement_added), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmation(achievement: Achievement) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_achievement_title))
            .setMessage(getString(R.string.delete_achievement_message, achievement.name))
            .setPositiveButton(getString(R.string.delete_button)) { _, _ ->
                lifecycleScope.launch {
                    achievementRepository.deleteAchievement(achievement)
                    Toast.makeText(this@AchievementManagementActivity, getString(R.string.achievement_deleted), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}


