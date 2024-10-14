package com.example.mealmaestro

data class AiResponse(
    val id: String,        // Add id field
    val userId: String = "",
    val question: String = "",
    val response: String = ""
)
