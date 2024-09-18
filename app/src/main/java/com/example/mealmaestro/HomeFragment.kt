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

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post> = mutableListOf()
    private var savedPosts: MutableList<String> = mutableListOf()
    private var postListener: ListenerRegistration? = null
    private var commentListeners: MutableMap<String, ListenerRegistration> = mutableMapOf()

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

        postAdapter = PostAdapter(requireContext(), postList) { post: Post ->
            Log.d("HomeFragment", "Post unsaved: ${post.postId}")
            fetchSavedPosts {
                fetchPosts()  // Refresh the UI after unsaving
            }
        }

        recyclerView.adapter = postAdapter
        fetchSavedPosts { fetchPosts() }

        val isPostCreated = arguments?.getBoolean("postCreated", false) ?: false
        if (isPostCreated) {
            Toast.makeText(requireContext(), "Post successfully created!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        fetchSavedPosts { fetchPosts() }
    }

    override fun onPause() {
        super.onPause()
        removeListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeListeners()
    }

    private fun removeListeners() {
        postListener?.remove()
        commentListeners.values.forEach { it.remove() }
        commentListeners.clear()
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
            .whereLessThanOrEqualTo("caption", query + "\uf8ff")

        searchQuery.get().addOnSuccessListener { snapshots ->
            postList.clear()
            for (document in snapshots.documents) {
                val post = document.toObject(Post::class.java)
                post?.let {
                    it.postId = document.id
                    it.isSaved = savedPosts.contains(it.postId)
                    postList.add(it)
                }
            }
            postAdapter.notifyDataSetChanged()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to search posts", Toast.LENGTH_SHORT).show()
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
                snapshot.documents.forEach { document ->
                    savedPosts.add(document.id)
                }
                callback()
            }
            .addOnFailureListener { callback() }
    }

    private fun fetchPosts() {
        removeListeners()

        postListener = FirebaseFirestore.getInstance()
            .collection("posts")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                postList.clear()

                snapshots.documents.forEach { document ->
                    val post = document.toObject(Post::class.java)
                    post?.let {
                        it.postId = document.id
                        it.isSaved = savedPosts.contains(it.postId)
                        postList.add(it)
                        fetchCommentsForPost(it)
                    }
                }
                postAdapter.notifyDataSetChanged()
            }
    }

    private fun fetchCommentsForPost(post: Post) {
        val commentListener = FirebaseFirestore.getInstance()
            .collection("posts")
            .document(post.postId)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                post.comments.clear()
                snapshot.documents.forEach { document ->
                    val comment = document.toObject(Comment::class.java)
                    comment?.let { post.comments.add(it) }
                }
                postAdapter.notifyItemChanged(postList.indexOf(post))
            }
        commentListeners[post.postId] = commentListener
    }
}
