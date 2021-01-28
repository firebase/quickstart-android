package com.google.firebase.quickstart.database.kotlin.listfragments

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class MyTopPostsFragment : PostListFragment() {

    override fun getQuery(databaseReference: DatabaseReference): Query {
        // My top posts by number of stars
        val myUserId = uid

        return databaseReference.child("user-posts").child(myUserId)
                .orderByChild("starCount")
    }
}
