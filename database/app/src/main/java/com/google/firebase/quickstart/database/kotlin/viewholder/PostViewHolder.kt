package com.google.firebase.quickstart.database.kotlin.viewholder

import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Post
import kotlinx.android.synthetic.main.include_post_author.view.postAuthor
import kotlinx.android.synthetic.main.include_post_text.view.postBody
import kotlinx.android.synthetic.main.include_post_text.view.postTitle


class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    fun bindToPost(post: Post, starClickListener: View.OnClickListener) {
        itemView.postTitle.text = post.title
        itemView.postAuthor.text = post.author

        itemView.postBody.text = post.body


    }


}
