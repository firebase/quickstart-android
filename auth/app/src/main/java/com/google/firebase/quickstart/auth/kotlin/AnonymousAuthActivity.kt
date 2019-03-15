package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.quickstart.auth.R
import kotlinx.android.synthetic.main.activity_anonymous_auth.anonymousStatusEmail
import kotlinx.android.synthetic.main.activity_anonymous_auth.anonymousStatusId
import kotlinx.android.synthetic.main.activity_anonymous_auth.buttonAnonymousSignIn
import kotlinx.android.synthetic.main.activity_anonymous_auth.buttonAnonymousSignOut
import kotlinx.android.synthetic.main.activity_anonymous_auth.buttonLinkAccount
import kotlinx.android.synthetic.main.activity_anonymous_auth.fieldEmail
import kotlinx.android.synthetic.main.activity_anonymous_auth.fieldPassword

/**
 * Activity to demonstrate anonymous login and account linking (with an email/password account).
 */
class AnonymousAuthActivity : BaseActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_anonymous_auth)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        // Click listeners
        buttonAnonymousSignIn.setOnClickListener(this)
        buttonAnonymousSignOut.setOnClickListener(this)
        buttonLinkAccount.setOnClickListener(this)
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
        showProgressDialog()
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
                    hideProgressDialog()
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
        val email = fieldEmail.text.toString()
        val password = fieldPassword.text.toString()

        // Create EmailAuthCredential with email and password
        val credential = EmailAuthProvider.getCredential(email, password)

        // Link the anonymous user to the email credential
        showProgressDialog()

        // [START link_credential]
        auth.currentUser?.linkWithCredential(credential)
                ?.addOnCompleteListener(this) { task ->
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
                    hideProgressDialog()
                    // [END_EXCLUDE]
                }
        // [END link_credential]
    }

    private fun validateLinkForm(): Boolean {
        var valid = true

        val email = fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            fieldEmail.error = "Required."
            valid = false
        } else {
            fieldEmail.error = null
        }

        val password = fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            fieldPassword.error = "Required."
            valid = false
        } else {
            fieldPassword.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        val isSignedIn = user != null

        // Status text
        if (isSignedIn) {
            anonymousStatusId.text = getString(R.string.id_fmt, user!!.uid)
            anonymousStatusEmail.text = getString(R.string.email_fmt, user.email)
        } else {
            anonymousStatusId.setText(R.string.signed_out)
            anonymousStatusEmail.text = null
        }

        // Button visibility
        buttonAnonymousSignIn.isEnabled = !isSignedIn
        buttonAnonymousSignOut.isEnabled = isSignedIn
        buttonLinkAccount.isEnabled = isSignedIn
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.buttonAnonymousSignIn -> signInAnonymously()
            R.id.buttonAnonymousSignOut -> signOut()
            R.id.buttonLinkAccount -> linkAccount()
        }
    }

    companion object {
        private const val TAG = "AnonymousAuth"
    }
}
