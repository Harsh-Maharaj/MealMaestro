package com.example.mealmaestro.Chats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(val context: Context, val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.receive_layout, parent, false)
            return ReceiveViewHolder(view)
        } else {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.send_layout, parent, false)
            return SendViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val currenteMessage = messageList[position]

        if (holder.javaClass == SendViewHolder::class.java) {
            val viewHolder = holder as SendViewHolder
            holder.sendMessage.text = currenteMessage.message.toString()
        } else {
            val viewHolder = holder as ReceiveViewHolder
            holder.receiveMessage.text = currenteMessage.message.toString()
        }
    }

    // check if we are sending or receiving a message
    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser!!.uid == currentMessage.sender) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    // capture the id to where to send the message
    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sendMessage = itemView.findViewById<TextView>(R.id.send_friend_message)

    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage = itemView.findViewById<TextView>(R.id.receive_friend_message)
    }

}