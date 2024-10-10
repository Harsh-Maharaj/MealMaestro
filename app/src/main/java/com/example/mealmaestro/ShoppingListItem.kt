package com.example.mealmaestro

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShoppingListItem(
    var id: String = "", // Ensure the id has a default value
    var name: String = "", // Add a default value for name
    var checked: Boolean = false // Set checked to false by default
) : Parcelable {
    // Function to convert to a map (useful for Firestore)
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "checked" to checked
        )
    }
}
