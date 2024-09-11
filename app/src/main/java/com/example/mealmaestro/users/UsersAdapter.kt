package com.example.mealmaestro.users

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
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
import java.util.Locale

class UsersAdapter(
    val context: Context,
    private var userList: ArrayList<Users>,
) : RecyclerView.Adapter<UsersAdapter.UserViewHolder>(), Filterable {

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

        // Fetch views by IDs and ensure they are found
        val chat = holder.itemView.findViewById<ImageView>(R.id.user_message)
        val addFriend = holder.itemView.findViewById<ImageView>(R.id.add_friend)

        // Verify that the views are not null before attaching click listeners
        chat?.setOnClickListener {
            val intent = Intent(context, ChatFriendsActivity::class.java)
            intent.putExtra("name", currentUser.name)
            intent.putExtra("uid", currentUser.uid)
            intent.putExtra("icon", currentUser.icon)
            context.startActivity(intent)
        }

        addFriend?.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val dataBase = DataBase(context)
            dataBase.addFriendToDataBase(auth.currentUser!!.uid, currentUser.uid)

            // Notify RecycleUserView that a friend was added and finish the activity
            (context as Activity).setResult(Activity.RESULT_OK)
            context.finish()
        }

    }

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textName: TextView = itemView.findViewById(R.id.user_id)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<Users>()
                Log.d("AdapterInit", "UserListFull size: ${userListFull.size}")
                Log.d("PerformFiltering", "Constraint: $constraint")



                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(userListFull)
                    Log.d("UserListFullCheck", "User list full size: ${userListFull.size}")

                } else {
                    val filterPattern = constraint.toString().lowercase().trim()

                    for (user in userListFull) {
                        Log.d("UserListFull", "User name: ${user.name}")
                        Log.d("PerformFiltering", "User name: ${user.name}")
                        if (user.name?.lowercase()?.contains(filterPattern) == true) {
                            filteredList.add(user)
                        }
                    }
                }

                Log.d("FilterResults", "Filtered list size: ${filteredList.size}")

                val results = FilterResults()
                results.values = filteredList
                return results
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                userList.clear()
                userList.addAll(results?.values as ArrayList<Users>)
                Log.d("PublishResults", "Updated list size: ${userList.size}")
                notifyDataSetChanged()
            }
        }
    }

    fun updateUserListFull(newUserList: ArrayList<Users>) {
        userListFull = newUserList
    }

}
