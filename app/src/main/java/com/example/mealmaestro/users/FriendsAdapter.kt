package com.example.mealmaestro.users

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Chats.ChatFriendsActivity
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.R
import com.google.firebase.auth.FirebaseAuth

class FriendsAdapter(
    val context: Context,
    val friendsList: ArrayList<Users>,
 //   private val addOrRemoveFriend: AddOrRemoveFriend // interface to update friends view
) : RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.friend_id)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view: View =
            LayoutInflater.from(context).inflate(R.layout.friends_layout, parent, false)
        return FriendsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val currentFriend = friendsList[position]
        holder.textName.text = currentFriend.username

        // Find the ImageViews from the layout
        val chat = holder.itemView.findViewById<ImageView>(R.id.friend_message)
        val removeFriend = holder.itemView.findViewById<ImageView>(R.id.remove_friend)

        val dataBase = DataBase(context)
        val auth = FirebaseAuth.getInstance()

        // Open the chat with the current friend
        chat?.setOnClickListener {
            val intent = Intent(context, ChatFriendsActivity::class.java)
            intent.putExtra("username", currentFriend.username)
            intent.putExtra("uid", currentFriend.uid)
            intent.putExtra("icon", currentFriend.icon)
            context.startActivity(intent)
        }

        // Remove the current friend from the user's friend list
        removeFriend?.setOnClickListener {
            auth.currentUser?.uid?.let { userId ->
                currentFriend.uid?.let { friendId ->
                    dataBase.removeFriendFromDataBase(userId, friendId)
                }
            }
            // refresh friends view after remove friend
            val options = ActivityOptions.makeCustomAnimation(context, 0, 0) // remove animation
            context.startActivity(Intent(context, RecycleUserFriends::class.java), options.toBundle())
            if (context is RecycleUserFriends){
                context.overridePendingTransition(0, 0) // No animation older android version
                context.finish()
            }
        }
    }
}
