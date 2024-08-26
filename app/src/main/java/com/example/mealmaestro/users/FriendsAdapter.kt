package com.example.mealmaestro.users

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Chats.ChatFriendsActivity
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.R
import com.google.firebase.auth.FirebaseAuth

class FriendsAdapter(val context: Context, val friendsList: ArrayList<Users>) :
    RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName = itemView.findViewById<TextView>(R.id.friend_id)

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
        holder.textName.text = currentFriend.name

        val chat = holder.itemView.findViewById<ImageView>(R.id.friend_message)
        val removeFriend = holder.itemView.findViewById<ImageView>(R.id.remove_friend)
        val openUsers = holder.itemView.findViewById<ImageView>(R.id.friends_to_users)
        val friendsBack = holder.itemView.findViewById<ImageView>(R.id.friend_back)
        val dataBase = DataBase(context)
        val auth = FirebaseAuth.getInstance()

        // go back
        friendsBack.setOnClickListener {
            if (context is Activity) {
                context.finish()
            }
        }
        // open users
        openUsers.setOnClickListener {
            context.startActivity(Intent(context, RecycleUserView::class.java))
        }

        // open friend chat
        chat.setOnClickListener {
            val intent = Intent(context, ChatFriendsActivity::class.java)
            intent.putExtra("name", currentFriend.name)
            intent.putExtra("uid", currentFriend.uid)
            intent.putExtra("icon", currentFriend.icon)
            context.startActivity(intent)
        }

        // remove friend
        removeFriend.setOnClickListener {
            dataBase.removeFriendFromDataBase(auth.currentUser!!.uid, currentFriend.uid!!)
        }

    }
}