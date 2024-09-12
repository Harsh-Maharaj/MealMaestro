package com.example.mealmaestro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.widget.TextView
import android.widget.LinearLayout


class FavouritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var noFavouritesTextView: TextView
    private lateinit var headerAndListContainer: LinearLayout  // Add a reference to the header and list container
    private var favouriteList: MutableList<Post> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        // Initialize RecyclerView and Header-List Container
        recyclerView = view.findViewById(R.id.recycler_view_favourites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(requireContext(), favouriteList)
        recyclerView.adapter = postAdapter

        // Initialize No Favorites TextView and header-list container
        noFavouritesTextView = view.findViewById(R.id.tv_no_favourites)
        headerAndListContainer = view.findViewById(R.id.header_and_list_container)

        // Fetch favourite recipes
        fetchFavourites()

        // Handle header and bottom navigation button clicks
        setupButtons(view)

        return view
    }

    private fun fetchFavourites() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("favorites")
            .document(currentUserId)
            .collection("posts")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    favouriteList.clear()
                    for (document in snapshots.documents) {
                        val post = document.toObject(Post::class.java)
                        if (post != null) {
                            favouriteList.add(post)
                        }
                    }

                    postAdapter.notifyDataSetChanged()

                    // Show or hide the RecyclerView, header, and "No favourites saved" message
                    if (favouriteList.isEmpty()) {
                        headerAndListContainer.visibility = View.GONE  // Hide header and RecyclerView
                        noFavouritesTextView.visibility = View.VISIBLE  // Show 'No favourites saved'
                    } else {
                        headerAndListContainer.visibility = View.VISIBLE  // Show header and RecyclerView
                        noFavouritesTextView.visibility = View.GONE  // Hide 'No favourites saved'
                    }
                }
            }
    }

    private fun setupButtons(view: View) {
        // Header buttons
        val btnBack: ImageButton = view.findViewById(R.id.btn_back)
        val btnSearch: ImageButton = view.findViewById(R.id.btn_search)

        // Bottom navigation buttons
        val btnNavHome: ImageButton = view.findViewById(R.id.btn_nav_home)
        val btnNavAdd: ImageButton = view.findViewById(R.id.btn_nav_add)
        val btnNavFavorites: ImageButton = view.findViewById(R.id.btn_nav_favourites)
        val btnNavLeaf: ImageButton = view.findViewById(R.id.btn_nav_leaf)

        // Set click listeners for header buttons
        btnBack.setOnClickListener {
            activity?.onBackPressed()
        }

        btnSearch.setOnClickListener {
            Toast.makeText(context, "Search clicked", Toast.LENGTH_SHORT).show()
        }

        // Set click listeners for bottom navigation buttons
        btnNavHome.setOnClickListener {
            Toast.makeText(context, "Home clicked", Toast.LENGTH_SHORT).show()
        }

        btnNavAdd.setOnClickListener {
            Toast.makeText(context, "Add clicked", Toast.LENGTH_SHORT).show()
        }

        btnNavFavorites.setOnClickListener {
            Toast.makeText(context, "Favorites clicked", Toast.LENGTH_SHORT).show()
        }

        btnNavLeaf.setOnClickListener {
            Toast.makeText(context, "Leaf clicked", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavouritesFragment()
    }
}
