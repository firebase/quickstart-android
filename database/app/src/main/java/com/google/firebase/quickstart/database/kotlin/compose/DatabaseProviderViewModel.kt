package com.google.firebase.quickstart.database.kotlin.compose

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.database.kotlin.compose.flowcontrol.LoadDataStatus
import com.google.firebase.quickstart.database.kotlin.models.Comment
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.kotlin.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class DatabaseProviderViewModel(
    val database: FirebaseDatabase
) : ViewModel() {

    //variables for posts
    private val postList = mutableStateListOf<Post>()
    private val postDetails = mutableStateOf(Post())
    private val commentList = mutableStateListOf<Comment>()
    private val postId = mutableStateOf("")

    //variables for flow of loading data
    private val _dataLoadFlow = MutableStateFlow<LoadDataStatus<String>?>(null)
    val dataLoadFlow: StateFlow<LoadDataStatus<String>?> = _dataLoadFlow

    //for handling new post
    private val _title = mutableStateOf("")
    private val _body = mutableStateOf("")
    private val _uid = mutableStateOf("")

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>, extras: CreationExtras
            ): T {
                // Get instance.
                val database = Firebase.database
                return DatabaseProviderViewModel(database) as T
            }
        }
    }

    fun setContent(title: String, body: String, uid: String) {
        _title.value = title
        _body.value = body
        _uid.value = uid
    }

    private fun writeNewPost(userId: String, username: String, title: String, body: String) = viewModelScope.launch {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        val key = database.reference.child("posts").push().key ?: return@launch
        val post = Post(key, userId, username, title, body)
        val postValues = post.toMap()

        val childUpdates = hashMapOf<String, Any>(
            "/posts/$key" to postValues, "/user-posts/$userId/$key" to postValues
        )

        _dataLoadFlow.value = LoadDataStatus.Loading
        try {
            database.reference.updateChildren(childUpdates).await()
            _dataLoadFlow.value = LoadDataStatus.Loaded()
        } catch (e: Exception) {
            _dataLoadFlow.value = LoadDataStatus.Failed(e)
        }
    }

    fun submitPost() = viewModelScope.launch {

        // Title is required
        if (_title.value.isEmpty()) {
            return@launch
        }

        if (_body.value.isEmpty()) {
            return@launch
        }

        val userId = _uid.value
        database.reference.child("users").child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get user value
                val user = dataSnapshot.getValue<User>()
                if (user == null) {
                    // User is null, error out
                    Log.e("NewPostFragment", "User $userId is unexpectedly null")/*  Toast.makeText(context,
                              "Error: could not fetch user.",
                              Toast.LENGTH_SHORT).show()*/
                } else {
                    // Write new post
                    writeNewPost(userId, user.username.toString(), _title.value, _body.value)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("NewPostFragment", "getUser:onCancelled", databaseError.toException())
            }
        })
    }

    //for post detail and comments page
    fun getPostDetail(uid: String): Post {
        setPostDetail(uid)
        return postDetails.value
    }

    fun setPostValues(post: Post) {
        postDetails.value = post
    }

    fun getCommentLists(): MutableList<Comment> {
        return commentList
    }

    fun initCommentLists(commentId: String) {

        val commentsReference = Firebase.database.reference
            .child("post-comments").child(commentId)

        commentsReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for (s in snapshot.children) {
                    var comment = s.getValue(Comment::class.java)
                    commentList.add(comment!!)
                }

            }

            override fun onCancelled(p0: DatabaseError) {
            }
        }
        )
    }

    fun postComment(commentId: String, uid: String, commentText: String) {
        val commentsReference = Firebase.database.reference
            .child("post-comments").child(commentId)

        database.reference.child("users").child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue<User>() ?: return

                    val authorName = user.username
                    val comment = Comment(uid, authorName, commentText)
                    commentsReference.push().setValue(comment)
                    initCommentLists(commentId)
                }

                override fun onCancelled(p0: DatabaseError) {
                }
            }
            )
    }

    private fun setPostDetail(uid: String) {
        database.getReference("posts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.child(uid).getValue(Post::class.java)
                post?.key = snapshot.child(uid).key.toString()
                setPostValues(post!!)
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }

    fun setStarStatus(postRef: DatabaseReference, uid: String) {
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
                //update the list
                database.reference.child("posts")
            }
        })
    }

    //get the list of posts
    fun getPosts(databaseReference: DatabaseReference): MutableList<Post> {
        updateList(databaseReference)
        return postList
    }

    //update list
    private fun updateList(databaseReference: DatabaseReference) {
        _dataLoadFlow.value = LoadDataStatus.Loading
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                postList.clear()
                for (s in snapshot.children) {
                    var post = s.getValue(Post::class.java)
                    post?.key = s.key.toString()
                    postList.add(post!!)
                }
                _dataLoadFlow.value = LoadDataStatus.Loaded()
            }

            override fun onCancelled(error: DatabaseError) {
                _dataLoadFlow.value = LoadDataStatus.Failed(error.toException())
            }

        })
    }


    fun getPostID():String{
        return postId.value
    }

    fun setPostID(id: String){
        postId.value = id
    }

}


