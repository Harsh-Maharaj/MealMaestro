package com.example.mealmaestro

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AiResponseAdapter(private val responses: List<AiResponse>) :
    RecyclerView.Adapter<AiResponseAdapter.AiResponseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AiResponseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ai_response, parent, false)
        return AiResponseViewHolder(view)
    }

    override fun onBindViewHolder(holder: AiResponseViewHolder, position: Int) {
        val response = responses[position]
        holder.bind(response)
    }

    override fun getItemCount(): Int = responses.size

    class AiResponseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        private val responseTextView: TextView = itemView.findViewById(R.id.responseTextView)

        fun bind(response: AiResponse) {
            questionTextView.text = response.question
            responseTextView.text = response.response
        }
    }
}
