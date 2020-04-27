package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.ActivityAnonymousAuthBinding

/**
 * Activity to demonstrate anonymous login and account linking (with an email/password account).
 */
class AnonymousAuthActivity : BaseActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private lateinit var binding: ActivityAnonymousAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAnonymousAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setProgressBar(binding.progressBar)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        // Click listeners
        binding.buttonAnonymousSignIn.setOnClickListener(this)
        binding.buttonAnonymousSignOut.setOnClickListener(this)
        binding.buttonLinkAccount.setOnClickListener(this)
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]

    private fun signInAnonymously() {
        showProgressBar()
        // [START signin_anonymously]
        auth.signInAnonymously()
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInAnonymously:success")
                        val user = auth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInAnonymously:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    hideProgressBar()
                    // [END_EXCLUDE]
                }
        // [END signin_anonymously]
    }

    private fun signOut() {
        auth.signOut()
        updateUI(null)
    }

    private fun linkAccount() {
        // Make sure form is valid
        if (!validateLinkForm()) {
            return
        }

        // Get email and password from the form
        val email = binding.fieldEmail.text.toString()
        val password = binding.fieldPassword.text.toString()

        // Create EmailAuthCredential with email and password
        val credential = EmailAuthProvider.getCredential(email, password)

        // Link the anonymous user to the email credential
        showProgressBar()

        // [START link_credential]
        auth.currentUser!!.linkWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "linkWithCredential:success")
                        val user = task.result?.user
                        updateUI(user)
                    } else {
                        Log.w(TAG, "linkWithCredential:failure", task.exception)
                        Toast.makeText(baseContext, "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    hideProgressBar()
                    // [END_EXCLUDE]
                }
        // [END link_credential]
    }

    private fun validateLinkForm(): Boolean {
        var valid = true

        val email = binding.fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.fieldEmail.error = "Required."
            valid = false
        } else {
            binding.fieldEmail.error = null
        }

        val password = binding.fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.fieldPassword.error = "Required."
            valid = false
        } else {
            binding.fieldPassword.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        val isSignedIn = user != null

        // Status text
        if (isSignedIn) {
            binding.anonymousStatusId.text = getString(R.string.id_fmt, user!!.uid)
            binding.anonymousStatusEmail.text = getString(R.string.email_fmt, user.email)
        } else {
            binding.anonymousStatusId.setText(R.string.signed_out)
            binding.anonymousStatusEmail.text = null
        }

        // Button visibility
        binding.buttonAnonymousSignIn.isEnabled = !isSignedIn
        binding.buttonAnonymousSignOut.isEnabled = isSignedIn
        binding.buttonLinkAccount.isEnabled = isSignedIn
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonAnonymousSignIn -> signInAnonymously()
            R.id.buttonAnonymousSignOut -> signOut()
            R.id.buttonLinkAccount -> linkAccount()
        }
    }

    companion object {
        private const val TAG = "AnonymousAuth"
    }
}
