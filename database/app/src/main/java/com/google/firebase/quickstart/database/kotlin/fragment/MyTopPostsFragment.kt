package com.google.firebase.quickstart.database.kotlin.fragment

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class MyTopPostsFragment : PostListFragment() {

    override fun getQuery(databaseReference: DatabaseReference): Query {
        // [START my_top_posts_query]
        // My top posts by number of stars
        val myUserId = uid
        // [END my_top_posts_query]

        return databaseReference.child("user-posts").child(myUserId)
                .orderByChild("starCount")
    }
}
