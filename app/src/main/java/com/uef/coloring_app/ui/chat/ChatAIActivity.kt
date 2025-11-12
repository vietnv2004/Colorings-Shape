package com.uef.coloring_app.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.uef.coloring_app.R
import com.uef.coloring_app.core.ai.ChatGPTService
import com.uef.coloring_app.core.ai.AppContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.uef.coloring_app.ui.common.BaseActivity
import kotlinx.coroutines.withContext

class ChatAIActivity : BaseActivity() {
    
    private lateinit var chatContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var sendButton: MaterialButton
    private lateinit var loadingContainer: LinearLayout
    private lateinit var chatScrollView: NestedScrollView
    
    private val chatGPTService = ChatGPTService()
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_ai)
        
        initViews()
        setupToolbar()
        setupClickListeners()
        showWelcomeMessage()
    }
    
    private fun initViews() {
        chatContainer = findViewById(R.id.chatContainer)
        messageInput = findViewById(R.id.messageInput)
        sendButton = findViewById(R.id.sendButton)
        loadingContainer = findViewById(R.id.loadingContainer)
        chatScrollView = findViewById(R.id.chatScrollView)
    }
    
    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        
        // Sử dụng setSupportActionBar với theme NoActionBar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
    
    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            sendMessage()
        }
        
        messageInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }
    
    private fun showWelcomeMessage() {
        val welcomeMessage = "✨🎯 Chào bạn! Tôi ở đây để hỗ trợ bạn trải nghiệm ứng dụng tô màu hình khối thông minh. Hãy cho tôi biết bạn cần hỗ trợ gì nhé!"
        addAIMessage(welcomeMessage)
    }
    
    private fun sendMessage() {
        val message = messageInput.text.toString().trim()
        if (message.isEmpty()) return
        
        // Add user message to chat
        addUserMessage(message)
        messageInput.text.clear()
        
        // Show loading
        showLoading(true)
        
        // Send message to AI
        coroutineScope.launch {
            try {
                val response = chatGPTService.sendMessage(message)
                response.onSuccess { aiResponse ->
                    // Sử dụng phản hồi từ API online
                    addAIMessage(aiResponse)
                }.onFailure { error ->
                    // Chỉ sử dụng fallback khi thực sự có lỗi API
                    val fallbackMessage = if (error is Exception && chatGPTService.isQuotaExceeded(error)) {
                        "🤖 Hiện tại tôi đang hoạt động ở chế độ offline do hạn mức API đã hết. Tôi vẫn có thể giúp bạn với thông tin về ứng dụng Coloring Shapes!\n\n" + chatGPTService.getFallbackResponse(message)
                    } else {
                        "🤖 Có lỗi kết nối với AI. Tôi đang hoạt động ở chế độ offline. Tôi vẫn có thể giúp bạn với thông tin về ứng dụng Coloring Shapes!\n\n" + chatGPTService.getFallbackResponse(message)
                    }
                    addAIMessage(fallbackMessage)
                }
            } catch (e: Exception) {
                // Chỉ sử dụng fallback khi có exception thực sự
                val fallbackMessage = if (chatGPTService.isQuotaExceeded(e)) {
                    "🤖 Hiện tại tôi đang hoạt động ở chế độ offline do hạn mức API đã hết. Tôi vẫn có thể giúp bạn với thông tin về ứng dụng Coloring Shapes!\n\n" + chatGPTService.getFallbackResponse(message)
                } else {
                    "🤖 Có lỗi kết nối với AI. Tôi đang hoạt động ở chế độ offline. Tôi vẫn có thể giúp bạn với thông tin về ứng dụng Coloring Shapes!\n\n" + chatGPTService.getFallbackResponse(message)
                }
                addAIMessage(fallbackMessage)
            } finally {
                showLoading(false)
            }
        }
    }
    
    private fun addUserMessage(message: String) {
        val messageView = createMessageView(true)
        val messageText = messageView.findViewById<TextView>(R.id.userMessageText)
        messageText.text = message
        
        chatContainer.addView(messageView)
        scrollToBottom()
    }
    
    private fun addAIMessage(message: String) {
        val messageView = createMessageView(false)
        val messageText = messageView.findViewById<TextView>(R.id.aiMessageText)
        messageText.text = message
        
        chatContainer.addView(messageView)
        scrollToBottom()
    }
    
    private fun createMessageView(isUserMessage: Boolean): View {
        val inflater = LayoutInflater.from(this)
        val messageView = inflater.inflate(R.layout.item_chat_message, chatContainer, false)
        
        val userContainer = messageView.findViewById<LinearLayout>(R.id.userMessageContainer)
        val aiContainer = messageView.findViewById<LinearLayout>(R.id.aiMessageContainer)
        
        if (isUserMessage) {
            userContainer.visibility = View.VISIBLE
            aiContainer.visibility = View.GONE
        } else {
            userContainer.visibility = View.GONE
            aiContainer.visibility = View.VISIBLE
        }
        
        return messageView
    }
    
    private fun showLoading(show: Boolean) {
        loadingContainer.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun scrollToBottom() {
        chatScrollView.post {
            chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }
    
    
}
