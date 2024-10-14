package com.example.mealmaestro.Chats

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mealmaestro.Helper.DataBase
import com.example.mealmaestro.R
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private val context: Context,
    private val messageList: ArrayList<Message>,
    private val senderRoom: String,
    private val receiverRoom: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layout = if (viewType == ITEM_RECEIVE) R.layout.receive_layout else R.layout.send_layout
        val view = LayoutInflater.from(context).inflate(layout, parent, false)
        return if (viewType == ITEM_RECEIVE) ReceiveViewHolder(view) else SendViewHolder(view)
    }

    override fun getItemCount(): Int = messageList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messageList[position]
        Log.d("MessageAdapter", "Binding position=$position, message=${currentMessage.message}, isImage=${currentMessage.image}")

        if (holder is SendViewHolder) {
            bindSendViewHolder(holder, currentMessage)
        } else if (holder is ReceiveViewHolder) {
            bindReceiveViewHolder(holder, currentMessage)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messageList[position]
        return if (FirebaseAuth.getInstance().currentUser!!.uid == currentMessage.sender) ITEM_SENT else ITEM_RECEIVE
    }

    private fun bindSendViewHolder(holder: SendViewHolder, message: Message) {
        if (message.image) {
            holder.sendImage.visibility = View.VISIBLE
            holder.sendMessage.visibility = View.GONE
            Glide.with(context).load(message.message).error(R.drawable.error_placeholder).into(holder.sendImage)
        } else {
            holder.sendMessage.visibility = View.VISIBLE
            holder.sendMessage.text = message.message
            holder.sendImage.visibility = View.GONE
        }
        holder.itemView.setOnLongClickListener {
            showOptionsDialog(message, isSender = true)
            true
        }
    }

    private fun bindReceiveViewHolder(holder: ReceiveViewHolder, message: Message) {
        if (message.image) {
            holder.receiveImage.visibility = View.VISIBLE
            holder.receiveMessage.visibility = View.GONE
            Glide.with(context).load(message.message).error(R.drawable.error_placeholder).into(holder.receiveImage)
        } else {
            holder.receiveMessage.visibility = View.VISIBLE
            holder.receiveMessage.text = message.message
            holder.receiveImage.visibility = View.GONE
        }
        holder.itemView.setOnLongClickListener {
            showOptionsDialog(message, isSender = false)
            true
        }
    }

    class SendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sendMessage: TextView = itemView.findViewById(R.id.send_friend_message)
        val sendImage: ImageView = itemView.findViewById(R.id.send_friend_image)
    }

    class ReceiveViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receiveMessage: TextView = itemView.findViewById(R.id.receive_friend_message)
        val receiveImage: ImageView = itemView.findViewById(R.id.receive_friend_image)
    }

    private fun showOptionsDialog(message: Message, isSender: Boolean) {
        val dataBase = DataBase()
        val options = when {
            message.image && isSender -> arrayOf("Delete")
            message.image && !isSender -> arrayOf("Download")
            isSender -> arrayOf("Edit", "Delete")
            else -> emptyArray()
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select an action")
        builder.setItems(options) { _, which ->
            when (options[which]) {
                "Edit" -> dataBase.editMessage(message, context, senderRoom, receiverRoom)
                "Delete" -> dataBase.deleteMessage(message, context, senderRoom, receiverRoom)
                "Download" -> downloadImage(message.message)
            }
        }
        builder.show()
    }

    private fun downloadImage(url: String) {
        val request = DownloadManager.Request(Uri.parse(url))
            .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setTitle("Downloading Image")
            .setDescription("Downloading image from chat")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "${System.currentTimeMillis()}.jpg")

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.enqueue(request)
        Toast.makeText(context, "Downloading Image...", Toast.LENGTH_SHORT).show()
    }
}
