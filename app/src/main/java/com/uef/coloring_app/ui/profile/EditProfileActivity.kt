package com.uef.coloring_app.ui.profile

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.uef.coloring_app.R
import com.uef.coloring_app.ui.common.BaseActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class EditProfileActivity : BaseActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userNameEditText: EditText
    private lateinit var userEmailEditText: EditText
    private lateinit var birthYearEditText: EditText
    private lateinit var genderRadioGroup: RadioGroup
    private lateinit var maleRadio: RadioButton
    private lateinit var femaleRadio: RadioButton
    private lateinit var otherRadio: RadioButton
    private lateinit var currentPasswordEditText: EditText
    private lateinit var newPasswordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private var userLevelTextView: TextView? = null
    private var userScoreTextView: TextView? = null
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var avatarImageView: ImageView
    
    private var currentPhotoPath: String? = null
    private var selectedImageUri: Uri? = null
    
    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1002
    }
    
    // Image picker launchers
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            if (data?.data != null) {
                selectedImageUri = data.data
                loadSelectedImage()
            }
        }
    }
    
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoPath?.let { path ->
                val bitmap = BitmapFactory.decodeFile(path)
                bitmap?.let {
                    selectedImageUri = saveImageToInternalStorage(it)
                    loadSelectedImage()
                }
                // Xóa file tạm thời sau khi sử dụng
                try {
                    File(path).delete()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        
        sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        setupToolbar()
        initViews()
        loadUserData()
        setupClickListeners()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Dọn dẹp các file tạm thời khi activity bị hủy
        cleanupTempFiles()
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.title = getString(R.string.edit_profile)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun initViews() {
        sharedPreferences = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
        userNameEditText = findViewById(R.id.userNameEditText)
        userEmailEditText = findViewById(R.id.userEmailEditText)
        birthYearEditText = findViewById(R.id.birthYearEditText)
        genderRadioGroup = findViewById(R.id.genderRadioGroup)
        maleRadio = findViewById(R.id.maleRadio)
        femaleRadio = findViewById(R.id.femaleRadio)
        otherRadio = findViewById(R.id.otherRadio)
        currentPasswordEditText = findViewById(R.id.currentPasswordEditText)
        newPasswordEditText = findViewById(R.id.newPasswordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        
        // Try to find optional views (may not exist in layout)
        try {
            userLevelTextView = findViewById(R.id.userLevelTextView)
        } catch (e: Exception) {
            // View doesn't exist, userLevelTextView will be null
        }
        
        try {
            userScoreTextView = findViewById(R.id.userScoreTextView)
        } catch (e: Exception) {
            // View doesn't exist, userScoreTextView will be null
        }
        
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        avatarImageView = findViewById(R.id.avatarImageView)
    }
    
    private fun loadUserData() {
        val userName = sharedPreferences.getString("user_name", "Demo User") ?: "Demo User"
        val userEmail = sharedPreferences.getString("user_email", "demo@uef.edu.vn") ?: "demo@uef.edu.vn"
        val birthYear = sharedPreferences.getString("birth_year", "2000") ?: "2000"
        val gender = sharedPreferences.getString("gender", "male") ?: "male"
        val userLevel = sharedPreferences.getInt("user_level", 5)
        val userScore = sharedPreferences.getInt("user_score", 1250)
        val profileImagePath = sharedPreferences.getString("profile_image_path", null)
        
        userNameEditText.setText(userName)
        userEmailEditText.setText(userEmail)
        birthYearEditText.setText(birthYear)
        
        // Set gender radio button
        when (gender) {
            "male" -> maleRadio.isChecked = true
            "female" -> femaleRadio.isChecked = true
            "other" -> otherRadio.isChecked = true
        }
        
        // Update optional views if they exist
        userLevelTextView?.text = "Level $userLevel"
        userScoreTextView?.text = userScore.toString()
        
        // Load profile image
        loadProfileImage(profileImagePath)
    }
    
    private fun setupClickListeners() {
        saveButton.setOnClickListener {
            saveUserData()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
        
        avatarImageView.setOnClickListener {
            showImagePickerDialog()
        }
    }
    
    private fun saveUserData() {
        val userName = userNameEditText.text.toString().trim()
        val userEmail = userEmailEditText.text.toString().trim()
        val birthYear = birthYearEditText.text.toString().trim()
        val currentPassword = currentPasswordEditText.text.toString().trim()
        val newPassword = newPasswordEditText.text.toString().trim()
        val confirmPassword = confirmPasswordEditText.text.toString().trim()
        
        // Get selected gender
        val selectedGender = when (genderRadioGroup.checkedRadioButtonId) {
            R.id.maleRadio -> "male"
            R.id.femaleRadio -> "female"
            R.id.otherRadio -> "other"
            else -> "male"
        }
        
        // Validate basic fields
        if (userName.isEmpty()) {
            userNameEditText.error = "Tên người dùng không được để trống"
            return
        }
        
        if (userEmail.isEmpty()) {
            userEmailEditText.error = "Email không được để trống"
            return
        }
        
        if (!isValidEmail(userEmail)) {
            userEmailEditText.error = "Email không hợp lệ"
            return
        }
        
        if (birthYear.isEmpty()) {
            birthYearEditText.error = "Năm sinh không được để trống"
            return
        }
        
        val year = birthYear.toIntOrNull()
        if (year == null || year < 1900 || year > 2024) {
            birthYearEditText.error = "Năm sinh không hợp lệ"
            return
        }
        
        // Validate password change if any password field is filled
        if (currentPassword.isNotEmpty() || newPassword.isNotEmpty() || confirmPassword.isNotEmpty()) {
            if (currentPassword.isEmpty()) {
                currentPasswordEditText.error = "Vui lòng nhập mật khẩu hiện tại"
                return
            }
            
            if (newPassword.isEmpty()) {
                newPasswordEditText.error = "Vui lòng nhập mật khẩu mới"
                return
            }
            
            if (confirmPassword.isEmpty()) {
                confirmPasswordEditText.error = "Vui lòng xác nhận mật khẩu mới"
                return
            }
            
            if (newPassword != confirmPassword) {
                confirmPasswordEditText.error = "Mật khẩu xác nhận không khớp"
                return
            }
            
            if (newPassword.length < 6) {
                newPasswordEditText.error = "Mật khẩu phải có ít nhất 6 ký tự"
                return
            }
            
            // TODO: Verify current password with stored password
            // For now, we'll just save the new password
        }
        
        // Save user data
        val editor = sharedPreferences.edit()
        editor.putString("user_name", userName)
        editor.putString("user_email", userEmail)
        editor.putString("birth_year", birthYear)
        editor.putString("gender", selectedGender)
        
        // Save new password if provided
        if (newPassword.isNotEmpty()) {
            editor.putString("user_password", newPassword)
        }
        
        // Save profile image path if selected
        selectedImageUri?.let { uri ->
            val imagePath = saveImageToInternalStorage(getBitmapFromUri(uri))
            imagePath?.let { path ->
                // Convert URI to absolute file path
                val absolutePath = if (path.scheme == "file") {
                    path.path
                } else {
                    path.toString()
                }
                editor.putString("profile_image_path", absolutePath)
            }
        }
        
        editor.apply()
        
        Toast.makeText(this, "Đã lưu thông tin hồ sơ", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    // Image picker methods
    private fun showImagePickerDialog() {
        val options = arrayOf(
            getString(R.string.take_photo),
            getString(R.string.choose_from_gallery),
            getString(R.string.cancel)
        )
        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.select_profile_image))
        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    if (checkCameraPermission()) {
                        openCamera()
                    } else {
                        requestCameraPermission()
                    }
                }
                1 -> {
                    // ACTION_PICK does not require storage permission
                    openImagePicker()
                }
            }
        }
        builder.show()
    }
    
    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, getString(R.string.camera_permission_required), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(packageManager) != null) {
            val photoFile = createImageFile()
            photoFile?.let { file ->
                currentPhotoPath = file.absolutePath
                val photoURI = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    file
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                cameraLauncher.launch(intent)
            }
        }
    }
    
    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }
    
    private fun createImageFile(): File? {
        return try {
            // Sử dụng internal storage thay vì external storage
            val profileImagesDir = File(filesDir, "profile_images")
            if (!profileImagesDir.exists()) {
                profileImagesDir.mkdirs()
            }
            
            // Tạo file tạm thời cho camera trong internal storage
            File.createTempFile(
                "camera_temp_",
                ".jpg",
                profileImagesDir
            )
        } catch (ex: IOException) {
            ex.printStackTrace()
            null
        }
    }
    
    private fun loadSelectedImage() {
        selectedImageUri?.let { uri ->
            Glide.with(this)
                .load(uri)
                .circleCrop()
                .into(avatarImageView)
        }
    }
    
    private fun loadProfileImage(imagePath: String?) {
        if (imagePath != null) {
            val file = File(imagePath)
            if (file.exists()) {
                Glide.with(this)
                    .load(file)
                    .circleCrop()
                    .into(avatarImageView)
            } else {
                avatarImageView.setImageResource(R.drawable.ic_user_avatar)
            }
        } else {
            avatarImageView.setImageResource(R.drawable.ic_user_avatar)
        }
    }
    
    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            null
        }
    }
    
    private fun saveImageToInternalStorage(bitmap: Bitmap?): Uri? {
        return try {
            bitmap?.let { bmp ->
                // Tạo thư mục riêng cho ảnh profile trong internal storage
                val profileImagesDir = File(filesDir, "profile_images")
                if (!profileImagesDir.exists()) {
                    profileImagesDir.mkdirs()
                }
                
                // Xóa ảnh profile cũ nếu có
                deleteOldProfileImages(profileImagesDir)
                
                // Lưu ảnh mới với tên cố định
                val fileName = "user_profile_image.jpg"
                val file = File(profileImagesDir, fileName)
                val outputStream = FileOutputStream(file)
                bmp.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                outputStream.close()
                
                // Trả về URI từ internal storage
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    private fun deleteOldProfileImages(directory: File) {
        try {
            directory.listFiles()?.forEach { file ->
                if (file.name.startsWith("user_profile_image") || file.name.startsWith("profile_image_")) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun cleanupTempFiles() {
        try {
            val profileImagesDir = File(filesDir, "profile_images")
            if (profileImagesDir.exists()) {
                profileImagesDir.listFiles()?.forEach { file ->
                    // Chỉ xóa các file tạm thời, giữ lại ảnh profile chính
                    if (file.name.startsWith("camera_temp_") || file.name.startsWith("temp_")) {
                        file.delete()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
