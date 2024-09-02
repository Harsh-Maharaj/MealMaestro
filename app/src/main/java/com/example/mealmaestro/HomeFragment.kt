package com.example.mealmaestro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.Post
import com.example.mealmaestro.Helper.DataBase
import com.google.firebase.firestore.FirebaseFirestore

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(requireContext(), postList)
        recyclerView.adapter = postAdapter

        fetchPosts()
    }

    private fun fetchPosts() {
        FirebaseFirestore.getInstance().collection("posts")
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    postList.clear()
                    for (document in snapshots.documents) {
                        val post = document.toObject(Post::class.java)
                        if (post != null) {
                            postList.add(post)
                        }
                    }
                    postAdapter.notifyDataSetChanged()
                }
            }
    }


}
