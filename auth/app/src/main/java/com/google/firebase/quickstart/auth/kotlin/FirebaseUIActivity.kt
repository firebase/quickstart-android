package com.google.firebase.quickstart.auth.kotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.BuildConfig
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.ActivityFirebaseUiBinding

/**
 * Demonstrate authentication using the FirebaseUI-Android library. This activity demonstrates
 * using FirebaseUI for basic email/password sign in.
 *
 * For more information, visit https://github.com/firebase/firebaseui-android
 */
class FirebaseUIActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityFirebaseUiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFirebaseUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.signInButton.setOnClickListener(this)
        binding.signOutButton.setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        updateUI(auth.currentUser)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                // Sign in succeeded
                updateUI(auth.currentUser)
            } else {
                // Sign in failed
                Toast.makeText(this, "Sign In Failed", Toast.LENGTH_SHORT).show()
                updateUI(null)
            }
        }
    }

    private fun startSignIn() {
        // Build FirebaseUI sign in intent. For documentation on this operation and all
        // possible customization see: https://github.com/firebase/firebaseui-android
        val intent = AuthUI.getInstance().createSignInIntentBuilder()
                .setIsSmartLockEnabled(!BuildConfig.DEBUG)
                .setAvailableProviders(listOf(AuthUI.IdpConfig.EmailBuilder().build()))
                .setLogo(R.mipmap.ic_launcher)
                .build()

        startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // Signed in
            binding.status.text = getString(R.string.firebaseui_status_fmt, user.email)
            binding.detail.text = getString(R.string.id_fmt, user.uid)

            binding.signInButton.visibility = View.GONE
            binding.signOutButton.visibility = View.VISIBLE
        } else {
            // Signed out
            binding.status.setText(R.string.signed_out)
            binding.detail.text = null

            binding.signInButton.visibility = View.VISIBLE
            binding.signOutButton.visibility = View.GONE
        }
    }

    private fun signOut() {
        AuthUI.getInstance().signOut(this)
        updateUI(null)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.signInButton -> startSignIn()
            R.id.signOutButton -> signOut()
        }
    }

    companion object {
        private const val RC_SIGN_IN = 9001
    }
}
