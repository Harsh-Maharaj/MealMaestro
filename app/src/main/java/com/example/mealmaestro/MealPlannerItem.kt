package com.example.mealmaestro

data class MealPlannerItem(
    var id: String = "",       // Document ID
    var time: Long = 0L,        // Scheduled time in milliseconds
    var mealType: String = "",  // Type of meal (e.g., Breakfast, Lunch)
    var title: String = "",     // Title of the meal
    var caption: String = ""    // Caption or description of the meal
)
