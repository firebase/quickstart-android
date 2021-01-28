package com.google.firebase.quickstart.database.kotlin.listfragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.Query
import com.google.firebase.database.Transaction
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.PostDetailFragment
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.kotlin.viewholder.PostViewHolder

abstract class PostListFragment : Fragment() {

    // [START define_database_reference]
    private lateinit var database: DatabaseReference
    // [END define_database_reference]

    private lateinit var recycler: RecyclerView
    private lateinit var manager: LinearLayoutManager
    private var adapter: FirebaseRecyclerAdapter<Post, PostViewHolder>? = null

    val uid: String
        get() = Firebase.auth.currentUser!!.uid

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_all_posts, container, false)

        // [START create_database_reference]
        database = Firebase.database.reference
        // [END create_database_reference]

        recycler = rootView.findViewById(R.id.messagesList)
        recycler.setHasFixedSize(true)

        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // Set up Layout Manager, reverse layout
        manager = LinearLayoutManager(activity)
        manager.reverseLayout = true
        manager.stackFromEnd = true
        recycler.layoutManager = manager

        // Set up FirebaseRecyclerAdapter with the Query
        val postsQuery = getQuery(database)

        val options = FirebaseRecyclerOptions.Builder<Post>()
                .setQuery(postsQuery, Post::class.java)
                .build()

        adapter = object : FirebaseRecyclerAdapter<Post, PostViewHolder>(options) {

            override fun onCreateViewHolder(viewGroup: ViewGroup, i: Int): PostViewHolder {
                val inflater = LayoutInflater.from(viewGroup.context)
                return PostViewHolder(inflater.inflate(R.layout.item_post, viewGroup, false))
            }

            override fun onBindViewHolder(viewHolder: PostViewHolder, position: Int, model: Post) {
                val postRef = getRef(position)

                // Set click listener for the whole post view
                val postKey = postRef.key
                viewHolder.itemView.setOnClickListener {
                    // Launch PostDetailFragment
                    val navController = requireActivity().findNavController(R.id.nav_host_fragment)
                    val args = bundleOf(PostDetailFragment.EXTRA_POST_KEY to postKey)
                    navController.navigate(R.id.action_MainFragment_to_PostDetailFragment, args)
                }

                // Determine if the current user has liked this post and set UI accordingly
                viewHolder.setLikedState(model.stars.containsKey(uid))

                // Bind Post to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToPost(model) {
                    // Need to write to both places the post is stored
                    val globalPostRef = database.child("posts").child(postRef.key!!)
                    val userPostRef = database.child("user-posts").child(model.uid!!).child(postRef.key!!)

                    // Run two transactions
                    onStarClicked(globalPostRef)
                    onStarClicked(userPostRef)
                }
            }
        }
        recycler.adapter = adapter
    }

    // [START post_stars_transaction]
    private fun onStarClicked(postRef: DatabaseReference) {
        postRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val p = mutableData.getValue(Post::class.java)
                        ?: return Transaction.success(mutableData)

                if (p.stars.containsKey(uid)) {
                    // Unstar the post and remove self from stars
                    p.starCount = p.starCount - 1
                    p.stars.remove(uid)
                } else {
                    // Star the post and add self to stars
                    p.starCount = p.starCount + 1
                    p.stars[uid] = true
                }

                // Set value and report transaction success
                mutableData.value = p
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                databaseError: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError!!)
            }
        })
    }
    // [END post_stars_transaction]

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    abstract fun getQuery(databaseReference: DatabaseReference): Query

    companion object {

        private const val TAG = "PostListFragment"
    }
}
