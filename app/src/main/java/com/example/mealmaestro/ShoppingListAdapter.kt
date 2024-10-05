package com.example.mealmaestro

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class ShoppingListitem(
    var id: String = "",
    var name: String = "", // Default value added
    var checked: Boolean = false // Default value added
) {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "checked" to checked
        )
    }
}

class ShoppingListAdapter(
    private val shoppingList: MutableList<ShoppingListItem>,
    private val onCheckedChange: (ShoppingListItem, Boolean) -> Unit
) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingListViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping_list, parent, false)
        return ShoppingListViewHolder(view)
    }

    override fun onBindViewHolder(holder: ShoppingListViewHolder, position: Int) {
        holder.bind(shoppingList[position])
    }

    override fun getItemCount(): Int = shoppingList.size

    fun updateShoppingList(newItems: List<ShoppingListItem>) {
        shoppingList.clear()
        shoppingList.addAll(newItems)
        notifyDataSetChanged()
    }

    inner class ShoppingListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val itemName: TextView = itemView.findViewById(R.id.item_name)
        private val itemCheckBox: CheckBox = itemView.findViewById(R.id.item_checkbox)

        fun bind(item: ShoppingListItem) {
            itemName.text = item.name
            itemCheckBox.isChecked = item.checked

            // Apply strikethrough if item is checked
            updateTextStrikethrough(itemName, item.checked)

            // Toggle the state when the card is clicked
            itemView.setOnClickListener {
                val isChecked = !itemCheckBox.isChecked
                itemCheckBox.isChecked = isChecked
                updateTextStrikethrough(itemName, isChecked)
                onCheckedChange(item, isChecked)
            }

            // Toggle the state when the checkbox is clicked
            itemCheckBox.setOnCheckedChangeListener { _, isChecked ->
                updateTextStrikethrough(itemName, isChecked)
                onCheckedChange(item, isChecked)
            }
        }

        // Function to add or remove strikethrough from the text
        private fun updateTextStrikethrough(textView: TextView, isChecked: Boolean) {
            if (isChecked) {
                textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }
}
