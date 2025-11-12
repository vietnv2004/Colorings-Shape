package com.uef.coloring_app.core.ai

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ChatGPTService {
    
    companion object {
        private const val TAG = "ChatGPTService"
        private const val BASE_URL = "https://openrouter.ai/api/v1"
        private const val API_URL = "$BASE_URL/chat/completions"
        private const val API_KEY = "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"
        private const val MODEL = "deepseek/deepseek-v3.1-terminus:exacto"
        private const val SITE_URL = "https://coloring-shapes-app.com"
        private const val SITE_NAME = "Coloring Shapes App"
    }
    
    suspend fun sendMessage(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL(API_URL)
            val connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Authorization", "Bearer $API_KEY")
            connection.setRequestProperty("HTTP-Referer", SITE_URL)
            connection.setRequestProperty("X-Title", SITE_NAME)
            connection.doOutput = true
            
            // Thiết lập timeout để tránh kết nối bị treo
            connection.connectTimeout = 30000 // 30 giây
            connection.readTimeout = 30000 // 30 giây
            
            val systemPrompt = AppContext.getSystemPrompt()
            
            val requestBody = JSONObject().apply {
                put("model", MODEL)
                put("max_tokens", 500)
                put("temperature", 0.7)
                put("messages", arrayOf(
                    JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    },
                    JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    }
                ))
                // Extra body theo OpenRouter API
                put("extra_body", JSONObject())
            }
            
            val outputStream = connection.outputStream
            val writer = OutputStreamWriter(outputStream)
            writer.write(requestBody.toString())
            writer.flush()
            writer.close()
            
            val responseCode = connection.responseCode
            Log.d(TAG, "Response code: $responseCode")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val inputStream = connection.inputStream
                val reader = BufferedReader(InputStreamReader(inputStream))
                val response = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                
                val jsonResponse = JSONObject(response.toString())
                val choices = jsonResponse.getJSONArray("choices")
                val message = choices.getJSONObject(0).getJSONObject("message")
                val content = message.getString("content")
                
                Log.d(TAG, "AI Response: $content")
                Result.success(content)
            } else {
                val errorStream = connection.errorStream
                val reader = BufferedReader(InputStreamReader(errorStream))
                val errorResponse = StringBuilder()
                var line: String?
                
                while (reader.readLine().also { line = it } != null) {
                    errorResponse.append(line)
                }
                reader.close()
                
                Log.e(TAG, "Error response: $errorResponse")
                
                // Chỉ trả về lỗi khi thực sự có lỗi API
                Result.failure(Exception("API_ERROR: $responseCode - $errorResponse"))
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message to ChatGPT", e)
            Result.failure(e)
        }
    }
    
    suspend fun getGreetingMessage(): String = withContext(Dispatchers.IO) {
        try {
            val greetingResult = sendMessage("Xin chào! Hãy chào mừng người dùng đến với ứng dụng Coloring Shapes.")
            greetingResult.getOrElse { 
                AppContext.GREETING_MESSAGES_DETAILED.random() 
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting greeting message", e)
            AppContext.GREETING_MESSAGES_DETAILED.random()
        }
    }
    
    suspend fun getHelpResponse(topic: String): String = withContext(Dispatchers.IO) {
        try {
            val helpMessage = "Người dùng hỏi về: $topic. Hãy cung cấp thông tin chi tiết và hữu ích."
            val helpResult = sendMessage(helpMessage)
            helpResult.getOrElse { 
                AppContext.HELP_TOPICS_DETAILED[topic] ?: "Xin lỗi, tôi chưa hiểu rõ câu hỏi của bạn. Bạn có thể hỏi về tính năng, hướng dẫn, cấp độ, điểm số, ngôn ngữ, hoặc liên hệ."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting help response", e)
            AppContext.HELP_TOPICS_DETAILED[topic] ?: "Xin lỗi, có lỗi xảy ra. Vui lòng thử lại sau."
        }
    }
    
    fun getFallbackResponse(userMessage: String): String {
        val message = userMessage.lowercase()
        
        return when {
            message.contains("xin chào") || message.contains("hello") -> {
                "🎨✨ Xin chào! Tôi là AI Assistant của ứng dụng Coloring Shapes. Hiện tại tôi đang hoạt động ở chế độ offline. Tôi có thể giúp bạn về các tính năng, hướng dẫn sử dụng, cấp độ, điểm số, thành tích, và thông tin liên hệ của ứng dụng! 🤖🎨"
            }
            message.contains("tính năng") -> {
                AppContext.HELP_TOPICS_DETAILED["tính năng"] ?: "🎨 Ứng dụng có các tính năng chính: tô màu hình khối, hệ thống cấp độ, theo dõi tiến trình, bảng xếp hạng, thành tích, đa ngôn ngữ, và nhiều chủ đề giao diện."
            }
            message.contains("hướng dẫn") -> {
                AppContext.HELP_TOPICS_DETAILED["hướng dẫn"] ?: "📖 Để sử dụng ứng dụng: 1) Chọn hình khối muốn tô màu 2) Chọn màu từ bảng màu 3) Tô màu bằng cách chạm vào các vùng 4) Hoàn thành và xem điểm số"
            }
            message.contains("cấp độ") -> {
                AppContext.HELP_TOPICS_DETAILED["cấp độ"] ?: "🎯 Ứng dụng có 3 cấp độ: Dễ (hình đơn giản), Trung bình (hình phức tạp), Khó (hình chi tiết cao). Bạn có thể chọn cấp độ phù hợp với khả năng của mình."
            }
            message.contains("điểm số") -> {
                AppContext.HELP_TOPICS_DETAILED["điểm số"] ?: "🏆 Điểm số được tính dựa trên: độ chính xác tô màu, thời gian hoàn thành, và mức độ khó của hình. Điểm cao sẽ giúp bạn leo lên bảng xếp hạng."
            }
            message.contains("liên hệ") || message.contains("contact") -> {
                AppContext.HELP_TOPICS_DETAILED["liên hệ"] ?: "📞 Bạn có thể liên hệ với chúng tôi qua: Điện thoại +84 28 5422 6666, Email info@uef.edu.vn, hoặc đến trực tiếp tại 145 Điện Biên Phủ, Bình Thạnh, TP.HCM."
            }
            message.contains("thành tích") -> {
                AppContext.HELP_TOPICS_DETAILED["thành tích"] ?: "🏅 Hệ thống thành tích bao gồm: Họa sĩ mới, Họa sĩ chuyên nghiệp, Bậc thầy tô màu, và nhiều thành tích đặc biệt khác."
            }
            message.contains("ngôn ngữ") -> {
                AppContext.HELP_TOPICS_DETAILED["ngôn ngữ"] ?: "🌍 Ứng dụng hỗ trợ 12 ngôn ngữ: Tiếng Việt, English, 中文, 日本語, 한국어, ไทย, Français, Deutsch, Español, العربية, Русский, Italiano."
            }
            message.contains("bảng xếp hạng") -> {
                AppContext.HELP_TOPICS_DETAILED["bảng xếp hạng"] ?: "🏆 Bảng xếp hạng bao gồm: xếp hạng theo điểm số, số hình hoàn thành, thời gian trung bình, và cấp độ."
            }
            message.contains("tùy chỉnh") || message.contains("cài đặt") -> {
                AppContext.HELP_TOPICS_DETAILED["tùy chỉnh"] ?: "⚙️ Bạn có thể tùy chỉnh: giao diện, âm thanh, rung động, game play, và thông báo trong phần Cài đặt."
            }
            message.contains("lỗi") || message.contains("bug") -> {
                AppContext.HELP_TOPICS_DETAILED["troubleshooting"] ?: "🔧 Nếu gặp lỗi, bạn có thể: khởi động lại ứng dụng, kiểm tra kết nối mạng, hoặc liên hệ hỗ trợ kỹ thuật."
            }
            else -> {
                "🤖 Tôi hiểu bạn đang hỏi về: '$userMessage'. Hiện tại tôi đang hoạt động ở chế độ offline. Bạn có thể hỏi tôi về:\n\n• Tính năng của ứng dụng\n• Hướng dẫn sử dụng\n• Cấp độ và điểm số\n• Thành tích và bảng xếp hạng\n• Thông tin liên hệ\n• Tùy chỉnh và cài đặt\n• Khắc phục sự cố\n\nHãy thử hỏi một trong những chủ đề trên nhé! 😊"
            }
        }
    }
    
    fun isQuotaExceeded(exception: Exception): Boolean {
        return exception.message?.contains("API_QUOTA_EXCEEDED") == true ||
               exception.message?.contains("insufficient_quota") == true ||
               exception.message?.contains("quota") == true
    }
}
