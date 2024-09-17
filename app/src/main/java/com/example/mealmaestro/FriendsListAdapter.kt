package com.example.mealmaestro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.R

class FriendsListAdapter(
    private val friendsList: List<String>,
    private val onFriendSelected: (String) -> Unit
) : RecyclerView.Adapter<FriendsListAdapter.FriendsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friendId = friendsList[position]
        holder.bind(friendId)
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val friendNameTextView: TextView = itemView.findViewById(R.id.text_view_friend_name)

        fun bind(friendId: String) {
            // Set the friend name (assuming friendId can be replaced with actual names if needed)
            friendNameTextView.text = friendId
            itemView.setOnClickListener {
                onFriendSelected(friendId)
            }
        }
    }
}
