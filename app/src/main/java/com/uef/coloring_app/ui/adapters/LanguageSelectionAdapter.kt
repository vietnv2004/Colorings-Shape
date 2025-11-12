package com.uef.coloring_app.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.core.utils.LanguageManager

class LanguageSelectionAdapter(
    private val languages: List<LanguageManager.SupportedLanguage>,
    private val currentLanguageCode: String,
    private val onLanguageSelected: (LanguageManager.SupportedLanguage) -> Unit
) : RecyclerView.Adapter<LanguageSelectionAdapter.LanguageViewHolder>() {

    private var selectedPosition = languages.indexOfFirst { it.code == currentLanguageCode }

    class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flagImageView: ImageView = itemView.findViewById(R.id.flagImageView)
        val languageNameTextView: TextView = itemView.findViewById(R.id.languageNameTextView)
        val languageNativeTextView: TextView = itemView.findViewById(R.id.languageNativeTextView)
        val checkImageView: ImageView = itemView.findViewById(R.id.checkImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_language_selection, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languages[position]
        
        // Set flag
        val flagResource = getFlagResource(language.code)
        holder.flagImageView.setImageResource(flagResource)
        
        // Set language names
        holder.languageNameTextView.text = language.displayName
        holder.languageNativeTextView.text = getNativeLanguageName(language.code)
        
        // Show/hide check mark
        holder.checkImageView.visibility = if (position == selectedPosition) View.VISIBLE else View.GONE
        
        // Set click listener
        holder.itemView.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                val previousPosition = selectedPosition
                selectedPosition = currentPosition
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                onLanguageSelected(languages[currentPosition])
            }
        }
    }

    override fun getItemCount(): Int = languages.size

    private fun getFlagResource(languageCode: String): Int {
        return when (languageCode) {
            "vi" -> R.drawable.vietnam
            "en" -> R.drawable.united_kingdom
            "zh" -> R.drawable.vietnam // Sử dụng cờ Việt Nam tạm thời cho Trung Quốc
            "ja" -> R.drawable.japan
            "ko" -> R.drawable.korea
            "th" -> R.drawable.thailand
            "fr" -> R.drawable.france
            "de" -> R.drawable.germany
            "es" -> R.drawable.vietnam // Sử dụng cờ Việt Nam tạm thời cho Tây Ban Nha
            "ar" -> R.drawable.saudi_arabia
            "ru" -> R.drawable.russia
            "it" -> R.drawable.italy
            else -> R.drawable.vietnam
        }
    }

    private fun getNativeLanguageName(languageCode: String): String {
        return when (languageCode) {
            "vi" -> "Tiếng Việt"
            "en" -> "English"
            "zh" -> "中文"
            "ja" -> "日本語"
            "ko" -> "한국어"
            "th" -> "ไทย"
            "fr" -> "Français"
            "de" -> "Deutsch"
            "es" -> "Español"
            "ar" -> "العربية"
            "ru" -> "Русский"
            "it" -> "Italiano"
            else -> "Tiếng Việt"
        }
    }
}
