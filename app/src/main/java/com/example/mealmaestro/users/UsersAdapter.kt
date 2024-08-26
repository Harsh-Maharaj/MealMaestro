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
import android.widget.Filter
import android.widget.Filterable

class UsersAdapter(val context: Context, private var userList: ArrayList<Users>) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>(), Filterable {

    private var userListFull: ArrayList<Users> = ArrayList(userList)

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
        val goBack = holder.itemView.findViewById<ImageView>(R.id.user_back)
        val dataBase = DataBase(context)
        val auth = FirebaseAuth.getInstance()

        // go back
        goBack.setOnClickListener {
            if (context is Activity) {
                context.finish()
            }
        }

        // open friend chat
        chat.setOnClickListener {
            val intent = Intent(context, ChatFriendsActivity::class.java)
            intent.putExtra("name", currentUser.name)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("icon", currentUser.icon)
            context.startActivity(intent)

        }
        // add new friend
        addFriend.setOnClickListener {
            dataBase.addFriendToDataBase(auth.currentUser!!.uid, currentUser.uid)
        }
    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.user_id)
    }

    // filter (search friend)
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<Users>()

                if (constraint == null || constraint.isEmpty()) {
                    filteredList.addAll(userListFull)
                } else {
                    val filterPattern = constraint.toString().toLowerCase().trim()

                    for (user in userListFull) {
                        if (user.name?.toLowerCase()?.contains(filterPattern) == true) {
                            filteredList.add(user)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults?
            ) {
                userList.clear()
                userList.addAll(results?.values as ArrayList<Users>)
                notifyDataSetChanged()
            }
        }
    }
}
