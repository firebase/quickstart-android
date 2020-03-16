package com.google.firebase.quickstart.auth.kotlin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseException
import com.google.firebase.auth.MultiFactorResolver
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.PhoneMultiFactorInfo
import com.google.firebase.quickstart.auth.R
import com.google.firebase.quickstart.auth.java.BaseActivity
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.finishMfaSignIn
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor1
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor2
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor3
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor4
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.phoneFactor5
import kotlinx.android.synthetic.main.activity_multi_factor_sign_in.smsCode
import java.util.concurrent.TimeUnit

/**
 * Activity that handles MFA sign-in
 */
class MultiFactorSignInActivity : BaseActivity(), View.OnClickListener {

    private lateinit var multiFactorResolver: MultiFactorResolver
    private var lastPhoneAuthCredential: PhoneAuthCredential? = null
    private var lastVerificationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_factor_sign_in)
        savedInstanceState?.let { onRestoreInstanceState(it) }

        // Users are currently limited to having 5 second factors
        val phoneFactorButtonList = listOf(
                phoneFactor1, phoneFactor2, phoneFactor3, phoneFactor4, phoneFactor5)
        for (button in phoneFactorButtonList) {
            button.visibility = View.GONE
        }

        finishMfaSignIn.setOnClickListener(this)
        multiFactorResolver = retrieveResolverFromIntent(intent)

        val multiFactorInfoList = multiFactorResolver.hints
        for (i in multiFactorInfoList.indices) {
            val phoneMultiFactorInfo = multiFactorInfoList[i] as PhoneMultiFactorInfo
            val button = phoneFactorButtonList[i]
            button.visibility = View.VISIBLE
            button.text = phoneMultiFactorInfo.phoneNumber
            button.isClickable = true
            button.setOnClickListener(generateFactorOnClickListener(phoneMultiFactorInfo))
        }
    }

    public override fun onSaveInstanceState(bundle: Bundle) {
        super.onSaveInstanceState(bundle)
        bundle.putString(KEY_VERIFICATION_ID, lastVerificationId)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        lastVerificationId = savedInstanceState.getString(KEY_VERIFICATION_ID)
    }

    private fun generateFactorOnClickListener(phoneMultiFactorInfo: PhoneMultiFactorInfo): View.OnClickListener {
        return View.OnClickListener {
            PhoneAuthProvider.verifyPhoneNumber(
                    PhoneAuthOptions.newBuilder()
                            .setActivity(this@MultiFactorSignInActivity)
                            .setMultiFactorSession(multiFactorResolver.session)
                            .setMultiFactorHint(phoneMultiFactorInfo)
                            .setCallbacks(generateCallbacks()) // A timeout of 0 disables SMS-auto-retrieval.
                            .setTimeout(0L, TimeUnit.SECONDS)
                            .build())
        }
    }

    private fun generateCallbacks(): PhoneAuthProvider.OnVerificationStateChangedCallbacks {
        return object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                lastPhoneAuthCredential = phoneAuthCredential
                finishMfaSignIn.performClick()
                Toast.makeText(
                        this@MultiFactorSignInActivity, "Verification complete!", Toast.LENGTH_SHORT)
                        .show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                lastVerificationId = verificationId
                finishMfaSignIn.isClickable = true
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(
                        this@MultiFactorSignInActivity, "Error: " + e.message, Toast.LENGTH_SHORT)
                        .show()
            }
        }
    }

    private fun retrieveResolverFromIntent(intent: Intent): MultiFactorResolver {
        return intent.getParcelableExtra(EXTRA_MFA_RESOLVER)!!
    }

    private fun onClickFinishSignIn() {
        if (lastPhoneAuthCredential == null) {
            if (TextUtils.isEmpty(smsCode.text.toString())) {
                Toast.makeText(
                        this@MultiFactorSignInActivity, "You need to enter an SMS code.", Toast.LENGTH_SHORT)
                        .show()
                return
            }
            lastPhoneAuthCredential = PhoneAuthProvider.getCredential(
                    lastVerificationId!!, smsCode.text.toString())
        }
        multiFactorResolver
                .resolveSignIn(PhoneMultiFactorGenerator.getAssertion(lastPhoneAuthCredential!!))
                .addOnSuccessListener {
                    setResult(Activity.RESULT_OK)
                    finish()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                            this@MultiFactorSignInActivity, "Error: " + e.message, Toast.LENGTH_SHORT)
                            .show()
                }
    }

    override fun onClick(v: View) {
        if (v.id == R.id.finishMfaSignIn) {
            onClickFinishSignIn()
        }
    }

    companion object {
        private const val KEY_VERIFICATION_ID = "key_verification_id"
        const val EXTRA_MFA_RESOLVER = "EXTRA_MFA_RESOLVER"
    }
}