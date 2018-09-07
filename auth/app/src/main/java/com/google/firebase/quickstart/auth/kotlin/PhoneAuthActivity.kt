package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.util.Log
import android.view.View
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.quickstart.auth.R
import kotlinx.android.synthetic.main.activity_phone_auth.*
import java.util.concurrent.TimeUnit


class PhoneAuthActivity : AppCompatActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var mAuth: FirebaseAuth
    // [END declare_auth]

    private var mVerificationInProgress = false
    private var mVerificationId: String? = ""
    private lateinit var mResendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var mCallbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        // Assign click listeners
        buttonStartVerification.setOnClickListener(this)
        buttonVerifyPhone.setOnClickListener(this)
        buttonResend.setOnClickListener(this)
        signOutButton.setOnClickListener(this)

        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                // [START_EXCLUDE silent]
                mVerificationInProgress = false
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential)
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                // [START_EXCLUDE silent]
                mVerificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    fieldPhoneNumber.error = "Invalid phone number."
                    // [END_EXCLUDE]
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                            Snackbar.LENGTH_SHORT).show()
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED)
                // [END_EXCLUDE]
            }

            override fun onCodeSent(verificationId: String?,
                                    token: PhoneAuthProvider.ForceResendingToken) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:" + verificationId!!)

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT)
                // [END_EXCLUDE]
            }
        }
        // [END phone_auth_callbacks]
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.currentUser
        updateUI(currentUser)

        // [START_EXCLUDE]
        if (mVerificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(fieldPhoneNumber.text.toString())
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, mVerificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mVerificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }


    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)
                mCallbacks)        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        mVerificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    // [START resend_verification]
    private fun resendVerificationCode(phoneNumber: String,
                                       token: PhoneAuthProvider.ForceResendingToken?) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber, // Phone number to verify
                60, // Timeout duration
                TimeUnit.SECONDS, // Unit of timeout
                this, // Activity (for callback binding)
                mCallbacks, // OnVerificationStateChangedCallbacks
                token)             // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success")

                        val user = task.result.user
                        // [START_EXCLUDE]
                        updateUI(STATE_SIGNIN_SUCCESS, user)
                        // [END_EXCLUDE]
                    } else {
                        // Sign in failed, display a message and update the UI
                        Log.w(TAG, "signInWithCredential:failure", task.exception)
                        if (task.exception is FirebaseAuthInvalidCredentialsException) {
                            // The verification code entered was invalid
                            // [START_EXCLUDE silent]
                            fieldVerificationCode.error = "Invalid code."
                            // [END_EXCLUDE]
                        }
                        // [START_EXCLUDE silent]
                        // Update UI
                        updateUI(STATE_SIGNIN_FAILED)
                        // [END_EXCLUDE]
                    }
                }
    }
    // [END sign_in_with_phone]

    private fun signOut() {
        mAuth.signOut()
        updateUI(STATE_INITIALIZED)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(uiState: Int, user: FirebaseUser? = mAuth.currentUser, cred: PhoneAuthCredential? = null) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button
                enableViews(buttonStartVerification, fieldPhoneNumber)
                disableViews(buttonVerifyPhone, buttonResend, fieldVerificationCode)
                detail.text = null
            }
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field, the
                enableViews(buttonVerifyPhone, buttonResend, fieldPhoneNumber, fieldVerificationCode)
                disableViews(buttonStartVerification)
                detail.setText(R.string.status_code_sent)
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                enableViews(buttonStartVerification, buttonVerifyPhone, buttonResend, fieldPhoneNumber,
                        fieldVerificationCode)
                detail.setText(R.string.status_verification_failed)
            }
            STATE_VERIFY_SUCCESS -> {
                // Verification has succeeded, proceed to firebase sign in
                disableViews(buttonStartVerification, buttonVerifyPhone, buttonResend, fieldPhoneNumber,
                        fieldVerificationCode)
                detail.setText(R.string.status_verification_succeeded)

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.smsCode != null) {
                        fieldVerificationCode.setText(cred.smsCode)
                    } else {
                        fieldVerificationCode.setText(R.string.instant_validation)
                    }
                }
            }
            STATE_SIGNIN_FAILED ->
                // No-op, handled by sign-in check
                detail.setText(R.string.status_sign_in_failed)
            STATE_SIGNIN_SUCCESS -> {
            }
        }// Np-op, handled by sign-in check

        if (user == null) {
            // Signed out
            phoneAuthFields.visibility = View.VISIBLE
            signedInButtons.visibility = View.GONE

            status.setText(R.string.signed_out)
        } else {
            // Signed in
            phoneAuthFields.visibility = View.GONE
            signedInButtons.visibility = View.VISIBLE

            enableViews(fieldPhoneNumber, fieldVerificationCode)
            fieldPhoneNumber.text = null
            fieldVerificationCode.text = null

            status.setText(R.string.signed_in)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)
        }
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = fieldPhoneNumber.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            fieldPhoneNumber.error = "Invalid phone number."
            return false
        }

        return true
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonStartVerification -> {
                if (!validatePhoneNumber()) {
                    return
                }

                startPhoneNumberVerification(fieldPhoneNumber.text.toString())
            }
            R.id.buttonVerifyPhone -> {
                val code = fieldVerificationCode.text.toString()
                if (TextUtils.isEmpty(code)) {
                    fieldVerificationCode.error = "Cannot be empty."
                    return
                }

                verifyPhoneNumberWithCode(mVerificationId, code)
            }
            R.id.buttonResend -> resendVerificationCode(fieldPhoneNumber.text.toString(), mResendToken)
            R.id.signOutButton -> signOut()
        }
    }

    companion object {
        private val TAG = "PhoneAuthActivity"
        private val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private val STATE_INITIALIZED = 1
        private val STATE_VERIFY_FAILED = 3
        private val STATE_VERIFY_SUCCESS = 4
        private val STATE_CODE_SENT = 2
        private val STATE_SIGNIN_FAILED = 5
        private val STATE_SIGNIN_SUCCESS = 6
    }
}