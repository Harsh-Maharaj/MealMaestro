package com.example.mealmaestro.users

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Chats.ChatFriendsActivity
import com.example.mealmaestro.DataBase
import com.example.mealmaestro.R
import com.google.firebase.auth.FirebaseAuth

class UsersAdapter(val context: Context, val userList: ArrayList<Users>) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: View = LayoutInflater.from(context).inflate(R.layout.users_layout, parent, false)
        return UserViewHolder(view)
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.textName.text = currentUser.name

        val chat = holder.itemView.findViewById<ImageView>(R.id.user_message)
        val addFriend = holder.itemView.findViewById<ImageView>(R.id.add_friend)
        val dataBase = DataBase(context)
        val auth = FirebaseAuth.getInstance()

        // open friend chat
        chat.setOnClickListener {
            val intent = Intent(context, ChatFriendsActivity::class.java)
            intent.putExtra("name", currentUser.name)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("icon",currentUser.icon)
            context.startActivity(intent)

        }
        // add new friend
        addFriend.setOnClickListener {
            dataBase.addFriendToDataBase(auth.currentUser!!.uid, currentUser.uid)
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName = itemView.findViewById<TextView>(R.id.user_id)
    }
}