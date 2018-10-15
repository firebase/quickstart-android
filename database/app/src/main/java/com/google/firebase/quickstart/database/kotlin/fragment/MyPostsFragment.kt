package com.google.firebase.quickstart.database.kotlin.fragment

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class MyPostsFragment : PostListFragment() {

    override fun getQuery(databaseReference: DatabaseReference): Query {
        // All my posts
        return databaseReference.child("user-posts")
                .child(uid)
    }
}
