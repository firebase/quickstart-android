package com.google.firebase.quickstart.database.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.database.R
import com.google.firebase.quickstart.database.databinding.FragmentNewPostBinding
import com.google.firebase.quickstart.database.kotlin.models.Post
import com.google.firebase.quickstart.database.kotlin.models.User

class NewPostFragment : BaseFragment() {
    private var _binding: FragmentNewPostBinding? = null
    private val binding get() = _binding!!

    // [START declare_database_ref]
    private lateinit var database: DatabaseReference
    // [END declare_database_ref]

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentNewPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // [START initialize_database_ref]
        database = Firebase.database.reference
        // [END initialize_database_ref]

        binding.fabSubmitPost.setOnClickListener { submitPost() }
    }

    private fun submitPost() {
        val title = binding.fieldTitle.text.toString()
        val body = binding.fieldBody.text.toString()

        // Title is required
        if (TextUtils.isEmpty(title)) {
            binding.fieldTitle.error = REQUIRED
            return
        }

        // Body is required
        if (TextUtils.isEmpty(body)) {
            binding.fieldBody.error = REQUIRED
            return
        }

        // Disable button so there are no multi-posts
        setEditingEnabled(false)
        Toast.makeText(context, "Posting...", Toast.LENGTH_SHORT).show()

        // [START single_value_read]
        val userId = uid
        database.child("users").child(userId).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        // Get user value
                        val user = dataSnapshot.getValue<User>()

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User $userId is unexpectedly null")
                            Toast.makeText(context,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show()
                        } else {
                            // Write new post
                            writeNewPost(userId, user.username.toString(), title, body)
                        }

                        setEditingEnabled(true)
                        findNavController().navigate(R.id.action_NewPostFragment_to_MainFragment)
                        // [END_EXCLUDE]
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException())
                        // [START_EXCLUDE]
                        setEditingEnabled(true)
                        // [END_EXCLUDE]
                    }
                })
        // [END single_value_read]
    }

    private fun setEditingEnabled(enabled: Boolean) {
        with(binding) {
            fieldTitle.isEnabled = enabled
            fieldBody.isEnabled = enabled
            if (enabled) {
                fabSubmitPost.show()
            } else {
                fabSubmitPost.hide()
            }
        }
    }

    // [START write_fan_out]
    private fun writeNewPost(userId: String, username: String, title: String, body: String) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        val key = database.child("posts").push().key
        if (key == null) {
            Log.w(TAG, "Couldn't get push key for posts")
            return
        }

        val post = Post(userId, username, title, body)
        val postValues = post.toMap()

        val childUpdates = hashMapOf<String, Any>(
                "/posts/$key" to postValues,
                "/user-posts/$userId/$key" to postValues
        )

        database.updateChildren(childUpdates)
    }
    // [END write_fan_out]

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val TAG = "NewPostFragment"
        private const val REQUIRED = "Required"
    }
}