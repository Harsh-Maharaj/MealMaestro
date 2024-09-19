package com.example.mealmaestro.Chats

// Data class representing a chat message between two users
data class Message(
    val message: String = "", // The actual message content, defaults to an empty string
    val sender: String = "", // The UID (User ID) of the message sender, defaults to an empty string
    val receiverUid: String? = null, // The UID (User ID) of the message receiver, nullable, defaults to null
    val timestamp: Long = System.currentTimeMillis() // The time when the message was created, defaults to the current system time
)
