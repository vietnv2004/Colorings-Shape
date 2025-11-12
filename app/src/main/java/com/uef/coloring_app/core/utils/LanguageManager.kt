package com.uef.coloring_app.core.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

/**
 * Language Manager để quản lý đa ngôn ngữ
 * Hỗ trợ chuyển đổi ngôn ngữ dễ dàng trong tương lai
 */
object LanguageManager {
    
    // Các ngôn ngữ được hỗ trợ
    enum class SupportedLanguage(val code: String, val displayName: String) {
        VIETNAMESE("vi", "Tiếng Việt"),
        ENGLISH("en", "English"),
        CHINESE("zh", "中文"),
        JAPANESE("ja", "日本語"),
        KOREAN("ko", "한국어"),
        THAI("th", "ไทย"),
        FRENCH("fr", "Français"),
        GERMAN("de", "Deutsch"),
        SPANISH("es", "Español"),
        ARABIC("ar", "العربية"),
        RUSSIAN("ru", "Русский")
    }
    
    private const val LANGUAGE_PREF_KEY = "selected_language"
    private const val DEFAULT_LANGUAGE = "vi" // Tiếng Việt mặc định
    
    /**
     * Lấy ngôn ngữ hiện tại từ SharedPreferences
     */
    fun getCurrentLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("ColoringAppPrefs", Context.MODE_PRIVATE)
        return prefs.getString(LANGUAGE_PREF_KEY, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }
    
    /**
     * Lưu ngôn ngữ được chọn vào SharedPreferences
     */
    fun setLanguage(context: Context, languageCode: String) {
        val prefs = context.getSharedPreferences("ColoringAppPrefs", Context.MODE_PRIVATE)
        prefs.edit().putString(LANGUAGE_PREF_KEY, languageCode).apply()
    }
    
    /**
     * Áp dụng ngôn ngữ cho Context
     */
    fun applyLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
    }
    
    /**
     * Tạo Context mới với locale đã được cấu hình
     * Sử dụng trong attachBaseContext() của activities
     */
    fun getLocalizedContext(context: Context): Context {
        val currentLanguage = getCurrentLanguage(context)
        val locale = Locale(currentLanguage)
        
        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            return context
        }
    }
    
    /**
     * Lấy danh sách tất cả ngôn ngữ được hỗ trợ
     */
    fun getSupportedLanguages(): List<SupportedLanguage> {
        return SupportedLanguage.values().toList()
    }
    
    /**
     * Lấy SupportedLanguage từ language code
     */
    fun getSupportedLanguage(code: String): SupportedLanguage? {
        return SupportedLanguage.values().find { it.code == code }
    }
    
    /**
     * Lấy tên hiển thị của ngôn ngữ từ code
     */
    fun getLanguageDisplayName(code: String): String {
        return getSupportedLanguage(code)?.displayName ?: code
    }
    
    /**
     * Kiểm tra xem ngôn ngữ có được hỗ trợ không
     */
    fun isLanguageSupported(code: String): Boolean {
        return SupportedLanguage.values().any { it.code == code }
    }
    
    /**
     * Lấy ngôn ngữ mặc định của hệ thống
     */
    fun getSystemLanguage(): String {
        return Locale.getDefault().language
    }
    
    /**
     * Khởi tạo ngôn ngữ khi app khởi động
     */
    fun initializeLanguage(context: Context) {
        val currentLanguage = getCurrentLanguage(context)
        applyLanguage(context, currentLanguage)
    }
    
    /**
     * Thay đổi ngôn ngữ và áp dụng ngay lập tức
     */
    fun changeLanguage(context: Context, languageCode: String) {
        if (isLanguageSupported(languageCode)) {
            setLanguage(context, languageCode)
            applyLanguage(context, languageCode)
        }
    }
    
    /**
     * Lấy danh sách tên ngôn ngữ để hiển thị trong UI
     */
    fun getLanguageDisplayNames(): List<String> {
        return SupportedLanguage.values().map { it.displayName }
    }
    
    /**
     * Lấy danh sách mã ngôn ngữ
     */
    fun getLanguageCodes(): List<String> {
        return SupportedLanguage.values().map { it.code }
    }
    
    /**
     * Lấy danh sách ngôn ngữ được hỗ trợ cho UI (Tất cả ngôn ngữ)
     */
    fun getSupportedLanguagesForUI(): List<SupportedLanguage> {
        return listOf(
            SupportedLanguage.VIETNAMESE, 
            SupportedLanguage.ENGLISH, 
            SupportedLanguage.CHINESE,
            SupportedLanguage.JAPANESE,
            SupportedLanguage.KOREAN,
            SupportedLanguage.THAI,
            SupportedLanguage.FRENCH,
            SupportedLanguage.GERMAN,
            SupportedLanguage.SPANISH,
            SupportedLanguage.ARABIC,
            SupportedLanguage.RUSSIAN
        )
    }
    
    /**
     * Lấy danh sách tên ngôn ngữ cho UI
     */
    fun getLanguageDisplayNamesForUI(): List<String> {
        return getSupportedLanguagesForUI().map { it.displayName }
    }
    
    /**
     * Lấy danh sách mã ngôn ngữ cho UI
     */
    fun getLanguageCodesForUI(): List<String> {
        return getSupportedLanguagesForUI().map { it.code }
    }
}
