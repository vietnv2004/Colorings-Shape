package com.uef.coloring_app.ui.simple

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.uef.coloring_app.R
import com.uef.coloring_app.core.data.DataManager
import com.uef.coloring_app.core.network.NetworkManager
import com.uef.coloring_app.core.offline.OfflineManager
import com.uef.coloring_app.core.utils.LanguageManager
import com.uef.coloring_app.core.haptic.HapticManager
import com.uef.coloring_app.data.local.entity.TaskAttemptEntity
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.data.repository.TaskAttemptRepository
import com.uef.coloring_app.ui.auth.LoginActivity
import com.uef.coloring_app.ui.profile.EditProfileActivity
import com.uef.coloring_app.ui.about.AboutActivity
import com.uef.coloring_app.ui.theme.ThemeSettingsActivity
import com.uef.coloring_app.ui.haptics.AdvancedHapticsActivity
import com.uef.coloring_app.ui.chat.ChatAIActivity
import com.uef.coloring_app.ui.network.AdvancedNetworkingActivity
import com.uef.coloring_app.ui.notifications.AdvancedNotificationsActivity
import com.uef.coloring_app.ui.security.AdvancedSecurityActivity
import com.uef.coloring_app.ui.sounds.AdvancedSoundsActivity
import com.uef.coloring_app.ui.themes.AdvancedThemesActivity
import com.uef.coloring_app.ui.analytics.AnalyticsActivity
import com.uef.coloring_app.ui.sounds.social.SocialActivity
import com.uef.coloring_app.ui.admin.AdminDashboardActivity
import kotlinx.coroutines.launch

class SimpleProfileFragment : Fragment() {

    // Views
    private lateinit var avatarImageView: android.widget.ImageView
    private lateinit var userNameTextView: TextView
    private lateinit var userEmailTextView: TextView
    private lateinit var userLevelTextView: TextView
    private lateinit var userScoreTextView: TextView
    private lateinit var languageTextView: TextView
    private lateinit var themeTextView: TextView
    private lateinit var offlineModeTextView: TextView

    // Managers
    private lateinit var offlineManager: OfflineManager
    private lateinit var networkManager: NetworkManager
    private lateinit var dataManager: DataManager
    private lateinit var sharedPreferences: SharedPreferences

    // Repositories
    private lateinit var userRepository: UserRepository
    private lateinit var taskAttemptRepository: TaskAttemptRepository

    companion object {
        private const val PREFS_NAME = "user_profile" // Changed to match EditProfileActivity
        private const val KEY_CURRENT_USER_ID = "current_user_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
    
    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View? {
        return inflater.inflate(R.layout.fragment_simple_profile, container, false)
    }
    
    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        initViews(view)
        initManagers()
        loadSettings()

        // Ẩn khu vực quản lý nếu không phải admin
        val role = sharedPreferences.getString("user_role", "participant")
        if (role != "admin") {
            val manageCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.manageCard)
            val manageLayout = manageCard?.getChildAt(0) as? android.widget.LinearLayout
            manageCard?.visibility = android.view.View.GONE
            manageLayout?.visibility = android.view.View.GONE
        }
        setupClickListeners(view)
    }

    private fun initViews(view: android.view.View) {
        avatarImageView = view.findViewById(R.id.avatarImageView)
        userNameTextView = view.findViewById(R.id.userNameTextView)
        userEmailTextView = view.findViewById(R.id.userEmailTextView)
        userLevelTextView = view.findViewById(R.id.userLevelTextView)
        userScoreTextView = view.findViewById(R.id.userScoreTextView)
        languageTextView = view.findViewById(R.id.languageTextView)
        themeTextView = view.findViewById(R.id.themeTextView)
        offlineModeTextView = view.findViewById(R.id.offlineModeTextView)

        // Edit Profile Icon
        val editProfileIcon = view.findViewById<android.widget.ImageView>(R.id.editProfileIcon)
        editProfileIcon?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            val intent = Intent(requireContext(), EditProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initManagers() {
        offlineManager = OfflineManager(requireContext())
        networkManager = NetworkManager(requireContext())
        dataManager = DataManager(requireContext())

        // Initialize repositories
        val database = com.uef.coloring_app.data.local.database.ColoringDatabase.getDatabase(requireContext())
        userRepository = UserRepository(database.userDao())
        taskAttemptRepository = TaskAttemptRepository(database.taskAttemptDao())

        offlineManager.startMonitoring()
        updateOfflineStatus()
    }

    private fun loadSettings() {
        // Load current language
        val currentLanguage = LanguageManager.getCurrentLanguage(requireContext())
        languageTextView.text = LanguageManager.getLanguageDisplayName(currentLanguage)
        
        // Load current theme
        val sharedPreferences = requireContext().getSharedPreferences("theme_prefs", android.content.Context.MODE_PRIVATE)
        val currentTheme = sharedPreferences.getString("theme_mode", "system") ?: "system"
        
        themeTextView.text = when (currentTheme) {
            "system" -> getString(R.string.system_theme)
            "light" -> getString(R.string.light_theme)
            "dark" -> getString(R.string.dark_theme)
            else -> getString(R.string.system_theme)
        }

        // Load user data from database
        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            try {
                // Load user data from SharedPreferences first
                val userName = sharedPreferences.getString("user_name", "Demo User") ?: "Demo User"
                val userEmail = sharedPreferences.getString("user_email", "demo@uef.edu.vn") ?: "demo@uef.edu.vn"
                val userLevel = sharedPreferences.getInt("user_level", 5)
                val userScore = sharedPreferences.getInt("user_score", 1250)
                
                // Update UI with loaded data
                userNameTextView.text = userName
                userEmailTextView.text = userEmail
                userLevelTextView.text = "Level $userLevel"
                userScoreTextView.text = userScore.toString()
                
                // Load profile image
                loadProfileImage()
                
                // Get current user ID from SharedPreferences
                val currentUserId = sharedPreferences.getString(KEY_CURRENT_USER_ID, null)

                if (currentUserId != null) {
                    // Load user data
                    val user = userRepository.getUserById(currentUserId)
                    if (user != null) {
                        userNameTextView.text = user.name
                        userEmailTextView.text = user.email

                        // Level theo số lượng thành tích đã mở khóa (đồng bộ với AchievementManager)
                        val achievementManager = com.uef.coloring_app.core.achievements.AchievementManager(requireContext())
                        val level = achievementManager.getUserUnlockedAchievements(currentUserId).size.coerceAtLeast(1)
                        userLevelTextView.text = "Level $level"

                        // Calculate total score
                        val userAttempts = taskAttemptRepository.getAttemptsByUser(currentUserId)
                        val totalScore = userAttempts.sumOf { it.score }
                        userScoreTextView.text = totalScore.toString()
                    } else {
                        // User not found, show default data
                        showDefaultUserData()
                    }
                } else {
                    // No current user, show default data
                    showDefaultUserData()
                }
            } catch (e: Exception) {
                // Error loading user data, show default
                showDefaultUserData()
            }
        }
    }

    private fun showDefaultUserData() {
        userNameTextView.text = "Demo User"
        userEmailTextView.text = "demo@uef.edu.vn"
        userLevelTextView.text = "Level 1"
        userScoreTextView.text = "0"
    }

    private fun calculateLevel(completedTasks: Int): Int {
        return when {
            completedTasks >= 50 -> 10
            completedTasks >= 40 -> 9
            completedTasks >= 35 -> 8
            completedTasks >= 30 -> 7
            completedTasks >= 25 -> 6
            completedTasks >= 20 -> 5
            completedTasks >= 15 -> 4
            completedTasks >= 10 -> 3
            completedTasks >= 5 -> 2
            else -> 1
        }
    }

    private fun updateOfflineStatus() {
        lifecycleScope.launch {
            try {
                val isConnected = networkManager.isNetworkAvailable()
                
                if (isConnected) {
                    val networkName = networkManager.getNetworkName()
                    offlineModeTextView.text = "Trực tuyến - $networkName"
                } else {
                    offlineModeTextView.text = "Chế độ offline"
                }
            } catch (e: Exception) {
                // Fallback to offline manager if network manager fails
                val isOffline = offlineManager.isOffline.value
                offlineModeTextView.text = if (isOffline) "Chế độ offline" else "Trực tuyến"
            }
        }
    }

    private fun setupClickListeners(view: android.view.View) {
        // Theme Settings Card
        val themeSettingsCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.themeSettingsCard)
        val themeSettingsLayout = themeSettingsCard?.getChildAt(0) as? LinearLayout
        
        themeSettingsCard?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            val intent = android.content.Intent(requireContext(), ThemeSettingsActivity::class.java)
            startActivity(intent)
        }
        themeSettingsLayout?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            val intent = android.content.Intent(requireContext(), ThemeSettingsActivity::class.java)
            startActivity(intent)
        }

        // Language Settings Card
        val languageCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.languageCard)
        val languageLayout = languageCard?.getChildAt(0) as? LinearLayout
        
        languageCard?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            showLanguageSelectionDialog()
        }
        languageLayout?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            showLanguageSelectionDialog()
        }

        // Export Card
        val exportCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.exportCard)
        val exportLayout = exportCard?.getChildAt(0) as? LinearLayout
        
        exportCard?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            exportData()
        }
        exportLayout?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            exportData()
        }

        // Import Card
        val importCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.importCard)
        val importLayout = importCard?.getChildAt(0) as? LinearLayout
        
        importCard?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            importData()
        }
        importLayout?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            importData()
        }

        // Management Card
        val manageCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.manageCard)
        val manageLayout = manageCard?.getChildAt(0) as? LinearLayout
        
        manageCard?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            val intent = Intent(requireContext(), AdminDashboardActivity::class.java)
            startActivity(intent)
        }
        manageLayout?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            val intent = Intent(requireContext(), AdminDashboardActivity::class.java)
            startActivity(intent)
        }

        // Advanced Features
        setupAdvancedFeatureListeners(view)

        // Help & Support
        setupHelpSupportListeners(view)
    }

    private fun setupAdvancedFeatureListeners(view: android.view.View) {
        // Chat AI
        val chatAiCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.chatAiCard)
        val chatAiLayout = chatAiCard?.getChildAt(0) as? LinearLayout
        
        chatAiCard?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            val intent = Intent(requireContext(), ChatAIActivity::class.java)
            startActivity(intent)
        }
        chatAiLayout?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            val intent = Intent(requireContext(), ChatAIActivity::class.java)
            startActivity(intent)
        }

        // Advanced Haptics
        val hapticsCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.advancedHapticsCard)
        val hapticsLayout = hapticsCard?.getChildAt(0) as? LinearLayout
        
        hapticsCard?.setOnClickListener {
            val intent = Intent(requireContext(), AdvancedHapticsActivity::class.java)
            startActivity(intent)
        }
        hapticsLayout?.setOnClickListener {
            val intent = Intent(requireContext(), AdvancedHapticsActivity::class.java)
            startActivity(intent)
        }

        // Advanced Networking
        val networkingCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.advancedNetworkingCard)
        val networkingLayout = networkingCard?.getChildAt(0) as? LinearLayout
        
        networkingCard?.setOnClickListener {
            val intent = Intent(requireContext(), AdvancedNetworkingActivity::class.java)
            startActivity(intent)
        }
        networkingLayout?.setOnClickListener {
            val intent = Intent(requireContext(), AdvancedNetworkingActivity::class.java)
            startActivity(intent)
        }

        // Advanced Notifications
        val notificationsCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.advancedNotificationsCard)
        val notificationsLayout = notificationsCard?.getChildAt(0) as? LinearLayout
        
        notificationsCard?.setOnClickListener {
            val intent = Intent(requireContext(), AdvancedNotificationsActivity::class.java)
            startActivity(intent)
        }
        notificationsLayout?.setOnClickListener {
            val intent = Intent(requireContext(), AdvancedNotificationsActivity::class.java)
            startActivity(intent)
        }

        // Advanced Security
        val securityCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.advancedSecurityCard)
        val securityLayout = securityCard?.getChildAt(0) as? LinearLayout
        
        securityCard?.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Tính năng Bảo mật đang phát triển", android.widget.Toast.LENGTH_SHORT).show()
        }
        securityLayout?.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Tính năng Bảo mật đang phát triển", android.widget.Toast.LENGTH_SHORT).show()
        }

        // Advanced Sounds
        val soundsCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.advancedSoundsCard)
        val soundsLayout = soundsCard?.getChildAt(0) as? LinearLayout
        
        soundsCard?.setOnClickListener {
            val intent = Intent(requireContext(), AdvancedSoundsActivity::class.java)
            startActivity(intent)
        }
        soundsLayout?.setOnClickListener {
            val intent = Intent(requireContext(), AdvancedSoundsActivity::class.java)
            startActivity(intent)
        }

        // Analytics
        val analyticsCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.advancedAnalyticsCard)
        val analyticsLayout = analyticsCard?.getChildAt(0) as? LinearLayout
        
        analyticsCard?.setOnClickListener {
            val intent = Intent(requireContext(), AnalyticsActivity::class.java)
            startActivity(intent)
        }
        analyticsLayout?.setOnClickListener {
            val intent = Intent(requireContext(), AnalyticsActivity::class.java)
            startActivity(intent)
        }

        // Social
        val socialCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.advancedSocialCard)
        val socialLayout = socialCard?.getChildAt(0) as? LinearLayout
        
        socialCard?.setOnClickListener {
            val intent = Intent(requireContext(), SocialActivity::class.java)
            startActivity(intent)
        }
        socialLayout?.setOnClickListener {
            val intent = Intent(requireContext(), SocialActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupHelpSupportListeners(view: android.view.View) {
        // About App Card
        val aboutAppCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.aboutAppCard)
        val aboutAppLayout = aboutAppCard?.getChildAt(0) as? LinearLayout
        
        aboutAppCard?.setOnClickListener {
            val intent = android.content.Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }
        aboutAppLayout?.setOnClickListener {
            val intent = android.content.Intent(requireContext(), AboutActivity::class.java)
            startActivity(intent)
        }

        // Contact Card
        val contactCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.contactCard)
        val contactLayout = contactCard?.getChildAt(0) as? LinearLayout
        
        contactCard?.setOnClickListener {
            showContactInfo()
        }
        contactLayout?.setOnClickListener {
            showContactInfo()
        }

        // Policies Card
        val policiesCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.policiesCard)
        val policiesLayout = policiesCard?.getChildAt(0) as? LinearLayout
        
        policiesCard?.setOnClickListener {
            openPrivacyPolicy()
        }
        policiesLayout?.setOnClickListener {
            openPrivacyPolicy()
        }

        // Support Center Card
        val supportCenterCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.supportCenterCard)
        val supportLayout = supportCenterCard?.getChildAt(0) as? LinearLayout
        
        supportCenterCard?.setOnClickListener {
            showSupportOptionsDialog()
        }
        supportLayout?.setOnClickListener {
            showSupportOptionsDialog()
        }

        // Logout Card
        val logoutCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.logoutCard)
        val logoutLayout = logoutCard?.getChildAt(0) as? LinearLayout
        
        logoutCard?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            showLogoutDialog()
        }
        logoutLayout?.setOnClickListener {
            HapticManager.buttonClick(requireContext())
            showLogoutDialog()
        }
    }

    private fun showSupportOptionsDialog() {
        val options = arrayOf(
            getString(R.string.about_info),
            getString(R.string.address_contact),
            getString(R.string.contact_support),
            getString(R.string.report_bug)
        )
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.support_center))
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        // About App
                        val intent = android.content.Intent(requireContext(), AboutActivity::class.java)
                        startActivity(intent)
                    }
                    1 -> {
                        // Address & Contact - Open Google Maps
                        openInMaps()
                    }
                    2 -> {
                        // Contact Support - Show phone number
                        showContactInfo()
                    }
                    3 -> {
                        // Report Bug
                        Toast.makeText(requireContext(), getString(R.string.send_bug_report_email, "bugs@coloring-shapes.com"), Toast.LENGTH_LONG).show()
                    }
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun openInMaps() {
        try {
            val mapsUrl = "https://maps.app.goo.gl/HzHVJrzxC69o1ZYc6"
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(mapsUrl))
            intent.setPackage("com.google.android.apps.maps")
            
            if (intent.resolveActivity(requireContext().packageManager) != null) {
                startActivity(intent)
            } else {
                // Fallback to browser if Google Maps app is not installed
                val browserIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(mapsUrl))
                startActivity(browserIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.cannot_open_map), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openPrivacyPolicy() {
        try {
            val privacyPolicyUrl = "https://www.freeprivacypolicy.com/live/a45cccc0-3fe5-420b-b5a8-fcf6062ae29f"
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(privacyPolicyUrl))
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.cannot_open_privacy_policy), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showContactInfo() {
        val message = "📞 ${getString(R.string.phone_label, "+84 28 5422 6666")}\n\n📍 ${getString(R.string.address_label, "145 Điện Biên Phủ, Phường 2, Bình Thạnh, TP.HCM, Việt Nam")}\n\n🌐 ${getString(R.string.website_label, "uef.edu.vn")}"
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.contact_information))
            .setMessage(message)
            .setPositiveButton(getString(R.string.call_phone)) { _, _ ->
                val phoneNumber = "+842854226666"
                val callIntent = android.content.Intent(android.content.Intent.ACTION_DIAL)
                callIntent.data = android.net.Uri.parse("tel:$phoneNumber")
                startActivity(callIntent)
            }
            .setNeutralButton(getString(R.string.open_map)) { _, _ ->
                openInMaps()
            }
            .setNegativeButton(getString(R.string.close), null)
            .show()
    }

    private fun showLanguageSelectionDialog() {
        val languages = LanguageManager.getLanguageDisplayNamesForUI().toTypedArray()
        val languageCodes = LanguageManager.getLanguageCodesForUI()
        val currentLanguage = LanguageManager.getCurrentLanguage(requireContext())
        val currentIndex = languageCodes.indexOf(currentLanguage).takeIf { it >= 0 } ?: 0

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.choose_language))
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguage = languageCodes[which]
                
                // Chỉ thay đổi nếu ngôn ngữ khác với hiện tại
                if (selectedLanguage != currentLanguage) {
                    // Thay đổi ngôn ngữ và áp dụng ngay lập tức
                    LanguageManager.changeLanguage(requireContext(), selectedLanguage)
                    
                    // Cập nhật UI
                    languageTextView.text = languages[which]
                    
                    // Hiển thị thông báo
                    val message = if (selectedLanguage == "vi") {
                        getString(R.string.language_changed_vietnamese)
                    } else {
                        getString(R.string.language_changed_english)
                    }
                    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                    
                    // Rung khi thay đổi ngôn ngữ thành công
                    HapticManager.success(requireContext())
                    
                    dialog.dismiss()
                    
                    // Restart toàn bộ app để áp dụng ngôn ngữ mới cho tất cả activities
                    restartApp()
                } else {
                    dialog.dismiss()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun exportData() {
        try {
            lifecycleScope.launch {
                // For now, just show a message since we need a file output stream
                Toast.makeText(requireContext(), getString(R.string.export_feature_developing), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.export_error) + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun importData() {
        try {
            lifecycleScope.launch {
                // For now, just show a message since we need a file input stream
                Toast.makeText(requireContext(), getString(R.string.import_feature_developing), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), getString(R.string.import_error) + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.logout_confirm))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun performLogout() {
        try {
            // Clear user data
            sharedPreferences.edit().clear().apply()

            // Stop offline monitoring
            offlineManager.stopMonitoring()

            // Navigate to login activity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            // Finish current activity
            requireActivity().finish()

            Toast.makeText(requireContext(), getString(R.string.logout_success), Toast.LENGTH_SHORT).show()
            
            // Rung khi logout thành công
            HapticManager.success(requireContext())
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi khi đăng xuất: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun refreshUserData() {
        loadUserData()
    }
    
    private fun loadProfileImage() {
        try {
            // Load profile image from SharedPreferences
            val imagePath = sharedPreferences.getString("profile_image_path", null)
            
            if (imagePath != null && imagePath.isNotEmpty()) {
                val file = java.io.File(imagePath)
                if (file.exists()) {
                    // Load image using Glide or BitmapFactory
                    val bitmap = android.graphics.BitmapFactory.decodeFile(file.absolutePath)
                    bitmap?.let {
                        avatarImageView.setImageBitmap(it)
                    }
                } else {
                    // File doesn't exist, use default avatar
                    avatarImageView.setImageResource(R.drawable.ic_user_avatar)
                }
            } else {
                // No image path, use default avatar
                avatarImageView.setImageResource(R.drawable.ic_user_avatar)
            }
        } catch (e: Exception) {
            // Error loading image, use default avatar
            avatarImageView.setImageResource(R.drawable.ic_user_avatar)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh user data when fragment resumes (in case data changed in other activities)
        refreshUserData()
        // Refresh network status
        updateOfflineStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        offlineManager.stopMonitoring()
    }
    
    /**
     * Restart toàn bộ app để áp dụng ngôn ngữ mới
     */
    private fun restartApp() {
        try {
            // Tạo intent để restart app từ SimpleMainActivity
            val intent = Intent(requireContext(), SimpleMainActivity::class.java)
            
            // Clear tất cả activities hiện tại và tạo task mới
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            
            // Thêm delay nhỏ để Toast hiển thị trước khi restart
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                requireContext().startActivity(intent)
                requireActivity().finishAffinity() // Đóng tất cả activities
            }, 1000) // Delay 1 giây
        } catch (e: Exception) {
            // Fallback: chỉ restart activity hiện tại nếu có lỗi
            requireActivity().recreate()
        }
    }
}