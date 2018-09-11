package com.google.firebase.quickstart.database.kotlin.fragment

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.Query

class RecentPostsFragment : PostListFragment() {

    override fun getQuery(databaseReference: DatabaseReference): Query {
        // [START recent_posts_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys.
        return databaseReference.child("posts")
                .limitToFirst(100)
        // [END recent_posts_query]
    }
}
