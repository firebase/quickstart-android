package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.java.BaseActivity
import kotlinx.android.synthetic.main.activity_phone_auth.buttonStartVerification
import kotlinx.android.synthetic.main.activity_phone_auth.buttonVerifyPhone
import kotlinx.android.synthetic.main.activity_phone_auth.detail
import kotlinx.android.synthetic.main.activity_phone_auth.fieldPhoneNumber
import kotlinx.android.synthetic.main.activity_phone_auth.fieldVerificationCode
import kotlinx.android.synthetic.main.activity_phone_auth.status
import kotlinx.android.synthetic.main.activity_phone_auth.titleText
import java.util.concurrent.TimeUnit

/**
 * Activity that allows the user to enroll second factors.
 */
class MultiFactorEnrollActivity : BaseActivity(), View.OnClickListener {

    private var lastCodeVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_phone_auth)
        titleText.text = "SMS as a Second Factor"
        status.visibility = View.GONE
        detail.visibility = View.GONE
        buttonStartVerification.setOnClickListener(this)
        buttonVerifyPhone.setOnClickListener(this)
    }

    private fun onClickVerifyPhoneNumber() {
        val phoneNumber = fieldPhoneNumber.text.toString()
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // Instant-validation has been disabled (see requireSmsValidation below).
                // Auto-retrieval has also been disabled (timeout is set to 0).
                // This should never be triggered.
                throw RuntimeException(
                        "onVerificationCompleted() triggered with instant-validation and auto-retrieval disabled.")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "onCodeSent:$verificationId")
                Toast.makeText(
                        this@MultiFactorEnrollActivity, "SMS code has been sent", Toast.LENGTH_SHORT)
                        .show()
                lastCodeVerificationId = verificationId
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed ", e)
                Toast.makeText(
                        this@MultiFactorEnrollActivity, "Verification failed: " + e.message, Toast.LENGTH_SHORT)
                        .show()
            }
        }
        FirebaseAuth.getInstance()
                .currentUser
                ?.multiFactor
                ?.session
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val phoneAuthOptions = PhoneAuthOptions.newBuilder()
                                .setPhoneNumber(phoneNumber) // A timeout of 0 disables SMS-auto-retrieval.
                                .setTimeout(0L, TimeUnit.SECONDS)
                                .setMultiFactorSession(task.result!!)
                                .setCallbacks(callbacks) // Disable instant-validation.
                                .requireSmsValidation(true)
                                .build()
                        PhoneAuthProvider.verifyPhoneNumber(phoneAuthOptions)
                    } else {
                        Toast.makeText(
                                this@MultiFactorEnrollActivity,
                                "Failed to get session: " + task.exception, Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }

    private fun onClickSignInWithPhoneNumber() {
        val smsCode = fieldVerificationCode.text.toString()
        if (TextUtils.isEmpty(smsCode)) {
            return
        }
        val credential = PhoneAuthProvider.getCredential(lastCodeVerificationId!!, smsCode)
        enrollWithPhoneAuthCredential(credential)
    }

    private fun enrollWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance()
                .currentUser
                ?.multiFactor
                ?.enroll(PhoneMultiFactorGenerator.getAssertion(credential), /* displayName= */null)
                ?.addOnSuccessListener {
                    Toast.makeText(
                            this@MultiFactorEnrollActivity,
                            "MFA enrollment was successful",
                            Toast.LENGTH_LONG)
                            .show()
                    finish()
                }
                ?.addOnFailureListener { e ->
                    Log.d(TAG, "MFA failure", e)
                    Toast.makeText(
                            this@MultiFactorEnrollActivity,
                            "MFA enrollment was unsuccessful. $e",
                            Toast.LENGTH_LONG)
                            .show()
                }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.buttonStartVerification -> onClickVerifyPhoneNumber()
            R.id.buttonVerifyPhone -> onClickSignInWithPhoneNumber()
        }
    }

    companion object {
        private const val TAG = "PhoneAuthActivity"
    }
}
