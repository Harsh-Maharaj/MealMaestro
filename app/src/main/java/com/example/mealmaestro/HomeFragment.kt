package com.example.mealmaestro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.Comment
import com.example.mealmaestro.Helper.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class HomeFragment : Fragment() {

    // UI components
    private lateinit var recyclerView: RecyclerView // RecyclerView for displaying posts
    private lateinit var postAdapter: PostAdapter // Adapter for managing posts in RecyclerView
    private var postList: MutableList<Post> = mutableListOf() // List to hold posts
    private var savedPosts: MutableList<String> = mutableListOf() // List to track saved posts
    private var postListener: ListenerRegistration? = null // Listener for post updates
    private var commentListeners: MutableMap<String, ListenerRegistration> = mutableMapOf() // Map to store listeners for comments on posts

    // Inflate the fragment's layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    // Initialize RecyclerView and load posts when the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view) // Find RecyclerView in the layout
        recyclerView.layoutManager = LinearLayoutManager(requireContext()) // Set layout manager

        // Initialize the post adapter with a callback to unsave posts
        postAdapter = PostAdapter(requireContext(), postList) { post: Post ->
            Log.d("HomeFragment", "Post unsaved: ${post.postId}")
            fetchSavedPosts {
                fetchPosts()  // Refresh the UI after unsaving
            }
        }

        recyclerView.adapter = postAdapter // Set the adapter for the RecyclerView
        fetchSavedPosts { fetchPosts() } // Fetch saved posts and then load all posts

        // Check if a post was just created and show a toast message
        val isPostCreated = arguments?.getBoolean("postCreated", false) ?: false
        if (isPostCreated) {
            Toast.makeText(requireContext(), "Post successfully created!", Toast.LENGTH_SHORT).show()
        }
    }

    // Reload posts when the fragment resumes
    override fun onResume() {
        super.onResume()
        fetchSavedPosts { fetchPosts() }
    }

    // Clean up listeners when the fragment is paused
    override fun onPause() {
        super.onPause()
        removeListeners() // Remove listeners to prevent memory leaks
    }

    // Clean up listeners when the fragment is destroyed
    override fun onDestroy() {
        super.onDestroy()
        removeListeners() // Ensure no active listeners remain
    }

    // Remove all listeners for posts and comments
    private fun removeListeners() {
        postListener?.remove() // Remove post listener
        commentListeners.values.forEach { it.remove() } // Remove all comment listeners
        commentListeners.clear() // Clear the map of comment listeners
    }

    // The function to handle searching posts by caption
    fun performSearch(query: String) {
        if (query.isEmpty()) {
            fetchPosts() // If the search query is empty, fetch all posts
            return
        }

        // Firestore query to search for captions that contain the query
        val searchQuery = FirebaseFirestore.getInstance()
            .collection("posts")
            .whereGreaterThanOrEqualTo("caption", query)
            .whereLessThanOrEqualTo("caption", query + "\uf8ff") // Range query for matching captions

        searchQuery.get().addOnSuccessListener { snapshots ->
            // Clear the existing post list
            postList.clear()
            for (document in snapshots.documents) {
                val post = document.toObject(Post::class.java)
                post?.let {
                    it.postId = document.id // Set post ID from Firestore document ID
                    it.isSaved = savedPosts.contains(it.postId) // Check if the post is saved
                    postList.add(it) // Add post to the list
                }
            }
            postAdapter.notifyDataSetChanged() // Notify adapter of changes
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to search posts", Toast.LENGTH_SHORT).show() // Show error message if search fails
        }
    }

    // Fetch saved posts from Firebase
    private fun fetchSavedPosts(callback: () -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return // Get current user ID
        FirebaseFirestore.getInstance().collection("favorites")
            .document(currentUserId)
            .collection("posts")
            .get()
            .addOnSuccessListener { snapshot ->
                savedPosts.clear() // Clear the saved posts list
                snapshot.documents.forEach { document ->
                    savedPosts.add(document.id) // Add post IDs to the saved posts list
                }
                callback() // Invoke the callback after fetching saved posts
            }
            .addOnFailureListener { callback() }
    }

    // Fetch all posts from Firebase Firestore
    private fun fetchPosts() {
        removeListeners() // Remove any existing listeners before adding new ones

        // Listen for real-time updates to posts
        postListener = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("created_at", Query.Direction.DESCENDING) // Order posts by creation time
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener // Return if there is an error or no data
                postList.clear() // Clear the post list

                // Loop through all posts and add them to the list
                snapshots.documents.forEach { document ->
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        it.postId = document.id // Set post ID
                        it.isSaved = savedPosts.contains(it.postId) // Check if the post is saved
                        postList.add(it) // Add the post to the list
                        fetchCommentsForPost(it) // Fetch comments for the post
                    }
                }
                postAdapter.notifyDataSetChanged() // Notify the adapter that data has changed
            }
    }

    // Fetch comments for a specific post
    private fun fetchCommentsForPost(post: Post) {
        val commentListener = FirebaseFirestore.getInstance()
            .collection("posts")
            .document(post.postId)
            .collection("comments")
            .orderBy("timestamp")  // Order comments by timestamp
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener  // Return if there is an error or no data
                post.comments.clear()  // Clear the comments list for the post
                snapshot.documents.forEach { document ->
                    val comment = document.toObject(Comment::class.java)
                    comment?.let { post.comments.add(it) }  // Add each comment to the post's comments list
                }
                postAdapter.notifyItemChanged(postList.indexOf(post))  // Notify adapter that the post's data has changed
            }
        commentListeners[post.postId] = commentListener  // Store the comment listener in the map
    }
}
