package com.example.mealmaestro

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mealmaestro.Helper.Comment
import java.text.SimpleDateFormat
import java.util.*

class CommentAdapter(
    private val context: Context,
    private var commentList: MutableList<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.bind(comment)
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    fun updateComments(newComments: MutableList<Comment>) {
        this.commentList = newComments
        notifyDataSetChanged()
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewComment: TextView = itemView.findViewById(R.id.comment_text)
        private val textViewUsername: TextView = itemView.findViewById(R.id.comment_username)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.comment_timestamp)

        fun bind(comment: Comment) {
            textViewComment.text = comment.text
            textViewUsername.text = comment.username

            val formattedDate = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                .format(Date(comment.timestamp))
            textViewTimestamp.text = formattedDate
        }
    }
}

