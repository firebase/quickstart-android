package com.google.firebase.quickstart.database.kotlin.viewholder

import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Post

class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val postTitle: TextView = itemView.findViewById(R.id.postTitle)
    private val postAuthor: TextView = itemView.findViewById(R.id.postAuthor)
    private val postNumStars: TextView = itemView.findViewById(R.id.postNumStars)
    private val postBody: TextView = itemView.findViewById(R.id.postBody)
    private val star: ImageView = itemView.findViewById(R.id.star)

    fun bindToPost(post: Post, starClickListener: View.OnClickListener) {
        postTitle.text = post.title
        postAuthor.text = post.author
        postNumStars.text = post.starCount.toString()
        postBody.text = post.body

        star.setOnClickListener(starClickListener)
    }

    fun setLikedState(liked: Boolean) {
        if (liked) {
            star.setImageResource(R.drawable.ic_toggle_star_24)
        } else {
            star.setImageResource(R.drawable.ic_toggle_star_outline_24)
        }
    }
}
