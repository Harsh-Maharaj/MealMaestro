package com.example.mealmaestro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment

class FavouritesFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        // Header buttons
        val btnBack: ImageButton = view.findViewById(R.id.btn_back)
        val btnSearch: ImageButton = view.findViewById(R.id.btn_search)

        // Recipe item buttons (for the first two recipe items in the XML)
        val btnViewRecipe1: Button = view.findViewById(R.id.btn_view_recipe_1)
        val btnViewRecipe2: Button = view.findViewById(R.id.btn_view_recipe_2)

        // Bottom navigation buttons
        val btnNavHome: ImageButton = view.findViewById(R.id.btn_nav_home)
        val btnNavAdd: ImageButton = view.findViewById(R.id.btn_nav_add)
        val btnNavFavorites: ImageButton = view.findViewById(R.id.btn_nav_favorites)
        val btnNavLeaf: ImageButton = view.findViewById(R.id.btn_nav_leaf)

        // Set click listeners for header buttons
        btnBack.setOnClickListener {
            // Handle back button click
            activity?.onBackPressed()
        }

        btnSearch.setOnClickListener {
            // Handle search button click
            Toast.makeText(context, "Search clicked", Toast.LENGTH_SHORT).show()
        }

        // Set click listeners for recipe item buttons
        btnViewRecipe1.setOnClickListener {
            // Handle "View Recipe" button click for the first recipe item
            Toast.makeText(context, "View Recipe 1 clicked", Toast.LENGTH_SHORT).show()
        }

        btnViewRecipe2.setOnClickListener {
            // Handle "View Recipe" button click for the second recipe item
            Toast.makeText(context, "View Recipe 2 clicked", Toast.LENGTH_SHORT).show()
        }

        // Set click listeners for bottom navigation buttons
        btnNavHome.setOnClickListener {
            // Handle Home navigation button click
            Toast.makeText(context, "Home clicked", Toast.LENGTH_SHORT).show()
        }

        btnNavAdd.setOnClickListener {
            // Handle Add navigation button click
            Toast.makeText(context, "Add clicked", Toast.LENGTH_SHORT).show()
        }

        btnNavFavorites.setOnClickListener {
            // Handle Favorites navigation button click
            Toast.makeText(context, "Favorites clicked", Toast.LENGTH_SHORT).show()
        }

        btnNavLeaf.setOnClickListener {
            // Handle Leaf navigation button click
            Toast.makeText(context, "Leaf clicked", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavouritesFragment()
    }
}
