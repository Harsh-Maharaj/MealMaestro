package com.example.mealmaestro

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MealPlannerAdapter(
    private val mealList: MutableList<MealPlannerItem>,
    private val editMealTime: (MealPlannerItem) -> Unit,
    private val deleteMeal: (MealPlannerItem) -> Unit
) : RecyclerView.Adapter<MealPlannerAdapter.MealViewHolder>() {

    // ViewHolder class to bind meal items to the RecyclerView
    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealTypeTextView: TextView = itemView.findViewById(R.id.mealTypeTextView)
        val mealTimeTextView: TextView = itemView.findViewById(R.id.mealTimeTextView)
        val mealTitleTextView: TextView = itemView.findViewById(R.id.mealTitleTextView)
        val mealCaptionTextView: TextView = itemView.findViewById(R.id.mealCaptionTextView)

        fun bind(meal: MealPlannerItem) {
            mealTypeTextView.text = meal.mealType
            mealTimeTextView.text = "Time: ${formatTime(meal.time)}"
            mealTitleTextView.text = meal.title
            mealCaptionTextView.text = meal.caption

            itemView.setOnLongClickListener {
                showOptionsDialog(meal)
                true
            }
        }

        private fun formatTime(timeInMillis: Long): String {
            val formatter = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
            return formatter.format(timeInMillis)
        }

        private fun showOptionsDialog(meal: MealPlannerItem) {
            AlertDialog.Builder(itemView.context)
                .setTitle("Options")
                .setItems(arrayOf("Edit Time", "Delete")) { _, which ->
                    when (which) {
                        0 -> editMealTime(meal)
                        1 -> deleteMeal(meal)
                    }
                }
                .show()
        }
    }


    // Inflate the item layout for each meal item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_planner, parent, false)
        return MealViewHolder(view)
    }

    // Bind the ViewHolder to the specific position in the meal list
    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        holder.bind(mealList[position])
    }

    // Get the size of the meal list
    override fun getItemCount(): Int = mealList.size
}
