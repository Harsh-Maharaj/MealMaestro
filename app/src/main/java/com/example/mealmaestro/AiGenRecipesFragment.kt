package com.example.mealmaestro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AiGenRecipesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var aiResponseAdapter: AiResponseAdapter
    private val aiResponses = mutableListOf<AiResponse>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_aigen_recipes, container, false)
        recyclerView = view.findViewById(R.id.recyclerViewAiResponses)

        aiResponseAdapter = AiResponseAdapter(aiResponses)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = aiResponseAdapter

        fetchResponsesFromFirestore()

        return view
    }

    private fun fetchResponsesFromFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId != null) {
            val db = Firebase.firestore
            db.collection("ai_responses")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    aiResponses.clear()
                    for (document in documents) {
                        val response = document.toObject(AiResponse::class.java)
                        aiResponses.add(response)
                    }
                    aiResponseAdapter.notifyDataSetChanged()
                }
                .addOnFailureListener { e ->
                    Log.e("AiGenRecipesFragment", "Failed to fetch responses from Firestore", e)
                }
        } else {
            Log.e("AiGenRecipesFragment", "User not logged in. Cannot fetch responses.")
        }
    }
}
