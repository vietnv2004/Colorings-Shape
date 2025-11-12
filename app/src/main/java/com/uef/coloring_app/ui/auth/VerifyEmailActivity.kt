package com.uef.coloring_app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.uef.coloring_app.R
import com.uef.coloring_app.ui.common.BaseActivity
import com.google.firebase.auth.FirebaseAuth

class VerifyEmailActivity : BaseActivity() {

    private lateinit var codeEditText: EditText
    private lateinit var verifyButton: MaterialButton
    private lateinit var resendButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_email)

        codeEditText = findViewById(R.id.codeEditText)
        verifyButton = findViewById(R.id.verifyButton)
        resendButton = findViewById(R.id.resendButton)

        verifyButton.setOnClickListener {
            val code = codeEditText.text.toString().trim()
            if (code.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập mã xác thực", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            FirebaseAuth.getInstance().applyActionCode(code)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Xác thực email thành công", Toast.LENGTH_LONG).show()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, task.exception?.localizedMessage ?: "Mã không hợp lệ", Toast.LENGTH_LONG).show()
                    }
                }
        }

        resendButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                Toast.makeText(this, "Vui lòng đăng nhập trước khi gửi lại email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            user.sendEmailVerification()
                .addOnCompleteListener { t ->
                    if (t.isSuccessful) {
                        Toast.makeText(this, "Đã gửi lại email xác thực", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, t.exception?.localizedMessage ?: "Không thể gửi email", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}


