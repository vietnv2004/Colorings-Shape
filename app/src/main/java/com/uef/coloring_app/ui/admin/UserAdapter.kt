package com.uef.coloring_app.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.data.local.entity.UserEntity
import java.text.SimpleDateFormat
import java.util.*

class UserAdapter(
    private val onEditClick: (UserEntity) -> Unit,
    private val onDeleteClick: (UserEntity) -> Unit,
    private val onToggleActiveClick: (UserEntity) -> Unit
) : ListAdapter<UserEntity, UserAdapter.UserViewHolder>(UserDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_user, parent, false)
        return UserViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val emailTextView: TextView = itemView.findViewById(R.id.emailTextView)
        private val roleTextView: TextView = itemView.findViewById(R.id.roleTextView)
        private val birthYearTextView: TextView = itemView.findViewById(R.id.birthYearTextView)
        private val genderTextView: TextView = itemView.findViewById(R.id.genderTextView)
        private val createdAtTextView: TextView = itemView.findViewById(R.id.createdAtTextView)
        private val activeSwitch: Switch = itemView.findViewById(R.id.activeSwitch)
        private val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        
        fun bind(user: UserEntity) {
            val context = itemView.context
            nameTextView.text = user.name
            emailTextView.text = user.email
            roleTextView.text = if (user.role == "admin") context.getString(R.string.admin_role) else context.getString(R.string.user_role_label)
            birthYearTextView.text = context.getString(R.string.birth_year_label, user.birthYear)
            genderTextView.text = user.gender
            
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            createdAtTextView.text = context.getString(R.string.created_at_label, dateFormat.format(Date(user.createdAt)))
            
            activeSwitch.isChecked = user.isActive
            activeSwitch.setOnCheckedChangeListener { _, _ ->
                onToggleActiveClick(user)
            }
            
            editButton.setOnClickListener {
                onEditClick(user)
            }
            
            deleteButton.setOnClickListener {
                onDeleteClick(user)
            }
            
            // Change background color based on status
            itemView.alpha = if (user.isActive) 1.0f else 0.6f
        }
    }
    
    class UserDiffCallback : DiffUtil.ItemCallback<UserEntity>() {
        override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
            return oldItem == newItem
        }
    }
}


