package com.example.mealmaestro

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MealPlannerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MealPlannerAdapter
    private val mealPlannerList = mutableListOf<MealPlannerItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meal_planner_activity)

        recyclerView = findViewById(R.id.mealPlannerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MealPlannerAdapter(mealPlannerList, ::editMealTime, ::deleteMeal)
        recyclerView.adapter = adapter

        loadMealPlannerData()
    }

    private fun loadMealPlannerData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val mealPlannerRef = FirebaseFirestore.getInstance()
            .collection("mealPlanner").document(userId)

        val mealTypes = listOf("Breakfast", "Lunch", "Dinner")

        mealPlannerList.clear() // Clear list to avoid duplicates

        // Loop through each meal type to fetch its data
        for (mealType in mealTypes) {
            mealPlannerRef.collection(mealType).get()
                .addOnSuccessListener { snapshot ->
                    for (meal in snapshot.documents) {
                        val item = meal.toObject(MealPlannerItem::class.java)
                        item?.let {
                            it.id = meal.id  // Set the Firestore document ID
                            it.mealType = mealType // Assign meal type
                            mealPlannerList.add(it)
                        }
                    }
                    adapter.notifyDataSetChanged() // Refresh UI
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to load $mealType meals.", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun editMealTime(meal: MealPlannerItem) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_time, null)
        val editTextTime = dialogView.findViewById<EditText>(R.id.editMealTime)

        AlertDialog.Builder(this)
            .setTitle("Edit Meal Time")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newTime = editTextTime.text.toString().toLongOrNull()
                if (newTime != null) {
                    updateMealTimeInFirestore(meal, newTime)
                } else {
                    Toast.makeText(this, "Invalid time.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateMealTimeInFirestore(meal: MealPlannerItem, newTime: Long) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Use the correct collection path for meal type
        val mealRef = FirebaseFirestore.getInstance()
            .collection("mealPlanner")
            .document(userId)
            .collection(meal.mealType)  // Use meal type as the collection
            .document(meal.id)  // Use the document ID

        mealRef.update("time", newTime)
            .addOnSuccessListener {
                meal.time = newTime // Update local data
                adapter.notifyDataSetChanged() // Refresh UI
                Toast.makeText(this, "Meal time updated.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update meal time.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteMeal(meal: MealPlannerItem) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Use the correct collection path for meal type
        val mealRef = FirebaseFirestore.getInstance()
            .collection("mealPlanner")
            .document(userId)
            .collection(meal.mealType)  // Use meal type as the collection
            .document(meal.id)  // Use the document ID

        mealRef.delete()
            .addOnSuccessListener {
                mealPlannerList.remove(meal) // Remove from local list
                adapter.notifyDataSetChanged() // Refresh UI
                Toast.makeText(this, "Meal removed.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to remove meal.", Toast.LENGTH_SHORT).show()
            }
    }
}
