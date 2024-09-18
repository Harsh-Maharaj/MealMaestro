package com.example.mealmaestro.Chats

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.R
import com.google.firebase.auth.FirebaseAuth

// MessageAdapter is a RecyclerView adapter that manages chat messages
// and displays them in the appropriate format (sent or received)
class MessageAdapter(val context: Context, val messageList: ArrayList<Message>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Constants to represent the two different view types: sent and received messages
    private val ITEM_RECEIVE = 1 // Message received by the user
    private val ITEM_SENT = 2 // Message sent by the user

    // This method is called to create the ViewHolder (the view for each item in the list)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        // If the message type is receive (1), inflate the receive layout
        if (viewType == ITEM_RECEIVE) {
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.receive_layout, parent, false)
            return ReceiveViewHolder(view) // Return the ReceiveViewHolder for received messages
        } else {
            // If the message type is sent (2), inflate the send layout
            val view: View =
                LayoutInflater.from(context).inflate(R.layout.send_layout, parent, false)
            return SendViewHolder(view) // Return the SendViewHolder for sent messages
        }
    }

    // Returns the total number of messages in the chat (size of the messageList)
    override fun getItemCount(): Int {
        return messageList.size
    }

    // Called to bind data (message content) to the view holder based on the position in the list
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        // Get the message at the current position
        val currentMessage = messageList[position]

        // Check the type of the holder (whether it's a sent or received message)
        if (holder.javaClass == SendViewHolder::class.java) {
            // If it's a SendViewHolder, bind the sent message content
            val viewHolder = holder as SendViewHolder
            viewHolder.sendMessage.text = currentMessage.message.toString()
        } else {
            // If it's a ReceiveViewHolder, bind the received message content
            val viewHolder = holder as ReceiveViewHolder
            viewHolder.receiveMessage.text = currentMessage.message.toString()
        }
    }

    // Determines whether the current message is sent or received, to decide the view type
    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        // If the current user's UID matches the message sender, it's a sent message
        return if (FirebaseAuth.getInstance().currentUser!!.uid == currentMessage.sender) {
            ITEM_SENT // Return 2 for sent messages
        } else {
            ITEM_RECEIVE // Return 1 for received messages
        }
    }

    // ViewHolder for sent messages, referencing the layout for a sent message
    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sendMessage: TextView = itemView.findViewById(R.id.send_friend_message) // TextView for the sent message
    }

    // ViewHolder for received messages, referencing the layout for a received message
    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_friend_message) // TextView for the received message
    }
}
