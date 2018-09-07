package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.java.TokenBroadcastReceiver
import kotlinx.android.synthetic.main.activity_custom.*


/**
 * Demonstrate Firebase Authentication using a custom minted token. For more information, see:
 * https://firebase.google.com/docs/auth/android/custom-auth
 */
class CustomAuthActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "CustomAuthActivity"

    // [START declare_auth]
    private var mAuth: FirebaseAuth? = null
    // [END declare_auth]

    private var mCustomToken: String? = null
    private var mTokenReceiver: TokenBroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_custom)

        // Button click listeners
        buttonSignIn.setOnClickListener(this)

        // Create token receiver (for demo purposes only)
        mTokenReceiver = object : TokenBroadcastReceiver() {
            override fun onNewToken(token: String) {
                Log.d(TAG, "onNewToken:$token")
                setCustomToken(token)
            }
        }

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance()
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth!!.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]

    override fun onResume() {
        super.onResume()
        registerReceiver(mTokenReceiver, TokenBroadcastReceiver.getFilter())
    }


    override fun onPause() {
        super.onPause()
        unregisterReceiver(mTokenReceiver)
    }

    private fun startSignIn() {
        // Initiate sign in with custom token
        // [START sign_in_custom]
        mAuth!!.signInWithCustomToken(mCustomToken!!)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCustomToken:success")
                        val user = mAuth!!.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        // [END sign_in_custom]
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            textSignInStatus.text = "User ID: $user.uid"
        } else {
            textSignInStatus.text = "Error: sign in failed."
        }
    }

    private fun setCustomToken(token: String) {
        mCustomToken = token

        val status: String
        if (mCustomToken != null) {
            status = "Token:" + mCustomToken!!
        } else {
            status = "Token: null"
        }

        // Enable/disable sign-in button and show the token
        buttonSignIn.isEnabled = mCustomToken != null
        textTokenStatus.text = status
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.buttonSignIn) {
            startSignIn()

        }
    }

}
