package com.uef.coloring_app.core.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.uef.coloring_app.MainActivity
import com.uef.coloring_app.R

/**
 * Service for handling notifications
 */
class NotificationService : Service() {
    
    companion object {
        const val CHANNEL_ID = "coloring_app_channel"
        const val CHANNEL_NAME = "Coloring App Notifications"
        const val NOTIFICATION_ID = 1
        
        /**
         * Show a notification
         */
        fun showNotification(context: Context, title: String, message: String) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Create notification channel for Android O and above
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for coloring tasks"
                }
                notificationManager.createNotificationChannel(channel)
            }
            
            // Create intent for notification tap
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            
            // Build notification
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()
            
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
        
        /**
         * Show time's up notification
         */
        fun showTimesUpNotification(context: Context) {
            showNotification(
                context,
                context.getString(R.string.time_up),
                context.getString(R.string.time_up_notification)
            )
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle notification service commands
        return START_NOT_STICKY
    }
}


