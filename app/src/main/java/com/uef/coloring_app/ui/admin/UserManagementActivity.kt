package com.uef.coloring_app.ui.admin

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.database.ColoringDatabase
import com.uef.coloring_app.data.local.entity.UserEntity
import com.uef.coloring_app.data.repository.UserRepository
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class UserManagementActivity : BaseActivity() {
    
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var addUserButton: Button
    private lateinit var exportExcelButton: Button
    private lateinit var filterSpinner: Spinner
    private lateinit var searchEditText: EditText
    private lateinit var totalUsersTextView: TextView
    
    private lateinit var userRepository: UserRepository
    private lateinit var userAdapter: UserAdapter
    private var allUsers = listOf<UserEntity>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)
        
        // Hide action bar
        supportActionBar?.hide()
        
        initViews()
        setupDatabase()
        setupRecyclerView()
        setupClickListeners()
        loadUsers()
    }
    
    private fun initViews() {
        usersRecyclerView = findViewById(R.id.usersRecyclerView)
        addUserButton = findViewById(R.id.addUserButton)
        exportExcelButton = findViewById(R.id.exportExcelButton)
        filterSpinner = findViewById(R.id.filterSpinner)
        searchEditText = findViewById(R.id.searchEditText)
        totalUsersTextView = findViewById(R.id.totalUsersTextView)
    }
    
    private fun setupDatabase() {
        val database = ColoringDatabase.getDatabase(this)
        userRepository = UserRepository(database.userDao())
    }
    
    private fun setupRecyclerView() {
        userAdapter = UserAdapter(
            onEditClick = { user -> showEditUserDialog(user) },
            onDeleteClick = { user -> showDeleteConfirmation(user) },
            onToggleActiveClick = { user -> toggleUserActive(user) }
        )
        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = userAdapter
    }
    
    private fun setupClickListeners() {
        addUserButton.setOnClickListener {
            showAddUserDialog()
        }
        
        exportExcelButton.setOnClickListener {
            exportToExcel()
        }
        
        // Filter setup
        val filters = arrayOf(
            getString(R.string.filter_all_users),
            getString(R.string.filter_users_role),
            getString(R.string.filter_admin_role),
            getString(R.string.filter_active_users),
            getString(R.string.filter_locked_users)
        )
        filterSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filters)
        filterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                filterUsers(position)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Search
        searchEditText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchUsers(s.toString())
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }
    
    private fun loadUsers() {
        lifecycleScope.launch {
            userRepository.getAllUsers().collectLatest { users ->
                allUsers = users
                userAdapter.submitList(users)
                totalUsersTextView.text = getString(R.string.total_users_text, users.size)
            }
        }
    }
    
    private fun filterUsers(filterType: Int) {
        val filtered = when (filterType) {
            0 -> allUsers // Tất cả
            1 -> allUsers.filter { it.role == "participant" } // Người dùng
            2 -> allUsers.filter { it.role == "admin" } // Quản trị viên
            3 -> allUsers.filter { it.isActive } // Đang hoạt động
            4 -> allUsers.filter { !it.isActive } // Bị khóa
            else -> allUsers
        }
        userAdapter.submitList(filtered)
        totalUsersTextView.text = getString(R.string.total_users_text, filtered.size)
    }
    
    private fun searchUsers(query: String) {
        if (query.isEmpty()) {
            userAdapter.submitList(allUsers)
            return
        }
        
        val filtered = allUsers.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.email.contains(query, ignoreCase = true)
        }
        userAdapter.submitList(filtered)
        totalUsersTextView.text = getString(R.string.found_users_text, filtered.size)
    }
    
    private fun showAddUserDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.add_new_user_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.add_user_button), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = dialogView.findViewById<EditText>(R.id.nameEditText).text.toString()
                val email = dialogView.findViewById<EditText>(R.id.emailEditText).text.toString()
                val password = dialogView.findViewById<EditText>(R.id.passwordEditText).text.toString()
                val birthYear = dialogView.findViewById<EditText>(R.id.birthYearEditText).text.toString()
                val genderSpinner = dialogView.findViewById<Spinner>(R.id.genderSpinner)
                val roleSpinner = dialogView.findViewById<Spinner>(R.id.roleSpinner)
                
                if (validateInput(name, email, password, birthYear)) {
                    val user = UserEntity(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        email = email,
                        password = password,
                        birthYear = birthYear.toInt(),
                        gender = genderSpinner.selectedItem.toString(),
                        role = if (roleSpinner.selectedItemPosition == 0) "participant" else "admin",
                        isActive = true,
                        createdAt = System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    lifecycleScope.launch {
                        userRepository.insertUser(user)
                        Toast.makeText(this@UserManagementActivity, getString(R.string.user_added_success), Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            }
        }
        
        dialog.show()
    }
    
    private fun showEditUserDialog(user: UserEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_user, null)
        
        // Populate existing data
        dialogView.findViewById<EditText>(R.id.nameEditText).setText(user.name)
        dialogView.findViewById<EditText>(R.id.emailEditText).setText(user.email)
        dialogView.findViewById<EditText>(R.id.passwordEditText).setText(user.password)
        dialogView.findViewById<EditText>(R.id.birthYearEditText).setText(user.birthYear.toString())
        
        val genderSpinner = dialogView.findViewById<Spinner>(R.id.genderSpinner)
        genderSpinner.setSelection(if (user.gender == "Nam") 0 else 1)
        
        val roleSpinner = dialogView.findViewById<Spinner>(R.id.roleSpinner)
        roleSpinner.setSelection(if (user.role == "participant") 0 else 1)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.edit_user_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.update_button), null)
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
        
        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = dialogView.findViewById<EditText>(R.id.nameEditText).text.toString()
                val email = dialogView.findViewById<EditText>(R.id.emailEditText).text.toString()
                val password = dialogView.findViewById<EditText>(R.id.passwordEditText).text.toString()
                val birthYear = dialogView.findViewById<EditText>(R.id.birthYearEditText).text.toString()
                val genderSpinnerView = dialogView.findViewById<Spinner>(R.id.genderSpinner)
                val roleSpinnerView = dialogView.findViewById<Spinner>(R.id.roleSpinner)
                
                if (validateInput(name, email, password, birthYear)) {
                    val updatedUser = user.copy(
                        name = name,
                        email = email,
                        password = password,
                        birthYear = birthYear.toInt(),
                        gender = genderSpinnerView.selectedItem.toString(),
                        role = if (roleSpinnerView.selectedItemPosition == 0) "participant" else "admin",
                        updatedAt = System.currentTimeMillis()
                    )
                    
                    lifecycleScope.launch {
                        userRepository.updateUser(updatedUser)
                        Toast.makeText(this@UserManagementActivity, getString(R.string.user_updated_success), Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
            }
        }
        
        dialog.show()
    }
    
    private fun showDeleteConfirmation(user: UserEntity) {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.delete_user_title))
            .setMessage(getString(R.string.delete_user_message, user.name))
            .setPositiveButton(getString(R.string.delete_button)) { _, _ ->
                lifecycleScope.launch {
                    userRepository.deleteUser(user)
                    Toast.makeText(this@UserManagementActivity, getString(R.string.user_deleted_success), Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
    
    private fun toggleUserActive(user: UserEntity) {
        lifecycleScope.launch {
            val updatedUser = user.copy(
                isActive = !user.isActive,
                updatedAt = System.currentTimeMillis()
            )
            userRepository.updateUser(updatedUser)
            val status = if (updatedUser.isActive) getString(R.string.user_activated) else getString(R.string.user_locked)
            Toast.makeText(this@UserManagementActivity, getString(R.string.user_status_changed, status), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun validateInput(name: String, email: String, password: String, birthYear: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, getString(R.string.enter_name_hint), Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, getString(R.string.invalid_email), Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(this, getString(R.string.password_min_length), Toast.LENGTH_SHORT).show()
            return false
        }
        if (birthYear.isEmpty() || birthYear.toIntOrNull() == null || birthYear.toInt() < 1900 || birthYear.toInt() > 2024) {
            Toast.makeText(this, getString(R.string.invalid_birth_year), Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
    
    private fun exportToExcel() {
        lifecycleScope.launch {
            try {
                // Create CSV content
                val csvContent = StringBuilder()
                
                // Header
                csvContent.append("ID,Tên,Email,Năm sinh,Giới tính,Vai trò,Trạng thái,Ngày tạo\n")
                
                // Data rows
                allUsers.forEach { user ->
                    csvContent.append("${user.id},")
                    csvContent.append("\"${user.name}\",")
                    csvContent.append("${user.email},")
                    csvContent.append("${user.birthYear},")
                    csvContent.append("${user.gender},")
                    csvContent.append("${if (user.role == "admin") "Quản trị viên" else "Người dùng"},")
                    csvContent.append("${if (user.isActive) "Hoạt động" else "Bị khóa"},")
                    csvContent.append("${android.text.format.DateFormat.format("dd/MM/yyyy", java.util.Date(user.createdAt))}\n")
                }
                
                // Save to Downloads folder
                val fileName = "Danh_sach_nguoi_dung_${System.currentTimeMillis()}.csv"
                val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                val file = File(downloadsDir, fileName)
                
                // Write content to file
                FileOutputStream(file).use { out ->
                    out.write(csvContent.toString().toByteArray())
                }
                
                Toast.makeText(
                    this@UserManagementActivity,
                    "✅ Đã xuất file Excel thành công!",
                    Toast.LENGTH_LONG
                ).show()
                
                // Ask if user wants to open the file
                AlertDialog.Builder(this@UserManagementActivity)
                    .setTitle(getString(R.string.open_excel_file))
                    .setMessage(getString(R.string.open_excel_question))
                    .setPositiveButton(getString(R.string.open_file)) { _, _ ->
                        openExcelFile(file)
                    }
                    .setNegativeButton(getString(R.string.no_capital), null)
                    .show()
                
            } catch (e: Exception) {
                Toast.makeText(
                    this@UserManagementActivity,
                    "❌ Lỗi xuất Excel: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun openExcelFile(file: File) {
        try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                FileProvider.getUriForFile(
                    this,
                    "com.uef.coloring_app.fileprovider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/csv")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            startActivity(Intent.createChooser(intent, "Mở với"))
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Không thể mở file. Vui lòng kiểm tra thư mục Downloads.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
