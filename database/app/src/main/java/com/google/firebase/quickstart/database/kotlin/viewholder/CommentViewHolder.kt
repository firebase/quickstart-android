package com.google.firebase.quickstart.database.kotlin.viewholder

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Comment

class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    fun bind(comment: Comment) {
        itemView.findViewById<TextView>(R.id.commentAuthor).text = comment.author
        itemView.findViewById<TextView>(R.id.commentBody).text = comment.text
    }
}