package com.example.mealmaestro.Chats

data class Message(
    val message: String = "",
    val sender: String = "",
    val receiverUid: String? = null,
    val timestamp: Long = System.currentTimeMillis() // Optional: you can include a timestamp
)