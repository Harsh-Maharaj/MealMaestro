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
import com.example.mealmaestro.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

// This class handles Firebase Cloud Messaging (FCM) notifications for the app
class FCMNotificationService : FirebaseMessagingService() {
    val channelId = "chat_message"
    override fun onCreate() {
        super.onCreate()
        // Ensure notification channel is created on service start
        createNotificationChannel()
    }

    // Called when a new FCM message is received
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            // Extract custom data fields from the notification
            val friendName = remoteMessage.data["friendName"]
            val friendUid = remoteMessage.data["friendUid"]
            val friendIcon = remoteMessage.data["friendIcon"]
            // Send the notification with extracted data
            sendNotification(friendName, friendUid, friendIcon)
        }
    }

    // Function to build and send the notification to the user
    @SuppressLint("MissingPermission")
    private fun sendNotification(friendName: String?, friendUid: String?, friendIcon: String?) {
        val intent = Intent(this, ChatFriendsActivity::class.java).apply {
            putExtra("username", friendName)
            putExtra("uid", friendUid)
            putExtra("icon", friendIcon)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification using NotificationCompat
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
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

    // Called when the FCM registration token is updated
    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
        // Send token to your server, if needed
    }

    // Function to create a notification channel
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Channel"
            val descriptionText = "Channel for default notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Function to send the actual Firebase Cloud Messaging (FCM) notification
    fun sendFCMNotification(
        friendFcmToken: String,
        friendName: String,
        friendUid: String,
        friendIcon: String,
        message: String
    ) {
        // Build the JSON payload for the FCM notification
        val jsonObject = JSONObject().apply {
            // Create the notification object that holds the title and body of the notification
            val notificationObject = JSONObject().apply {
                put("title", "New message from $friendName") // Notification title
                put("body", message) // Notification body containing the message content
            }
            // Create the data object that holds additional information about the friend (sent as data payload)
            val dataObject = JSONObject().apply {
                put("friendName", friendName) // Friend's username
                put("friendUid", friendUid) // Friend's UID
                put("friendIcon", friendIcon) // Friend's profile icon URL
            }
            // Attach the notification and data to the FCM payload
            put("notification", notificationObject)
            put("data", dataObject)
            put("to", friendFcmToken) // Send the notification to the friend's FCM token
        }

        // Create an OkHttp client to send the HTTP request to FCM
        val client = OkHttpClient()

        // Define the media type for the request as JSON
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody =
            jsonObject.toString().toRequestBody(mediaType) // Convert JSON to request body

        // Build the HTTP request to send to the FCM server
        val request = Request.Builder()
            .url("https://fcm.googleapis.com/fcm/send") // FCM send URL
            .post(requestBody) // Attach the request body (notification payload)
            .addHeader(
                "Authorization",
                "key=AIzaSyDB24uVr8v76ti0Cd5x-nWPUfrP4OlnHPo"
            )  // Firebase server key
            .addHeader("Content-Type", "application/json") // Content type header
            .build()

        // Send the HTTP request asynchronously using OkHttp
        client.newCall(request).enqueue(object : Callback {
            // Handle failure to send the notification
            override fun onFailure(call: Call, e: IOException) {
                Log.e("FCMNotification", "Failed to send FCM notification", e)
            }

            // Handle the response from the FCM server
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        // If the response is not successful, log the error message
                        Log.e(
                            "FCMNotification",
                            "FCM Notification Response Failed: ${response.body?.string()}"
                        )
                    } else {
                        // Log the successful response for debugging
                        Log.i(
                            "FCMNotification",
                            "FCM Notification Response: ${response.body?.string()}"
                        )
                    }
                }
            }
        })
    }
}
