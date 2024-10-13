package com.example.mealmaestro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.Post

class MealPlannerAdapter(private val mealPlannerList: List<Post>) :
    RecyclerView.Adapter<MealPlannerAdapter.MealViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_planner, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val post = mealPlannerList[position]
        holder.bind(post)
    }

    override fun getItemCount(): Int = mealPlannerList.size

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mealTypeTextView: TextView =
            itemView.findViewById(R.id.mealTypeTextView)
        private val mealTimeTextView: TextView =
            itemView.findViewById(R.id.mealTimeTextView)
        private val captionTextView: TextView =
            itemView.findViewById(R.id.captionTextView)

        fun bind(post: Post) {
            mealTypeTextView.text = post.mealType
            mealTimeTextView.text = "Time: ${formatTime(post.time)}"
            captionTextView.text = post.caption
        }

        private fun formatTime(timeInMillis: Long): String {
            val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            return formatter.format(timeInMillis)
        }
    }
}

