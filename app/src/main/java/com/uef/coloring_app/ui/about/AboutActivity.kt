package com.uef.coloring_app.ui.about

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.uef.coloring_app.R
import com.uef.coloring_app.core.haptic.HapticManager
import com.uef.coloring_app.ui.common.BaseActivity

class AboutActivity : BaseActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private var isAdmin = false
    
    // TextViews để hiển thị nội dung
    private lateinit var introTextView: TextView
    private lateinit var whoWeAreTextView: TextView
    private lateinit var importantInfoTextView: TextView
    private lateinit var collectionTextView: TextView
    private lateinit var policyTextView: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        
        sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        isAdmin = checkIfAdmin()
        
        setupToolbar()
        initViews()
        loadContent()
    }
    
    private fun checkIfAdmin(): Boolean {
        val userEmail = sharedPreferences.getString("user_email", "")
        return userEmail?.contains("admin", ignoreCase = true) == true
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        // Don't set as support action bar to avoid conflict
        toolbar.title = getString(R.string.about_app)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // Set menu để hiển thị nút edit nếu là admin
        if (isAdmin) {
            toolbar.inflateMenu(R.menu.menu_about_admin)
            toolbar.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_manage_contact -> {
                        showManageContactDialog()
                        HapticManager.success(this)
                        true
                    }
                    R.id.action_manage_info -> {
                        showEditAboutDialog()
                        HapticManager.success(this)
                        true
                    }
                    else -> false
                }
            }
        }
    }
    
    private fun initViews() {
        // Find TextViews by IDs (add IDs to layout file)
        introTextView = findViewById(R.id.introTextView)
        whoWeAreTextView = findViewById(R.id.whoWeAreTextView)
        importantInfoTextView = findViewById(R.id.importantInfoTextView)
        collectionTextView = findViewById(R.id.collectionTextView)
        policyTextView = findViewById(R.id.policyTextView)
    }
    
    private fun loadContent() {
        // Load content from SharedPreferences or use default
        introTextView.text = sharedPreferences.getString("about_introduction", getString(R.string.about_introduction))
        whoWeAreTextView.text = sharedPreferences.getString("about_who_we_are_content", getString(R.string.about_who_we_are_content))
        importantInfoTextView.text = sharedPreferences.getString("about_important_info_content", getString(R.string.about_important_info_content))
        collectionTextView.text = sharedPreferences.getString("about_collection_content", getString(R.string.about_collection_content))
        policyTextView.text = sharedPreferences.getString("about_policy_content", getString(R.string.about_policy_content))
    }
    
    /**
     * Dialog quản lý thông tin liên hệ (chỉ dành cho admin)
     */
    private fun showManageContactDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_contact, null)
        
        val phoneEditText = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.phoneEditText)
        val addressEditText = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.addressEditText)
        val websiteEditText = view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.websiteEditText)
        
        // Load thông tin hiện tại
        phoneEditText.setText(sharedPreferences.getString("contact_phone", "+84 123 456 789"))
        addressEditText.setText(sharedPreferences.getString("contact_address", "141 Điện Biên Phủ, Quận Bình Thạnh, TP. Hồ Chí Minh"))
        websiteEditText.setText(sharedPreferences.getString("contact_website", "https://www.coloring-shapes.com"))
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.manage_contact_info))
            .setView(view)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // Lưu vào SharedPreferences
                sharedPreferences.edit()
                    .putString("contact_phone", phoneEditText.text.toString())
                    .putString("contact_address", addressEditText.text.toString())
                    .putString("contact_website", websiteEditText.text.toString())
                    .apply()
                
                Toast.makeText(this, getString(R.string.contact_info_saved), Toast.LENGTH_SHORT).show()
                HapticManager.success(this)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    /**
     * Dialog chỉnh sửa thông tin giới thiệu (chỉ dành cho admin)
     */
    private fun showEditAboutDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_edit_about, null)
        
        val introEditText = view.findViewById<android.widget.EditText>(R.id.introEditText)
        val whoWeAreEditText = view.findViewById<android.widget.EditText>(R.id.whoWeAreEditText)
        val importantInfoEditText = view.findViewById<android.widget.EditText>(R.id.importantInfoEditText)
        val collectionEditText = view.findViewById<android.widget.EditText>(R.id.collectionEditText)
        val policyEditText = view.findViewById<android.widget.EditText>(R.id.policyEditText)
        
        // Load thông tin hiện tại
        introEditText.setText(sharedPreferences.getString("about_introduction", getString(R.string.about_introduction)))
        whoWeAreEditText.setText(sharedPreferences.getString("about_who_we_are_content", getString(R.string.about_who_we_are_content)))
        importantInfoEditText.setText(sharedPreferences.getString("about_important_info_content", getString(R.string.about_important_info_content)))
        collectionEditText.setText(sharedPreferences.getString("about_collection_content", getString(R.string.about_collection_content)))
        policyEditText.setText(sharedPreferences.getString("about_policy_content", getString(R.string.about_policy_content)))
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.manage_app_info))
            .setView(view)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                // Lưu vào SharedPreferences
                sharedPreferences.edit()
                    .putString("about_introduction", introEditText.text.toString())
                    .putString("about_who_we_are_content", whoWeAreEditText.text.toString())
                    .putString("about_important_info_content", importantInfoEditText.text.toString())
                    .putString("about_collection_content", collectionEditText.text.toString())
                    .putString("about_policy_content", policyEditText.text.toString())
                    .apply()
                
                // Reload content
                loadContent()
                
                Toast.makeText(this, getString(R.string.app_info_saved), Toast.LENGTH_SHORT).show()
                HapticManager.success(this)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}
