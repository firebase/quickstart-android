package com.google.firebase.quickstart.auth.kotlin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneMultiFactorGenerator
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.quickstart.auth.databinding.FragmentPhoneAuthBinding
import java.util.concurrent.TimeUnit

/**
 * Activity that allows the user to enroll second factors.
 */
class MultiFactorEnrollFragment : BaseFragment() {

    private var _binding: FragmentPhoneAuthBinding? = null
    private val binding: FragmentPhoneAuthBinding
        get() = _binding!!

    private var lastCodeVerificationId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentPhoneAuthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.titleText.text = "SMS as a Second Factor"
        binding.status.visibility = View.GONE
        binding.detail.visibility = View.GONE
        binding.buttonStartVerification.setOnClickListener { onClickVerifyPhoneNumber() }
        binding.buttonVerifyPhone.setOnClickListener { onClickSignInWithPhoneNumber() }
    }

    private fun onClickVerifyPhoneNumber() {
        val phoneNumber = binding.fieldPhoneNumber.text.toString()
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
                Toast.makeText(context, "SMS code has been sent", Toast.LENGTH_SHORT)
                        .show()
                lastCodeVerificationId = verificationId
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.w(TAG, "onVerificationFailed ", e)
                Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
            }
        }
        Firebase.auth
                .currentUser!!
                .multiFactor
                .session
                .addOnCompleteListener { task ->
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
                        Toast.makeText(context,
                                "Failed to get session: ${task.exception}", Toast.LENGTH_SHORT)
                                .show()
                    }
                }
    }

    private fun onClickSignInWithPhoneNumber() {
        val smsCode = binding.fieldVerificationCode.text.toString()
        if (TextUtils.isEmpty(smsCode)) {
            return
        }
        val credential = PhoneAuthProvider.getCredential(lastCodeVerificationId!!, smsCode)
        enrollWithPhoneAuthCredential(credential)
    }

    private fun enrollWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Firebase.auth
                .currentUser!!
                .multiFactor
                .enroll(PhoneMultiFactorGenerator.getAssertion(credential), /* displayName= */null)
                .addOnSuccessListener {
                    Toast.makeText(context, "MFA enrollment was successful", Toast.LENGTH_LONG)
                            .show()
                    findNavController().popBackStack()
                }
                .addOnFailureListener { e ->
                    Log.d(TAG, "MFA failure", e)
                    Toast.makeText(context,
                            "MFA enrollment was unsuccessful. $e",
                            Toast.LENGTH_LONG)
                            .show()
                }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "MfaEnrollFragment"
    }
}
