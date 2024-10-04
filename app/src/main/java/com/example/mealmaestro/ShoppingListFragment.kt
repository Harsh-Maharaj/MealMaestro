package com.example.mealmaestro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class ShoppingListFragment : Fragment() {
    private lateinit var shoppingListAdapter: ShoppingListAdapter
    private val shoppingList = mutableListOf<ShoppingListItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)

        // Setup RecyclerView
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view_shopping_list)
        shoppingListAdapter = ShoppingListAdapter(shoppingList) { item, isChecked ->
            updateShoppingListItemState(item, isChecked)
        }
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = shoppingListAdapter

        // Add item to shopping list
        val addItemButton: FloatingActionButton = view.findViewById(R.id.fab_add_item)
        val addItemInput: TextInputEditText = view.findViewById(R.id.input_add_item)

        addItemButton.setOnClickListener {
            val itemName = addItemInput.text.toString()
            if (itemName.isNotBlank()) {
                addItemToShoppingList(itemName)
                addItemInput.text?.clear()
            }
        }

        loadShoppingList()

        return view
    }

    private fun loadShoppingList() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val shoppingListRef = db.collection("shoppingLists").document(userId).collection("items")

        shoppingListRef.get().addOnSuccessListener { documents ->
            val items = documents.map { doc ->
                ShoppingListItem(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    checked = doc.getBoolean("checked") ?: false
                )
            }
            shoppingListAdapter.updateShoppingList(items)
        }.addOnFailureListener { e ->
            Log.e("ShoppingList", "Failed to load shopping list: ${e.message}")
        }
    }

    private fun addItemToShoppingList(itemName: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val shoppingListRef = db.collection("shoppingLists").document(userId).collection("items")

        val newItem = ShoppingListItem(name = itemName, checked = false)
        shoppingListRef.add(newItem.toMap()).addOnSuccessListener { docRef ->
            newItem.id = docRef.id
            shoppingList.add(newItem)
            shoppingListAdapter.notifyItemInserted(shoppingList.size - 1)
            Log.d("ShoppingList", "Item added: $itemName")
        }.addOnFailureListener { e ->
            Log.e("ShoppingList", "Failed to add item: ${e.message}")
        }
    }

    private fun updateShoppingListItemState(item: ShoppingListItem, isChecked: Boolean) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()
        val itemRef = db.collection("shoppingLists").document(userId).collection("items").document(item.id)

        itemRef.update("checked", isChecked).addOnSuccessListener {
            item.checked = isChecked
            Log.d("ShoppingList", "Item state updated: ${item.name}, checked: $isChecked")
        }.addOnFailureListener { e ->
            Log.e("ShoppingList", "Failed to update item state: ${e.message}")
        }
    }
}
