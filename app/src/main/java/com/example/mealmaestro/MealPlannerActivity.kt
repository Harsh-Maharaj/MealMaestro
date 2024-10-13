package com.example.mealmaestro

import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MealPlannerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MealPlannerAdapter
    private val mealPlannerList = mutableListOf<Post>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.meal_planner_activity)

        recyclerView = findViewById(R.id.mealPlannerRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = MealPlannerAdapter(mealPlannerList)
        recyclerView.adapter = adapter

        loadMealPlannerData()
    }

    private fun loadMealPlannerData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val mealPlannerRef = FirebaseFirestore.getInstance()
            .collection("mealPlanner")
            .document(userId)
            .collection("meals")

        mealPlannerRef.orderBy("time", Query.Direction.ASCENDING).get()
            .addOnSuccessListener { snapshot ->
                mealPlannerList.clear()
                for (document in snapshot.documents) {
                    val post = document.toObject(Post::class.java)
                    post?.let { mealPlannerList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load meal planner data.", Toast.LENGTH_SHORT).show()
            }
    }
}
