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
import android.util.Log

class FavouritesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var noFavouritesTextView: TextView
    private lateinit var headerAndListContainer: LinearLayout  // Header and list container
    private var favouriteList: MutableList<Post> = mutableListOf()

    private var lastVisible: DocumentSnapshot? = null  // For pagination
    private var isLoading = false  // Prevent multiple queries at the same time

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favourites, container, false)

        // Initialize RecyclerView and Header-List Container
        recyclerView = view.findViewById(R.id.recycler_view_favourites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postAdapter = PostAdapter(requireContext(), favouriteList) { post: Post ->
            // Remove the post from Firebase and the list when unsaved
            unsavePostFromFirebase(post)
        }

        recyclerView.adapter = postAdapter

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
                if (!isLoading && layoutManager.findLastCompletelyVisibleItemPosition() == favouriteList.size - 1) {
                    fetchFavourites()  // Load more items when user reaches the bottom of the list
                }
            }
        })

        return view
    }

    private fun fetchFavourites() {
        if (isLoading) return
        isLoading = true

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        val query = if (lastVisible == null) {
            firestore.collection("favorites")
                .document(currentUserId)
                .collection("posts")
                .limit(10)
                .get(Source.SERVER)
        } else {
            firestore.collection("favorites")
                .document(currentUserId)
                .collection("posts")
                .startAfter(lastVisible!!)
                .limit(10)
                .get(Source.SERVER)
        }

        query.addOnSuccessListener { snapshots ->
            if (!snapshots.isEmpty) {
                for (document in snapshots.documents) {
                    val post = document.toObject(Post::class.java)
                    if (post != null) {
                        post.isSaved = true
                        if (!favouriteList.contains(post)) {
                            favouriteList.add(post)
                            postAdapter.notifyItemInserted(favouriteList.size - 1)
                        }
                    }
                }
                lastVisible = snapshots.documents[snapshots.size() - 1]
            }

            if (favouriteList.isEmpty()) {
                headerAndListContainer.visibility = View.GONE
                noFavouritesTextView.visibility = View.VISIBLE
            } else {
                headerAndListContainer.visibility = View.VISIBLE
                noFavouritesTextView.visibility = View.GONE
            }
            isLoading = false
        }.addOnFailureListener {
            isLoading = false
        }
    }


    // Method to unsave the post from Firebase
    private fun unsavePostFromFirebase(post: Post) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("favorites")
            .document(currentUserId)
            .collection("posts")
            .document(post.postId)  // Use post.postId instead of post.id
            .delete()
            .addOnSuccessListener {
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
                Toast.makeText(context, "Failed to unsave post", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
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
