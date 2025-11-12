package com.uef.coloring_app.ui.common

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.uef.coloring_app.core.utils.LanguageManager

/**
 * Base Activity để xử lý locale cho tất cả activities
 * Đảm bảo ngôn ngữ được áp dụng đúng cho mọi activity
 */
abstract class BaseActivity : AppCompatActivity() {
    
    /**
     * Override attachBaseContext để áp dụng locale đã được lưu
     */
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase?.let { LanguageManager.getLocalizedContext(it) })
    }
}


