package com.example.mealmaestro

import ShoppingListAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mealmaestro.Ingredient  // Make sure to import the Ingredient class if it's in a different file
import com.example.mealmaestro.databinding.FragmentShoppingListBinding

class ShoppingListFragment : Fragment() {

    private var _binding: FragmentShoppingListBinding? = null
    private val binding get() = _binding!!
    private lateinit var shoppingListAdapter: ShoppingListAdapter
    private val ingredientsList = mutableListOf<Ingredient>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        shoppingListAdapter = ShoppingListAdapter(ingredientsList)
        binding.recyclerView.apply {
            adapter = shoppingListAdapter
            layoutManager = LinearLayoutManager(context)
        }
        loadIngredients()
    }

    private fun loadIngredients() {
        ingredientsList.add(Ingredient("Tomatoes", false))
        ingredientsList.add(Ingredient("Chicken", false))
        ingredientsList.add(Ingredient("Rice", false))
        shoppingListAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
