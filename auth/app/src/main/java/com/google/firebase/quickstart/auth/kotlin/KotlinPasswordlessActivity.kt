package com.google.firebase.quickstart.auth.kotlin

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.google.firebase.auth.*
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.java.BaseActivity
import kotlinx.android.synthetic.main.activity_passwordless.*


/**
 * Demonstrate Firebase Authentication without a password, using a link sent to an
 * email address.
 */
class KotlinPasswordlessActivity : BaseActivity(), View.OnClickListener {

    private val TAG = "PasswordlessSignIn"
    private val KEY_PENDING_EMAIL = "key_pending_email"

    private var mPendingEmail: String = ""
    private var mEmailLink: String = ""
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passwordless)

        mAuth = FirebaseAuth.getInstance()

        passwordless_send_email_button.setOnClickListener(this)
        passwordless_sign_in_button.setOnClickListener(this)
        sign_out_button.setOnClickListener(this)

        // Restore the "pending" email address
        if (savedInstanceState != null) {
            mPendingEmail = savedInstanceState.getString(KEY_PENDING_EMAIL, null)
            field_email.setText(mPendingEmail)
        }

        // Check if the Intent that started the Activity contains an email sign-in link.
        checkIntent(intent)
    }

    override fun onStart() {
        super.onStart()
        updateUI(mAuth.currentUser)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        checkIntent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_PENDING_EMAIL, mPendingEmail)
    }

    /**
     * Check to see if the Intent has an email link, and if so set up the UI accordingly.
     * This can be called from either onCreate or onNewIntent, depending on how the Activity
     * was launched.
     */
    private fun checkIntent(intent: Intent?) {
        if (intentHasEmailLink(intent)) {
            mEmailLink = intent!!.data!!.toString()

            status.setText(R.string.status_link_found)
            passwordless_send_email_button.isEnabled = false
            passwordless_sign_in_button.isEnabled = true
        } else {
            status.setText(R.string.status_email_not_sent)
            passwordless_send_email_button.isEnabled = true
            passwordless_sign_in_button.isEnabled = false
        }
    }

    /**
     * Determine if the given Intent contains an email sign-in link.
     */
    private fun intentHasEmailLink(intent: Intent?): Boolean {
        if (intent != null && intent.data != null) {
            val intentData = intent.data!!.toString()
            if (mAuth.isSignInWithEmailLink(intentData)) {
                return true
            }
        }

        return false
    }

    /**
     * Send an email sign-in link to the specified email.
     */
    private fun sendSignInLink(email: String) {
        val settings = ActionCodeSettings.newBuilder()
                .setAndroidPackageName(
                        packageName,
                        false, null/* minimum app version */)/* install if not available? */
                .setHandleCodeInApp(true)
                .setUrl("https://auth.example.com/emailSignInLink")
                .build()

        hideKeyboard(field_email)
        showProgressDialog()

        mAuth.sendSignInLinkToEmail(email, settings)
                .addOnCompleteListener { task ->
                    hideProgressDialog()

                    if (task.isSuccessful) {
                        Log.d(TAG, "Link sent")
                        showSnackbar("Sign-in link sent!")

                        mPendingEmail = email
                        status.setText(R.string.status_email_sent)
                    } else {
                        val e = task.exception
                        Log.w(TAG, "Could not send link", e)
                        showSnackbar("Failed to send link.")

                        if (e is FirebaseAuthInvalidCredentialsException) {
                            field_email.error = "Invalid email address."
                        }
                    }
                }
    }

    /**
     * Sign in using an email address and a link, the link is passed to the Activity
     * from the dynamic link contained in the email.
     */
    private fun signInWithEmailLink(email: String, link: String?) {
        Log.d(TAG, "signInWithLink:" + link!!)

        hideKeyboard(field_email)
        showProgressDialog()

        mAuth.signInWithEmailLink(email, link)
                .addOnCompleteListener { task ->
                    hideProgressDialog()
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmailLink:success")

                        field_email.text = null
                        updateUI(task.result.user)
                    } else {
                        Log.w(TAG, "signInWithEmailLink:failure", task.exception)
                        updateUI(null)

                        if (task.exception is FirebaseAuthActionCodeException) {
                            showSnackbar("Invalid or expired sign-in link.")
                        }
                    }
                }
    }

    private fun onSendLinkClicked() {
        val email = field_email.text.toString()
        if (TextUtils.isEmpty(email)) {
            field_email.error = "Email must not be empty."
            return
        }

        sendSignInLink(email)
    }

    private fun onSignInClicked() {
        val email = field_email.text.toString()
        if (TextUtils.isEmpty(email)) {
            field_email.error = "Email must not be empty."
            return
        }

        signInWithEmailLink(email, mEmailLink)
    }

    private fun onSignOutClicked() {
        mAuth.signOut()

        updateUI(null)
        status.setText(R.string.status_email_not_sent)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            status.text = getString(R.string.passwordless_status_fmt,
                    user.email, user.isEmailVerified)

            passwordless_fields.visibility = View.GONE
            passwordless_buttons.visibility = View.GONE
            signed_in_buttons.visibility = View.VISIBLE
        } else {
            passwordless_fields.visibility = View.VISIBLE
            passwordless_buttons.visibility = View.VISIBLE
            signed_in_buttons.visibility = View.GONE
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.passwordless_send_email_button -> onSendLinkClicked()
            R.id.passwordless_sign_in_button -> onSignInClicked()
            R.id.sign_out_button -> onSignOutClicked()
        }
    }
}
