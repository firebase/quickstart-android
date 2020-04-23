package com.google.firebase.quickstart.auth.kotlin

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.databinding.ActivityMultiFactorBinding

class MultiFactorActivity : BaseActivity(), View.OnClickListener {
    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private lateinit var binding: ActivityMultiFactorBinding

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiFactorBinding.inflate(layoutInflater)
        setContentView(binding.reloadButton)
        setProgressBar(binding.progressBar)

        // Buttons
        binding.emailSignInButton.setOnClickListener(this)
        binding.signOutButton.setOnClickListener(this)
        binding.verifyEmailButton.setOnClickListener(this)
        binding.enrollMfa.setOnClickListener(this)
        binding.unenrollMfa.setOnClickListener(this)
        binding.reloadButton.setOnClickListener(this)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        showDisclaimer()
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    // [END on_start_check_user]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_MULTI_FACTOR) {
            if (resultCode == RESULT_NEEDS_MFA_SIGN_IN) {
                val intent = Intent(this, MultiFactorSignInActivity::class.java)
                intent.putExtras(data!!.extras!!)
                startActivityForResult(intent, RC_MULTI_FACTOR)
            }
        }
    }

    private fun signOut() {
        auth.signOut()
        updateUI(null)
    }

    private fun sendEmailVerification() { // Disable button
        findViewById<View>(R.id.verifyEmailButton).isEnabled = false
        // Send verification email
        // [START send_email_verification]
        val user = auth.currentUser
        user!!.sendEmailVerification()
                .addOnCompleteListener(this) { task ->
                    // [START_EXCLUDE]
                    // Re-enable button
                    findViewById<View>(R.id.verifyEmailButton).isEnabled = true
                    if (task.isSuccessful) {
                        Toast.makeText(this@MultiFactorActivity,
                                "Verification email sent to " + user.email,
                                Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e(TAG, "sendEmailVerification", task.exception)
                        Toast.makeText(this@MultiFactorActivity,
                                "Failed to send verification email.",
                                Toast.LENGTH_SHORT).show()
                    }
                    // [END_EXCLUDE]
                }
        // [END send_email_verification]
    }

    private fun reload() {
        auth.currentUser!!.reload().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                updateUI(auth.currentUser)
                Toast.makeText(this@MultiFactorActivity,
                        "Reload successful!",
                        Toast.LENGTH_SHORT).show()
            } else {
                Log.e(TAG, "reload", task.exception)
                Toast.makeText(this@MultiFactorActivity,
                        "Failed to reload user.",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressBar()
        if (user != null) {
            binding.status.text = getString(R.string.emailpassword_status_fmt,
                    user.email, user.isEmailVerified)
            binding.detail.text = getString(R.string.firebase_status_fmt, user.uid)
            val secondFactors = user.multiFactor.enrolledFactors
            if (secondFactors.isEmpty()) {
                findViewById<View>(R.id.unenrollMfa).visibility = View.GONE
            } else {
                findViewById<View>(R.id.unenrollMfa).visibility = View.VISIBLE
                val sb = StringBuilder("Second Factors: ")
                val delimiter = ", "
                for (x in secondFactors) {
                    sb.append((x as PhoneMultiFactorInfo).phoneNumber + delimiter)
                }
                sb.setLength(sb.length - delimiter.length)
                binding.mfaInfo.text = sb.toString()
            }
            findViewById<View>(R.id.emailPasswordButtons).visibility = View.GONE
            findViewById<View>(R.id.signedInButtons).visibility = View.VISIBLE
            val reloadVisibility = if (secondFactors.isEmpty()) View.VISIBLE else View.GONE
            findViewById<View>(R.id.reloadButton).visibility = reloadVisibility
            if (user.isEmailVerified) {
                findViewById<View>(R.id.verifyEmailButton).visibility = View.GONE
                findViewById<View>(R.id.enrollMfa).visibility = View.VISIBLE
            } else {
                findViewById<View>(R.id.verifyEmailButton).visibility = View.VISIBLE
                findViewById<View>(R.id.enrollMfa).visibility = View.GONE
            }
        } else {
            binding.status.setText(R.string.multi_factor_signed_out)
            binding.detail.text = null
            binding.mfaInfo.text = null
            findViewById<View>(R.id.emailPasswordButtons).visibility = View.VISIBLE
            findViewById<View>(R.id.signedInButtons).visibility = View.GONE
        }
    }

    private fun showDisclaimer() {
        AlertDialog.Builder(this)
                .setTitle("Warning")
                .setMessage("Multi-factor authentication with SMS is currently only available for " +
                        "Google Cloud Identity Platform projects. For more information see: " +
                        "https://cloud.google.com/identity-platform/docs/android/mfa")
                .setPositiveButton("OK", null)
                .show()
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.emailSignInButton -> {
                startActivityForResult(Intent(this, EmailPasswordActivity::class.java), RC_MULTI_FACTOR)
            }
            R.id.signOutButton -> signOut()
            R.id.verifyEmailButton -> sendEmailVerification()
            R.id.enrollMfa -> startActivity(Intent(this, MultiFactorEnrollActivity::class.java))
            R.id.unenrollMfa -> startActivity(Intent(this, MultiFactorUnenrollActivity::class.java))
            R.id.reloadButton -> reload()
        }
    }

    companion object {
        const val RESULT_NEEDS_MFA_SIGN_IN = 42
        private const val TAG = "MultiFactor"
        private const val RC_MULTI_FACTOR = 9005
    }
}
