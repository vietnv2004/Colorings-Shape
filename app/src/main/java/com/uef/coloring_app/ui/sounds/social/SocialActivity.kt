package com.uef.coloring_app.ui.sounds.social

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.uef.coloring_app.R
import com.uef.coloring_app.ui.common.BaseActivity

class SocialActivity : BaseActivity() {
    
    private lateinit var friendsCountTextView: TextView
    private lateinit var postsCountTextView: TextView
    private lateinit var likesCountTextView: TextView
    private lateinit var socialFeedRecyclerView: RecyclerView
    private lateinit var addFriendButton: Button
    private lateinit var shareButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_social)
        
        // Hide action bar to avoid duplicate title
        supportActionBar?.hide()
        
        initViews()
        setupClickListeners()
        loadSocialData()
    }
    
    private fun initViews() {
        friendsCountTextView = findViewById(R.id.friendsCountTextView)
        postsCountTextView = findViewById(R.id.postsCountTextView)
        likesCountTextView = findViewById(R.id.likesCountTextView)
        socialFeedRecyclerView = findViewById(R.id.socialFeedRecyclerView)
        addFriendButton = findViewById(R.id.addFriendButton)
        shareButton = findViewById(R.id.shareButton)
    }
    
    private fun setupClickListeners() {
        addFriendButton.setOnClickListener {
            // TODO: Show add friend dialog
        }
        
        shareButton.setOnClickListener {
            // TODO: Show share options
        }
    }
    
    private fun loadSocialData() {
        // Demo data
        friendsCountTextView.text = "12"
        postsCountTextView.text = "45"
        likesCountTextView.text = "234"
        
        val socialPosts = getSampleSocialPosts()
        
        socialFeedRecyclerView.layoutManager = LinearLayoutManager(this)
        socialFeedRecyclerView.adapter = SocialPostAdapter(socialPosts)
    }
    
    private fun getSampleSocialPosts(): List<SocialPost> {
        return listOf(
            SocialPost(
                userName = "Alice Johnson",
                userAvatar = "👩",
                postContent = getString(R.string.just_completed_first_task),
                postImage = "social_image_1",
                likes = 12,
                comments = 3,
                timeAgo = getString(R.string.hours_ago, 2)
            ),
            SocialPost(
                userName = "Bob Smith",
                userAvatar = "👨",
                postContent = getString(R.string.check_out_masterpiece),
                postImage = "social_image_2",
                likes = 28,
                comments = 7,
                timeAgo = getString(R.string.hours_ago, 5)
            ),
            SocialPost(
                userName = "Carol Davis",
                userAvatar = "👩",
                postContent = getString(R.string.achieved_100_accuracy),
                postImage = "social_image_3",
                likes = 15,
                comments = 5,
                timeAgo = getString(R.string.day_ago)
            ),
            SocialPost(
                userName = "David Wilson",
                userAvatar = "👨",
                postContent = getString(R.string.post_created_colorful_garden),
                postImage = "social_image_4",
                likes = 22,
                comments = 8,
                timeAgo = getString(R.string.days_ago, 1)
            ),
            SocialPost(
                userName = "Emma Brown",
                userAvatar = "👩",
                postContent = getString(R.string.post_beautiful_mountain_lake),
                postImage = "social_image_5",
                likes = 35,
                comments = 12,
                timeAgo = getString(R.string.days_ago, 2)
            ),
            SocialPost(
                userName = "Frank Miller",
                userAvatar = "👨",
                postContent = getString(R.string.post_city_at_night),
                postImage = "social_image_6",
                likes = 18,
                comments = 6,
                timeAgo = getString(R.string.days_ago, 3)
            ),
            SocialPost(
                userName = "Grace Lee",
                userAvatar = "👩",
                postContent = getString(R.string.post_fairytale_castle),
                postImage = "social_image_2",
                likes = 41,
                comments = 15,
                timeAgo = getString(R.string.days_ago, 4)
            ),
            SocialPost(
                userName = "Henry Chen",
                userAvatar = "👨",
                postContent = getString(R.string.post_tropical_beach),
                postImage = "social_image_3",
                likes = 29,
                comments = 9,
                timeAgo = getString(R.string.days_ago, 5)
            )
        )
    }
}

data class SocialPost(
    val userName: String,
    val userAvatar: String,
    val postContent: String,
    val postImage: String,
    val likes: Int,
    val comments: Int,
    val timeAgo: String
)
