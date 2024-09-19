package com.example.mealmaestro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mealmaestro.databinding.ActivityRecycleUserFriendsBinding

class FriendsFragment : Fragment() {

    // View binding to access views in the layout
    private var _binding: ActivityRecycleUserFriendsBinding? = null
    private val binding get() = _binding!! // Non-nullable access to the binding

    // Inflates the layout for the fragment and initializes view binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using view binding
        _binding = ActivityRecycleUserFriendsBinding.inflate(inflater, container, false)
        return binding.root // Return the root view of the binding
    }

    // Cleans up the view binding when the view is destroyed to avoid memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Set the binding to null when the view is destroyed
    }
}