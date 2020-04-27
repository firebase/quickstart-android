package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.ActivityCustomBinding

/**
 * Demonstrate Firebase Authentication using a custom minted token. For more information, see:
 * https://firebase.google.com/docs/auth/android/custom-auth
 */
class CustomAuthActivity : AppCompatActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private lateinit var binding: ActivityCustomBinding

    private var customToken: String? = null
    private lateinit var tokenReceiver: TokenBroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Button click listeners
        binding.buttonSignIn.setOnClickListener(this)

        // Create token receiver (for demo purposes only)
        tokenReceiver = object : TokenBroadcastReceiver() {
            override fun onNewToken(token: String?) {
                Log.d(TAG, "onNewToken:$token")
                setCustomToken(token.toString())
            }
        }

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]

    override fun onResume() {
        super.onResume()
        registerReceiver(tokenReceiver, TokenBroadcastReceiver.filter)
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(tokenReceiver)
    }

    private fun startSignIn() {
        // Initiate sign in with custom token
        // [START sign_in_custom]
        customToken?.let {
            auth.signInWithCustomToken(it)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCustomToken:success")
                            val user = auth.currentUser
                            updateUI(user)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                            Toast.makeText(baseContext, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show()
                            updateUI(null)
                        }
                    }
        }
        // [END sign_in_custom]
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.textSignInStatus.text = getString(R.string.custom_auth_signin_status_user, user.uid)
        } else {
            binding.textSignInStatus.text = getString(R.string.custom_auth_signin_status_failed)
        }
    }

    private fun setCustomToken(token: String) {
        customToken = token

        val status = "Token:$customToken"

        // Enable/disable sign-in button and show the token
        binding.buttonSignIn.isEnabled = true
        binding.textTokenStatus.text = status
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.buttonSignIn) {
            startSignIn()
        }
    }

    companion object {
        private const val TAG = "CustomAuthActivity"
    }
}
