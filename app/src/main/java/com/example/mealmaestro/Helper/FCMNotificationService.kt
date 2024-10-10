package com.example.mealmaestro.Helper

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
import com.google.auth.oauth2.GoogleCredentials
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class FCMNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.notification?.let {
            // Extract the necessary data from the message
            val senderName = remoteMessage.data["senderName"]
            val friendUid = remoteMessage.data["friendUid"]
            val message = remoteMessage.data["message"]

            // Show the notification when the message is received
            showNotification(senderName, message, friendUid, this)
        }
    }

    // Function to send the actual FCM notification using OAuth 2.0 token
    fun sendFCMNotification(friendFcmToken: String, senderName: String, friendUid: String, message: String, context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val jsonObject = JSONObject().apply {
                val messageObject = JSONObject().apply {
                    val notificationObject = JSONObject().apply {
                        put("title", "New message from $senderName")
                        put("body", message)
                    }
                    val dataObject = JSONObject().apply {
                        put("senderName", senderName)
                        put("friendUid", friendUid)
                        put("message", message)
                    }
                    put("token", friendFcmToken)
                    put("notification", notificationObject)
                    put("data", dataObject)
                }
                put("message", messageObject)
            }

            val client = OkHttpClient()

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = jsonObject.toString().toRequestBody(mediaType)

            // Get OAuth 2.0 Access Token
            val accessToken = getAccessToken(context)

            val request = Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/mealmaestro-46c0d/messages:send")
                .post(requestBody)
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .build()

            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    showNotification(senderName, message, friendUid, context)
                    Log.e("FCMNotification", "FCM Notification Response Failed: ${response.body?.string()}")
                } else {
                    Log.i("FCMNotification", "FCM Notification Sent Successfully: ${response.body?.string()}")
                }
            } catch (e: IOException) {
                Log.e("FCMNotification", "Failed to send FCM notification", e)
            }
        }
    }

    // Show notification when a message is received
    fun showNotification(senderName: String?, message: String?, friendUid: String?, context: Context) {
        val channelId = "chat_message_channel"
        val channelName = "Chat Messages"

        val intent = Intent(context, ChatFriendsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("uid", friendUid)
            putExtra("name", senderName)
        }

        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH).apply {
                description = "Channel for chat message notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.meal_maestro_logo)
            .setContentTitle("New message from $senderName")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    // Function to generate OAuth 2.0 Access Token
    fun getAccessToken(context: Context): String {
        val assetManager = context.assets
        val inputStream = assetManager.open("serviceAccountKey.json")
        val credentials = GoogleCredentials.fromStream(inputStream)
            .createScoped(listOf("https://www.googleapis.com/auth/cloud-platform"))
        credentials.refreshIfExpired()
        return credentials.accessToken.tokenValue
    }
}
