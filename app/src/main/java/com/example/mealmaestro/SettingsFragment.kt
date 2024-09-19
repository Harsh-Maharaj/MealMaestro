package com.example.mealmaestro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"  // Key for the first parameter
private const val ARG_PARAM2 = "param2"  // Key for the second parameter

/**
 * A simple [Fragment] subclass.
 * Use the [SettingsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SettingsFragment : Fragment() {
    // Parameters passed to the fragment during initialization
    private var param1: String? = null  // First parameter
    private var param2: String? = null  // Second parameter

    // Called when the fragment is created; initialize parameters if provided
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Retrieve the parameters from the arguments
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    // Called to create and return the view hierarchy associated with the fragment
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and return the view
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    // Factory method to create a new instance of SettingsFragment with parameters
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SettingsFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SettingsFragment().apply {
                // Create a bundle with the parameters and attach to the fragment
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)  // Put the first parameter in the bundle
                    putString(ARG_PARAM2, param2)  // Put the second parameter in the bundle
                }
            }
    }
}