package com.example.mealmaestro.Helper

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mealmaestro.Chats.ChatFriendsActivity
import com.example.mealmaestro.MainActivity
import com.example.mealmaestro.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM messages here.
        remoteMessage.notification?.let {
            // Extract data from the notification
            val friendName = remoteMessage.data["friendName"]
            val friendUid = remoteMessage.data["friendUid"]
            val friendIcon = remoteMessage.data["friendIcon"]

            sendNotification(friendName, friendUid, friendIcon)
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendNotification(friendName: String?, friendUid: String?, friendIcon: String?) {
        val intent = Intent(this, ChatFriendsActivity::class.java).apply {
            // Pass the friend's data to the activity
            putExtra("username", friendName)
            putExtra("uid", friendUid)
            putExtra("icon", friendIcon)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, "your_channel_id")
            .setSmallIcon(R.drawable.meal_maestro_logo)
            .setContentTitle("New message from $friendName")
            .setContentText("Tap to open the chat")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(1, notificationBuilder.build())
        }
    }

    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        // Optionally send the token to your server or other back-end services
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Channel"
            val descriptionText = "Channel for default notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("channel_id", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
