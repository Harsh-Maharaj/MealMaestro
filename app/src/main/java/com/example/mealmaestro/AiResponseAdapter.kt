package com.example.mealmaestro

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AiResponseAdapter(
    private val responses: MutableList<AiResponse>,
    private val context: Context
) : RecyclerView.Adapter<AiResponseAdapter.AiResponseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AiResponseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_response, parent, false)
        return AiResponseViewHolder(view)
    }

    override fun onBindViewHolder(holder: AiResponseViewHolder, position: Int) {
        val response = responses[position]
        holder.bind(response)

        // Set long click listener to delete response
        holder.itemView.setOnLongClickListener {
            showDeleteConfirmationDialog(response, position)
            true
        }
    }

    override fun getItemCount(): Int = responses.size

    fun showDeleteConfirmationDialog(response: AiResponse, position: Int) {
        AlertDialog.Builder(context).apply {
            setTitle("Delete Response")
            setMessage("Are you sure you want to delete this response?")
            setPositiveButton("Yes") { _, _ ->
                deleteResponseFromFirestore(response, position)
            }
            setNegativeButton("No", null)
        }.show()
    }

    private fun deleteResponseFromFirestore(response: AiResponse, position: Int) {
        val db = Firebase.firestore

        db.collection("ai_responses")
            .whereEqualTo("userId", response.userId)
            .whereEqualTo("question", response.question)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val document = documents.first()

                    db.collection("ai_responses").document(document.id).delete()
                        .addOnSuccessListener {
                            Log.d("AiResponseAdapter", "Successfully deleted response")
                            responses.removeAt(position)
                            notifyItemRemoved(position)
                        }
                        .addOnFailureListener { e ->
                            Log.e("AiResponseAdapter", "Failed to delete response", e)
                        }
                } else {
                    Log.e("AiResponseAdapter", "No matching document found")
                }
            }
            .addOnFailureListener { e ->
                Log.e("AiResponseAdapter", "Failed to query response", e)
            }
    }

    class AiResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        private val responseTextView: TextView = itemView.findViewById(R.id.responseTextView)

        fun bind(response: AiResponse) {
            questionTextView.text = response.question
            responseTextView.text = response.response
        }
    }
}
