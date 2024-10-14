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
import android.widget.TextView
import android.widget.LinearLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Source
import com.example.mealmaestro.Helper.Post

class FavouritesFragment : Fragment() {

    // UI components
    private lateinit var recyclerView: RecyclerView // RecyclerView to display favorite posts
    private lateinit var postAdapter: PostAdapter // Adapter for managing posts in the RecyclerView
    private lateinit var noFavouritesTextView: TextView // TextView to display when no favorites are available
    private lateinit var headerAndListContainer: LinearLayout  // Header and list container
    private var favouriteList: MutableList<Post> = mutableListOf() // List to hold favorite posts

    // Variables for pagination
    private var lastVisible: DocumentSnapshot? = null  // For pagination
    private var isLoading = false  // Prevent multiple queries at the same time

    // Inflate the fragment's layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        // Initialize RecyclerView and Header-List Container
        recyclerView = view.findViewById(R.id.recycler_view_favourites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up the post adapter and handle unsaving a post
        postAdapter = PostAdapter(requireContext(), favouriteList) { post: Post ->
            // Remove the post from Firebase and the list when unsaved
            unsavePostFromFirebase(post)
        }

        recyclerView.adapter = postAdapter // Assign adapter to RecyclerView

        // Initialize No Favorites TextView and header-list container
        noFavouritesTextView = view.findViewById(R.id.tv_no_favourites)
        headerAndListContainer = view.findViewById(R.id.header_and_list_container)

        // Fetch favourite recipes with real-time updates
        fetchFavourites()

        // Handle header and bottom navigation button clicks
        setupButtons(view)

        // Add scroll listener for pagination
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                // Load more items when the user reaches the bottom of the list
                if (!isLoading && layoutManager.findLastCompletelyVisibleItemPosition() == favouriteList.size - 1) {
                    fetchFavourites()  // Load more items when user reaches the bottom of the list
                }
            }
        })

        return view
    }

    // Fetch favorite posts from Firebase
    private fun fetchFavourites() {
        if (isLoading) return // Return if already loading data
        isLoading = true // Set loading flag to true

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return // Get current user ID
        val firestore = FirebaseFirestore.getInstance() // Reference to Firestore

        // Build query: If `lastVisible` is null, it's the first query; otherwise, paginate after the last item
        val query = if (lastVisible == null) {
            firestore.collection("favorites")
                .document(currentUserId)
                .collection("posts")
                .limit(10) // Limit results to 10 posts
                .get(Source.SERVER)
        } else {
            firestore.collection("favorites")
                .document(currentUserId)
                .collection("posts")
                .startAfter(lastVisible!!) // Start after the last visible post for pagination
                .limit(10)
                .get(Source.SERVER)
        }

        // Fetch results from the query
        query.addOnSuccessListener { snapshots ->
            if (!snapshots.isEmpty) {
                // Loop through documents and add posts to the list
                for (document in snapshots.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        post.isSaved = true // Mark post as saved
                        if (!favouriteList.contains(post)) {
                            favouriteList.add(post) // Add post to the list
                            postAdapter.notifyItemInserted(favouriteList.size - 1) // Notify the adapter
                        }
                    }
                }
                lastVisible = snapshots.documents[snapshots.size() - 1] // Update `lastVisible` for pagination
            }

            // Show or hide the "No Favourites" message based on whether the list is empty
            if (favouriteList.isEmpty()) {
                headerAndListContainer.visibility = View.GONE
                noFavouritesTextView.visibility = View.VISIBLE
            } else {
                headerAndListContainer.visibility = View.VISIBLE
                noFavouritesTextView.visibility = View.GONE
            }
            isLoading = false // Reset loading flag
        }.addOnFailureListener {
            isLoading = false // Reset loading flag if the query fails
        }
    }


    // Method to unsave the post from Firebase
    private fun unsavePostFromFirebase(post: Post) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return // Get current user ID
        val firestore = FirebaseFirestore.getInstance() // Reference to Firestore

        // Delete the post from the user's favorites in Firebase
        firestore.collection("favorites")
            .document(currentUserId)
            .collection("posts")
            .document(post.postId)  // Use post.postId instead of post.id
            .delete()
            .addOnSuccessListener {
                // Show success message
                Toast.makeText(context, "Post unsaved successfully", Toast.LENGTH_SHORT).show()

                // Remove the post from the local list as well
                favouriteList.remove(post)
                postAdapter.notifyDataSetChanged()

                // Optionally refresh the list or UI
                if (favouriteList.isEmpty()) {
                    headerAndListContainer.visibility = View.GONE
                    noFavouritesTextView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener { e ->
                // Show failure message
                Toast.makeText(context, "Failed to unsave post", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
    }

    // Set up button click listeners for the header and bottom navigation buttons
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
            activity?.onBackPressed() // Go back to the previous activity/fragment
        }

        btnSearch.setOnClickListener {
            Toast.makeText(context, "Search clicked", Toast.LENGTH_SHORT).show() // Show a message for search click
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

    // Static method to create a new instance of the fragment
    companion object {
        @JvmStatic
        fun newInstance() = FavouritesFragment()
    }
}
