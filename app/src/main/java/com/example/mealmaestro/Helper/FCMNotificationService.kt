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

// This class handles Firebase Cloud Messaging (FCM) notifications for the app
class FCMNotificationService : FirebaseMessagingService() {

    // Called when a new FCM message is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains a notification payload
        remoteMessage.notification?.let {
            // Extract custom data fields from the notification (friend's details)
            val friendName = remoteMessage.data["friendName"] // Friend's name
            val friendUid = remoteMessage.data["friendUid"]   // Friend's user ID
            val friendIcon = remoteMessage.data["friendIcon"] // Friend's icon

            // Send the notification using the extracted data
            sendNotification(friendName, friendUid, friendIcon)
        }
    }

    // Function to build and send the notification to the user
    @SuppressLint("MissingPermission")
    private fun sendNotification(friendName: String?, friendUid: String?, friendIcon: String?) {
        // Create an Intent to open the ChatFriendsActivity when the user taps the notification
        val intent = Intent(this, ChatFriendsActivity::class.java).apply {
            // Pass the friend's details to the activity
            putExtra("username", friendName)
            putExtra("uid", friendUid)
            putExtra("icon", friendIcon)
            // Set the flags to start a new task and clear the previous one
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // Create a PendingIntent to launch the ChatFriendsActivity when the user taps the notification
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification using NotificationCompat
        val notificationBuilder = NotificationCompat.Builder(this, "your_channel_id")
            .setSmallIcon(R.drawable.meal_maestro_logo) // Set the notification icon
            .setContentTitle("New message from $friendName") // Set the notification title
            .setContentText("Tap to open the chat") // Set the notification text
            .setPriority(NotificationCompat.PRIORITY_HIGH) // Set the priority for high importance
            .setContentIntent(pendingIntent) // Attach the PendingIntent to the notification
            .setAutoCancel(true) // Automatically cancel the notification when tapped

        // Use NotificationManagerCompat to show the notification
        with(NotificationManagerCompat.from(this)) {
            // Display the notification with an ID of 1
            notify(1, notificationBuilder.build())
        }
    }

    // Called when the FCM registration token is updated
    override fun onNewToken(token: String) {
        // Log the new FCM token to the console for debugging
        Log.d("FCM", "Refreshed token: $token")
        // Optionally send the token to your server or other back-end services for updating the token
    }

    // Function to create a notification channel (required for Android O and above)
    private fun createNotificationChannel() {
        // Only create the notification channel if the device is running Android O (API 26) or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Set the channel name and description
            val name = "Default Channel"
            val descriptionText = "Channel for default notifications"
            // Set the importance level for the channel
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            // Create the notification channel
            val channel = NotificationChannel("channel_id", name, importance).apply {
                description = descriptionText
            }
            // Get the NotificationManager service to create the channel
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            // Create the notification channel
            notificationManager.createNotificationChannel(channel)
        }
    }
}

