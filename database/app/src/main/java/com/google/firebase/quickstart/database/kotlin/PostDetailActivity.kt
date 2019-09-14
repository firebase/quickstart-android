package com.google.firebase.quickstart.database.kotlin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.kotlin.models.Comment
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.kotlin.models.User
import kotlinx.android.synthetic.main.activity_post_detail.buttonPostComment
import kotlinx.android.synthetic.main.activity_post_detail.fieldCommentText
import kotlinx.android.synthetic.main.activity_post_detail.recyclerPostComments
import kotlinx.android.synthetic.main.include_post_author.postAuthor
import kotlinx.android.synthetic.main.include_post_text.postBody
import kotlinx.android.synthetic.main.include_post_text.postTitle
import kotlinx.android.synthetic.main.item_comment.view.commentAuthor
import kotlinx.android.synthetic.main.item_comment.view.commentBody
import java.util.ArrayList

class PostDetailActivity : BaseActivity(), View.OnClickListener {

    private lateinit var postKey: String
    private lateinit var postReference: DatabaseReference
    private lateinit var commentsReference: DatabaseReference

    private var postListener: ValueEventListener? = null
    private var adapter: CommentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        // Get post key from intent
        postKey = intent.getStringExtra(EXTRA_POST_KEY)
                ?: throw IllegalArgumentException("Must pass EXTRA_POST_KEY")

        // Initialize Database
        postReference = FirebaseDatabase.getInstance().reference
                .child("posts").child(postKey)
        commentsReference = FirebaseDatabase.getInstance().reference
                .child("post-comments").child(postKey)

        // Initialize Views
        buttonPostComment.setOnClickListener(this)
        recyclerPostComments.layoutManager = LinearLayoutManager(this)
    }

    public override fun onStart() {
        super.onStart()

        // Add value event listener to the post
        // [START post_value_event_listener]
        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val post = dataSnapshot.getValue(Post::class.java)
                // [START_EXCLUDE]
                post?.let {
                    postAuthor.text = it.author
                    postTitle.text = it.title
                    postBody.text = it.body
                }
                // [END_EXCLUDE]
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
                // [START_EXCLUDE]
                Toast.makeText(baseContext, "Failed to load post.",
                        Toast.LENGTH_SHORT).show()
                // [END_EXCLUDE]
            }
        }
        postReference.addValueEventListener(postListener)
        // [END post_value_event_listener]

        // Keep copy of post listener so we can remove it when app stops
        this.postListener = postListener

        // Listen for comments
        adapter = CommentAdapter(this, commentsReference)
        recyclerPostComments.adapter = adapter
    }

    public override fun onStop() {
        super.onStop()

        // Remove post value event listener
        postListener?.let {
            postReference.removeEventListener(it)
        }

        // Clean up comments listener
        adapter?.cleanupListener()
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.buttonPostComment) {
            postComment()
        }
    }

    private fun postComment() {
        val uid = uid
        FirebaseDatabase.getInstance().reference.child("users").child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user information
                        val user = dataSnapshot.getValue(User::class.java)
                        if (user == null) {
                            return
                        }

                        val authorName = user.username

                        // Create new comment object
                        val commentText = fieldCommentText.text.toString()
                        val comment = Comment(uid, authorName, commentText)

                        // Push the comment, it will appear in the list
                        commentsReference.push().setValue(comment)

                        // Clear the field
                        fieldCommentText.text = null
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
    }

    private class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(comment: Comment) {
            itemView.commentAuthor.text = comment.author
            itemView.commentBody.text = comment.text
        }
    }

    private class CommentAdapter(
        private val context: Context,
        private val databaseReference: DatabaseReference
    ) : RecyclerView.Adapter<CommentViewHolder>() {

        private val childEventListener: ChildEventListener?

        private val commentIds = ArrayList<String>()
        private val comments = ArrayList<Comment>()

        init {

            // Create child event listener
            // [START child_event_listener_recycler]
            val childEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildAdded:" + dataSnapshot.key!!)

                    // A new comment has been added, add it to the displayed list
                    val comment = dataSnapshot.getValue(Comment::class.java)

                    // [START_EXCLUDE]
                    // Update RecyclerView
                    commentIds.add(dataSnapshot.key!!)
                    comments.add(comment!!)
                    notifyItemInserted(comments.size - 1)
                    // [END_EXCLUDE]
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildChanged: ${dataSnapshot.key}")

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so displayed the changed comment.
                    val newComment = dataSnapshot.getValue(Comment::class.java)
                    val commentKey = dataSnapshot.key

                    // [START_EXCLUDE]
                    val commentIndex = commentIds.indexOf(commentKey)
                    if (commentIndex > -1 && newComment != null) {
                        // Replace with the new data
                        comments[commentIndex] = newComment

                        // Update the RecyclerView
                        notifyItemChanged(commentIndex)
                    } else {
                        Log.w(TAG, "onChildChanged:unknown_child: $commentKey")
                    }
                    // [END_EXCLUDE]
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "onChildRemoved:" + dataSnapshot.key!!)

                    // A comment has changed, use the key to determine if we are displaying this
                    // comment and if so remove it.
                    val commentKey = dataSnapshot.key

                    // [START_EXCLUDE]
                    val commentIndex = commentIds.indexOf(commentKey)
                    if (commentIndex > -1) {
                        // Remove data from the list
                        commentIds.removeAt(commentIndex)
                        comments.removeAt(commentIndex)

                        // Update the RecyclerView
                        notifyItemRemoved(commentIndex)
                    } else {
                        Log.w(TAG, "onChildRemoved:unknown_child:" + commentKey!!)
                    }
                    // [END_EXCLUDE]
                }

                override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
                    Log.d(TAG, "onChildMoved:" + dataSnapshot.key!!)

                    // A comment has changed position, use the key to determine if we are
                    // displaying this comment and if so move it.
                    val movedComment = dataSnapshot.getValue(Comment::class.java)
                    val commentKey = dataSnapshot.key

                    // ...
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w(TAG, "postComments:onCancelled", databaseError.toException())
                    Toast.makeText(context, "Failed to load comments.",
                            Toast.LENGTH_SHORT).show()
                }
            }
            databaseReference.addChildEventListener(childEventListener)
            // [END child_event_listener_recycler]

            // Store reference to listener so it can be removed on app stop
            this.childEventListener = childEventListener
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val inflater = LayoutInflater.from(context)
            val view = inflater.inflate(R.layout.item_comment, parent, false)
            return CommentViewHolder(view)
        }

        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.bind(comments[position])
        }

        override fun getItemCount(): Int = comments.size

        fun cleanupListener() {
            childEventListener?.let {
                databaseReference.removeEventListener(it)
            }
        }
    }

    companion object {

        private const val TAG = "PostDetailActivity"
        const val EXTRA_POST_KEY = "post_key"
    }
}
