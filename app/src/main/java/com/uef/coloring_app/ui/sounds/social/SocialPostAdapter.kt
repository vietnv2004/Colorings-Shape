package com.uef.coloring_app.ui.sounds.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R

class SocialPostAdapter(private val posts: List<SocialPost>) : 
    RecyclerView.Adapter<SocialPostAdapter.PostViewHolder>() {
    
    class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatarTextView: TextView = itemView.findViewById(R.id.userAvatarTextView)
        val userNameTextView: TextView = itemView.findViewById(R.id.userNameTextView)
        val timeAgoTextView: TextView = itemView.findViewById(R.id.timeAgoTextView)
        val postContentTextView: TextView = itemView.findViewById(R.id.postContentTextView)
        val postImageView: ImageView = itemView.findViewById(R.id.postImageView)
        val likesButton: Button = itemView.findViewById(R.id.likesButton)
        val commentsButton: Button = itemView.findViewById(R.id.commentsButton)
        val shareButton: Button = itemView.findViewById(R.id.shareButton)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_social_post, parent, false)
        return PostViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        
        holder.userAvatarTextView.text = post.userAvatar
        holder.userNameTextView.text = post.userName
        holder.timeAgoTextView.text = post.timeAgo
        holder.postContentTextView.text = post.postContent
        
        // Set image based on post image resource
        val imageResource = when (post.postImage) {
            "social_image_1" -> R.drawable.social_image_1
            "social_image_2" -> R.drawable.social_image_2
            "social_image_3" -> R.drawable.social_image_3
            "social_image_4" -> R.drawable.social_image_4
            "social_image_5" -> R.drawable.social_image_5
            "social_image_6" -> R.drawable.social_image_6
            else -> R.drawable.social_image_1
        }
        holder.postImageView.setImageResource(imageResource)
        
        holder.likesButton.text = holder.itemView.context.getString(R.string.like_count, post.likes)
        holder.commentsButton.text = holder.itemView.context.getString(R.string.comment_count, post.comments)
        
        holder.likesButton.setOnClickListener {
            // TODO: Handle like action
        }
        
        holder.commentsButton.setOnClickListener {
            // TODO: Show comments
        }
        
        holder.shareButton.setOnClickListener {
            // TODO: Share post
        }
    }
    
    override fun getItemCount(): Int = posts.size
}
