package com.example.mealmaestro

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.mealmaestro.Helper.Post


class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post> = mutableListOf()  // Use the correct Post class
    private var savedPosts: MutableList<String> = mutableListOf() // Store saved post IDs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Initialize the adapter with the unsave callback
        postAdapter = PostAdapter(requireContext(), postList) { post: Post ->
            // Unsave callback handling
            Log.d("HomeFragment", "Post unsaved: ${post.postId}")
            fetchSavedPosts {
                fetchPosts() // Refresh the UI after unsaving
            }
        }

        recyclerView.adapter = postAdapter

        // Fetch saved posts and update the UI
        fetchSavedPosts {
            fetchPosts()
        }
    }

    private fun fetchSavedPosts(callback: () -> Unit) {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance().collection("favorites")
            .document(currentUserId)
            .collection("posts")
            .get()
            .addOnSuccessListener { snapshot ->
                savedPosts.clear()
                if (snapshot != null) {
                    for (document in snapshot.documents) {
                        savedPosts.add(document.id)
                    }
                }
                callback()
            }
            .addOnFailureListener {
                callback()
            }
    }

    private fun fetchPosts() {
        FirebaseFirestore.getInstance().collection("posts")
            .get()
            .addOnSuccessListener { snapshots ->
                if (snapshots != null) {
                    postList.clear() // Clear the current postList to refresh the data

                    for (document in snapshots.documents) {
                        val post = document.toObject(Post::class.java)
                        post?.let {
                            it.isSaved = savedPosts.contains(it.postId)
                            postList.add(it)
                        }
                    }
                    postAdapter.notifyDataSetChanged() // Notify adapter to update the UI
                }
            }
    }

    override fun onResume() {
        super.onResume()
        // Ensure that posts are always refreshed when coming back to this fragment
        fetchSavedPosts {
            fetchPosts()
        }
    }
}
